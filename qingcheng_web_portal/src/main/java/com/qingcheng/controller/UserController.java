package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.user.User;
import com.qingcheng.service.user.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 发送短信验证。
     * @param phone
     * @return
     */
    @GetMapping("/sendSms")
    public Result sendSms(String phone){
        System.out.println(userService);
        userService.sendSms(phone);
        return new Result();
    }

    @PostMapping("/save")
    public Result save(@RequestBody User user, String smsCode){
        //密码加密
        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
        String encode = encoder.encode(user.getPassword());
        user.setPassword(encode);

        userService.add(user,smsCode);
        return new Result();
    }
}
