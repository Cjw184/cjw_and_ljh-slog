package com.chenliao.chenliaoblog.controller;

import cn.hutool.core.util.StrUtil;
import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.service.UserService;
import com.chenliao.chenliaoblog.utils.JsonResult;
import com.chenliao.chenliaoblog.utils.PhoneUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * 用户列表
     * @return
     */
    @Operation(summary = "用户列表")
    @PostMapping("/list")
    public JsonResult<Object> list() {
        List<User> userList = userService.findAll();
        return JsonResult.success(userList);
    }

    /**
     * 添加用户
     * @return
     */
    @Operation(summary = "添加用户")
    @PostMapping("/create")
    public JsonResult<Object> userCreate(@RequestBody User user) {
        if (StrUtil.isEmpty(user.getPassWord())) {
            return JsonResult.error("密码为空，请填写密码！");
        }
        if (!PhoneUtils.checkMobile(user.getPhone())) {
            return JsonResult.error("手机号码格式错误！");
        }
        //这里java客户端采用驼峰命名，而传进来的数据应该使用下划线命名
      /*  //查看前端传递的参数
        System.out.println("========================================");
        System.out.println("======== User 对象字段值 ========");
        System.out.println("userName: " + user.getUserName());
        System.out.println("passWord: " + user.getPassWord());
        System.out.println("email: " + user.getEmail());
        System.out.println("nickname: " + user.getNickname());
        System.out.println("phone: " + user.getPhone());
        System.out.println("========================================");
        
        log.info("开始创建用户");
        log.info("接收前端创建user: {}", user);
        */
        userService.createUser(user);
        
       // log.info("========== 用户创建成功 ==========");
        return JsonResult.success();
    }

    /**
     *
     * 修改用户
     * @return
     */
    @Operation(summary = "修改用户")
    @PostMapping("/update")
    public JsonResult<Object> userUpdate(@RequestBody User user) {
        if (StrUtil.isEmpty(user.getPassWord())) {
            return JsonResult.error("密码为空，请填写密码！");
        }
        if (!PhoneUtils.checkMobile(user.getPhone())) {
            return JsonResult.error("手机号码格式错误！");
        }
        userService.updateUser(user);
        return JsonResult.success();
    }

    /**
     * 删除
     * @return
     */
    @Operation(summary = "删除用户")
    @PostMapping("/delete/{id}")
    public JsonResult<Object> userDelete(@PathVariable(value = "id") int id) {
        userService.deleteUser(id);
        return JsonResult.success();
    }

}
