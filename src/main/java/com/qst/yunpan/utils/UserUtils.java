package com.qst.yunpan.utils;

import com.qst.yunpan.pojo.User;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

public class UserUtils {

    public static String MD5(String password){                                   //MD5加密封装
        if(password!=null){
            return DigestUtils.md5DigestAsHex(password.getBytes()).toUpperCase();
        }else{
            return null;
        }
    }

    public static String getUsername(HttpServletRequest request){                           //从session获取用户名
        return (String) request.getSession().getAttribute(User.NAMESPACE);
    }
}
