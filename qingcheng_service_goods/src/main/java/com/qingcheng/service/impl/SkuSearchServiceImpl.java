package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.BrandMapper;
import com.qingcheng.dao.SpecMapper;
import com.qingcheng.service.goods.SkuSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private SpecMapper specMapper;

    public Map search(Map<String,String> searchMap) {

        //1封装查询请求
        SearchRequest searchRequest = new SearchRequest("sku");
        //查询"doc"类型，不写查询所有类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        //布尔查询构建器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1关键字搜素
        MatchQueryBuilder matchQueryBuilder= QueryBuilders.matchQuery("name",searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);
        //1.2商品分类过滤
        if (searchMap.get("category")!=null) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }   //1.3商品品牌过滤
        if (searchMap.get("brand")!=null) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
            boolQueryBuilder.filter(termQueryBuilder);
        }
        //1.4规格过滤
        for (String key : searchMap.keySet()) {
            //如果是规格参数
            if (key.startsWith("spec.")){
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key+".keyword", searchMap.get(key));
                boolQueryBuilder.filter(termQueryBuilder);

            }
        }
        //1.5价格过滤
        if (searchMap.get("price")!=null){
            String[] prices = searchMap.get("price").split("-");
            if (!prices[0].equals("0")){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(prices[0] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
            if (prices[1].equals("*")){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(prices[1] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //分页
        Integer pageNo=Integer.parseInt(searchMap.get("pageNo"));
        Integer pageSize=30;
        //开始索引公式。开始索引计算
        int fromIndex=(pageNo-1)*pageSize;
        searchSourceBuilder.from(fromIndex);
        searchSourceBuilder.size(pageSize);

        //排序
        String sort = searchMap.get("sort");
        String sortOrder = searchMap.get("sortOrder");

        if (!"".equals(sort)) {
            searchSourceBuilder.sort(sort, SortOrder.valueOf(sortOrder));
        }

        searchRequest.source(searchSourceBuilder);
        //聚合查询商品分类
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //2封装查询结果
        Map resultMap=new HashMap();
        try {
          SearchResponse  searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            long totalHits = searchHits.getTotalHits();
            System.out.println("记录数"+totalHits);
            SearchHit[] hits = searchHits.getHits();
            //2.1商品列表
            List<Map<String,Object>> resultList=new ArrayList<Map<String, Object>>();
            for (SearchHit hit : hits) {
                Map<String, Object> skuMap = hit.getSourceAsMap();

                //name高亮处理
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField name = highlightFields.get("name");
                Text[] fragments = name.fragments();
                //用高亮内容替换原有内容
                skuMap.put("name",fragments[0].toString());
                resultList.add(skuMap);
            }
            resultMap.put("rows",resultList);
            //2.2商品分类列表
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> aggregationsAsMap = aggregations.getAsMap();
            Terms tearms = (Terms) aggregationsAsMap.get("sku_category");
            List<?extends Terms.Bucket>buckets=tearms.getBuckets();
            List<String> categoryList=new ArrayList<String>();
            for (Terms.Bucket bucket : buckets) {
                System.out.println(bucket.getKeyAsString());
                categoryList.add(bucket.getKeyAsString());
            }
            resultMap.put("categoryList",categoryList);
            //2.3品牌列表
            //判断，没有品牌列表则查询.
            String categoryaName = "";
            if (searchMap.get("category") == null) {
                if (categoryList.size() > 0) {
                    //提取分类列表第一个分类
                }
                    categoryaName = categoryList.get(0);
                } else {
                    //取出参数中的分裂
                    categoryaName = searchMap.get("category");
                }
            if (searchMap.get("brand")==null) {
                List<Map> brandList = brandMapper.findListByCategoryName(categoryaName);
                resultMap.put("brandList", brandList);
            }
            //2.4规格列表
            List<Map> specList = specMapper.findListByCategoryName(categoryaName);
            for (Map spec : specList) {
                //规格选线列表
                String [] options = ((String)spec.get("options")).split(",");
                spec.put("options",options);
            }

            resultMap.put("specList",specList);


            //2.5页码
            //总记录数
            long totalCount = searchHits.getTotalHits();
            //总页数
            long pageCount=(totalCount%pageSize==0)?totalCount/pageSize:(totalCount/pageSize+1);

            resultMap.put("totalPages",pageCount);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultMap;
    }
}
