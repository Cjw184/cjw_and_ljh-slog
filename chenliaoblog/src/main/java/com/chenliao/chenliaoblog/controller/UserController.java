package com.chenliao.chenliaoblog.controller;

import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.service.UserService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    /**
     * 用户列表
     * @return
     */
    @PostMapping("/list")
    public JsonResult<Object> list() {
        List<User> userList = userService.findAll();
        return JsonResult.success(userList);
    }

    /**
     * 添加用户
     * @return
     */
    @PostMapping("/create")
    public JsonResult<Object> userCreate(@RequestBody User user) {
        userService.createUser(user);
        return JsonResult.success();
    }

    /**
     *
     * 修改用户
     * @return
     */
    @PostMapping("/update")
    public JsonResult<Object> userUpdate(@RequestBody User user) {
        userService.updateUser(user);
        return JsonResult.success();
    }

    /**
     * 删除
     * @return
     */
    @PostMapping("/delete/{id}")
    public JsonResult<Object> userDelete(@PathVariable(value = "id") int id) {
        userService.deleteUser(id);
        return JsonResult.success();
    }

}
