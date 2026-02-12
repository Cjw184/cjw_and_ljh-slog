package com.chenliao.chenliaoblog.mapper;

import com.chenliao.chenliaoblog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 查询全部用户信息
     *
     * @return
     */
    List<User> findAll();

    /**
     * 添加用户
     * @param user
     */
    void insert(User user);

    /**
     * 更新用户
     * @param user
     */
    void update(User user);

    /**
     * 删除用户
     * @param id
     * @return
     */
    int delete(@Param("id")int id);
    /**
     * 根据用户id查找
     * @param userId
     * @return
     */
    User getUserById(Integer userId);

    User getByUsername(String username);

}
