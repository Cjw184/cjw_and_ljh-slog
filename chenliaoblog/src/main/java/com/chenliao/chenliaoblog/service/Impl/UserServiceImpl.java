package com.chenliao.chenliaoblog.service.Impl;

import com.chenliao.chenliaoblog.entity.User;
import com.chenliao.chenliaoblog.mapper.UserMapper;
import com.chenliao.chenliaoblog.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@Slf4j
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
    @Override
    public User findByUserId(Integer userId) {
        User user = userMapper.getUserById(userId);
        return user;
    }

    @Override
    public User login(User user) {
        String username = user.getUserName();
        String password = user.getPassWord();
        log.info("username:{}", username);
        log.info("password:{}", password);
        User user1 = userMapper.getByUsername(username);
        if(user1 == null){throw new RuntimeException("用户不存在");}
        //对比md5密码
        if(!user1.getPassWord().equals(DigestUtils.md5DigestAsHex(password.getBytes()))){throw new RuntimeException("密码错误");}
        //返回用户信息
        return user1;

    }


}
