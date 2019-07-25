package com.qingcheng.service.goods;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Sku;

import java.util.*;

/**
 * sku业务逻辑层
 */
public interface SkuService {


    public List<Sku> findAll();


    public PageResult<Sku> findPage(int page, int size);


    public List<Sku> findList(Map<String,Object> searchMap);


    public PageResult<Sku> findPage(Map<String,Object> searchMap,int page, int size);


    public Sku findById(String id);

    public void add(Sku sku);


    public void update(Sku sku);


    public void delete(String id);

    public void saveAllPriceToRedis();

    /**
     * 根据sku Id查询
     * @param id
     * @return
     */
    public Integer findPrice(String id);

    public void savaPriceToRedisById(String id,Integer price);

    public void deletePriceFromRedis(String id);


}
