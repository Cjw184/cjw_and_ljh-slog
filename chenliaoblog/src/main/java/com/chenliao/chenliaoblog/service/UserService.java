package com.chenliao.chenliaoblog.service;

import com.chenliao.chenliaoblog.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;



public interface UserService {
    /**
     * 查询所有用户列表
     * @return
     */
    List<User> findAll();

    /**
     * 添加用户
     * @param user
     */
    void createUser(User user);

    /**
     * 修改用户信息
     * @param user
     */
    void updateUser(User user);

    /**
     * 删除用户
     * @param id
     */
    void deleteUser(int id);

}
