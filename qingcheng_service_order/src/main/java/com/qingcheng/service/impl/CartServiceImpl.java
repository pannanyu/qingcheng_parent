package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Map<String, Object>> findCartList(String username) {
        System.out.println("从redis中提取购物车" + username);
        List<Map<String, Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Reference
    private SkuService skuService;
    @Reference
    private CategoryService categoryService;

    @Override
    public void addItem(String username, String skuId, Integer num) {
        //遍历购物车  如果存在商品则累加  不存在则添加到购物车
        //获取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        //是否在购物车中存在
        boolean flag = false;
        for (Map<String, Object> map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            //存在该商品
            if (orderItem.getSkuId().equals(skuId)) {

                if(orderItem.getNum()<=0){
                    cartList.remove(map);
                    flag=true;
                    break;
                }
                //单个商品重量
                int weight = orderItem.getWeight() / orderItem.getNum();

                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setMoney(orderItem.getPrice() * orderItem.getNum());
                orderItem.setWeight(weight * orderItem.getNum());

                if(orderItem.getNum()<=0) {
                    cartList.remove(map);
                }
                flag = true;
                break;
            }
        }
        if (flag == false) {
            Sku sku = skuService.findById(skuId);
            if (sku == null) {
                throw new RuntimeException("商品不存在");
            }
            if (!"1".equals(sku.getStatus())) {
                throw new RuntimeException("商品状态不合法");
            }
            if (num <= 0) {
                throw new RuntimeException("商品数量不合法");
            }
            OrderItem orderItem = new OrderItem();


            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setNum(num);
            orderItem.setName(sku.getName());
            orderItem.setImage(sku.getImage());
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(orderItem.getPrice() * num);
            if (sku.getWeight() == null) {
                sku.setWeight(0);
            }
            orderItem.setWeight(sku.getWeight() * num);
            //商品分类
            orderItem.setCategoryId3(sku.getCategoryId());
            Category category3 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            if (category3 == null) {
                category3 = categoryService.findById(sku.getCategoryId());
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(), category3);
            }
            orderItem.setCategoryId2(category3.getParentId());
            Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            if (category2 == null) {
                category2 = categoryService.findById(category3.getParentId());
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category3.getParentId(), category2);
            }
            orderItem.setCategoryId1(category2.getParentId());


            Map map = new HashMap();
            map.put("item", orderItem);
            map.put("checked", true);
            cartList.add(map);

        }
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);


    }

    @Override
    public boolean updateChecked(String username, String skuId, boolean checked) {
        //获取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        //判断缓存中有无此商品
        boolean isOk=false;
        for (Map<String, Object> map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            if (orderItem.getSkuId().equals(skuId)){
                map.put("checked",checked);
                isOk=true;
                break;
            }
        }
        if (isOk){
            //无则存入缓存
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
        }
        return isOk;
    }

    @Override
    public void deleteCheckedCart(String username) {
        //获取未选中的购物车
       List<Map<String, Object>> cartList = (List<Map<String, Object>>) findCartList(username).stream().
               filter(cart->(boolean)cart.get("checked")==false).
               collect(Collectors.toList());
    redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username,cartList);
    }
}
