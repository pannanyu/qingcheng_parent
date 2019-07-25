package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.service.order.CartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CarController {

    @Reference
    private CartService cartService;

    @GetMapping("/findCardList")
    public List<Map<String,Object>> findCartList(){
        String username= SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> cartList = cartService.findCartList(username);
        return cartList;
    }
    @GetMapping("/addItem")
    public Result addItem(String skuId,Integer num){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(username,skuId,num);
        return new Result();
    }
    @RequestMapping("/buy")
    public void buy(HttpServletResponse response,String skuId,Integer num) throws IOException {
    String username=SecurityContextHolder.getContext().getAuthentication().getName();
    cartService.addItem(username,skuId,num);
    response.sendRedirect("/cart.html");
}
@GetMapping("/updateChecked")
public Result updateChecked(String skuId,boolean checked){
        String username=SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateChecked(username,skuId,checked);
        return new Result();
}
@GetMapping("/deleteCheckedCart")
public Result deleteCheckedCart(){
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    cartService.deleteCheckedCart(username);
    return new Result();
};


}