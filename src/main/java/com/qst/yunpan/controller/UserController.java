package com.qst.yunpan.controller;

import com.qst.yunpan.service.FileService;
import com.qst.yunpan.service.UserService;
import com.qst.yunpan.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@Controller                                 //这是一个Spring注解，标识这个类是一个控制器，用于处理Web请求。
@RequestMapping("/user")                       //这个注解表示所有映射到这个控制器的方法的基础URL路径是/user。
public class UserController {
    @Autowired
    private UserService UserService;//实例化
    @Autowired
    private FileService FileService;//实例化
    @RequestMapping("/regist")                            //将/user/regist路径的请求映射到regist方法。

    //处理注册请求
    public String regist(HttpServletRequest request, HttpServletResponse response, User user) throws Exception{
        System.out.println(user.getUsername()+"-------"+user.getPassword());
        if(user.getUsername() == null || user.getPassword() == null||user.getUsername().equals("")||user.getPassword().equals("")){
            request.setAttribute("msg", "请输入用户名和密码");
            return "regist";
        }else{
            boolean isSuccess = UserService.addUser(user);                  //根据返回值判断注册是否成功
            if(isSuccess){
                FileService.addNewNameSpace(request, user.getUsername());                                //为新用户添加命名空间，并返回login视图。
                return "login";
            }else{
                request.setAttribute("msg", "注册失败");
                return "regist";                                                          //如果注册失败，设置一个消息属性并返回regist视图，提示注册失败。
            }
        }
    }

    @RequestMapping("/login")                                            //处理登录请求的代码，验证用户的登录功能
    public String login(HttpServletRequest request, User user){
        if(user.getUsername()==null||user.getUsername().equals("")||user.getPassword()==null||user.getPassword().equals("")){
            request.setAttribute("msg", "请输入用户名或密码");
            return "login";
        }
        User exsitUser = UserService.findUser(user);
        if(exsitUser != null){
            HttpSession session = request.getSession();
            session.setAttribute(User.NAMESPACE, exsitUser.getUsername());
            session.setAttribute("totalSize", exsitUser.getTotalSize());
            return "redirect:/index.action";
        }else{
            request.setAttribute("msg", "用户名或密码错误");
            return "login";
        }

    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request){                              //退出登录的代码，主要用于清空当前登录用户的session信息
        request.getSession().invalidate();
        return "redirect:/user/login.action";
    }


}



