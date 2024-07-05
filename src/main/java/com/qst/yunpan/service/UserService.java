package com.qst.yunpan.service;


import com.qst.yunpan.dao.UserDao;
import com.qst.yunpan.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qst.yunpan.pojo.User;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;
    public boolean addUser(User user) throws Exception{
        User users=userDao.checkUser(user);
        if(users==null) {
            user.setPassword(UserUtils.MD5(user.getPassword()));
            userDao.addUser(user);
        } 	else {
            return false;
        }
        return true;
    }

    public User findUser(User user) {                           //验证用户的登录功能
        try {
            user.setPassword(UserUtils.MD5(user.getPassword()));
            User exsitUser = userDao.findUser(user);
            return exsitUser;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
