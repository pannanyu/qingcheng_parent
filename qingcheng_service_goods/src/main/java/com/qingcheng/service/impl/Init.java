package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 实现InitializingBean接口的类会在启动时自动调用。
 */
public class Init implements InitializingBean {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;
    public void afterPropertiesSet() throws Exception {
        System.out.println("缓存预热！！！！！！");
        //加载商品分类导航缓存
        categoryService.saveCategoryTreeRedis();
        //加载价格数据
        skuService.saveAllPriceToRedis();

    }
}
