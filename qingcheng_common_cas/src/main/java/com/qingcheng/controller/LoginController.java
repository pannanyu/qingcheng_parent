package com.qingcheng.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    /**
     * 获取用户名
     * @return
     */
    @GetMapping("/username")
    public Map us1ername(){
        String username= SecurityContextHolder.getContext().getAuthentication().getName();

        System.out.println("当前登录用户:"+username);
        if ("anonymousUser".equals(username)){
            username="";
        }
        Map map=new HashMap();
        map.put("username",username);
        return map;
    }
}
