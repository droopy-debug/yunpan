package com.qst.yunpan.dao;

import com.qst.yunpan.pojo.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao {
    User findUser(User user) throws Exception;              //验证用户的登录功能
    public void addUser(User user) throws Exception;
    public User checkUser(User user) throws Exception;
}
