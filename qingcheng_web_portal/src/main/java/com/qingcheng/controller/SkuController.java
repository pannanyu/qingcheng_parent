package com.qingcheng.controller;

import com.qingcheng.service.goods.SkuService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sku")
@CrossOrigin
public class SkuController {

    @Reference
    private SkuService skuService;

    @GetMapping("/price")
    public Integer price(String id){
        return skuService.findPrice(id);
    }
}
