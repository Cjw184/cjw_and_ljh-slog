package com.chenliao.chenliaoblog.controller.admin;

import com.chenliao.chenliaoblog.annotation.OperationLog;
import com.chenliao.chenliaoblog.annotation.OperationType;
import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.service.UserService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chenliao.chenliaoblog.utils.JwtUtil;

@Slf4j
@RestController
@RequestMapping("/admin")
public class Login {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @OperationLog(desc = "用户登录", operationType = OperationType.SELECT)
    public JsonResult<User> login(@RequestBody User user) {
        log.info("登录：{}", user);

        User loginUser = userService.login(user);
        //生成token
        String token = JwtUtil.generateToken(loginUser.getId().longValue(), loginUser.getUserName());
        loginUser.setToken(token);
        return JsonResult.success(loginUser);
    }
}
