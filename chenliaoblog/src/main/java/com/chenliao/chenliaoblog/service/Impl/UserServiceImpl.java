package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.mapper.UserMapper;
import com.chenliao.chenliaoblog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> findAll() {
        List<User> userList = userMapper.findAll();
        return userList;
    }

    @Override
    public void createUser(User user) {
        user.setPassWord(DigestUtils.md5DigestAsHex(user.getPassWord().getBytes()));
        userMapper.insert(user);
    }

    @Override
    public void updateUser(User user) {
        user.setPassWord(DigestUtils.md5DigestAsHex(user.getPassWord().getBytes()));
        userMapper.update(user);
    }

    @Override
    public void deleteUser(int id) {
        userMapper.delete(id);
    }

}
