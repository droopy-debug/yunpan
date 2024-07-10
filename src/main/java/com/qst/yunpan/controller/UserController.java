package com.qst.yunpan.controller;

import com.qst.yunpan.service.FileService;
import com.qst.yunpan.service.UserService;
import com.qst.yunpan.pojo.User;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;


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

            System.out.println(user.getUsername() + user.getPassword() + "pause 1");

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

    /**
     * APP端登录接口
     * @param req
     * @param rep
     * @throws Exception
     */
    @RequestMapping("/loginForApp")
    public void getjson(HttpServletRequest req, HttpServletResponse rep)
            throws Exception {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        System.out.println("安卓端访问中..............");

        PrintWriter writer = rep.getWriter();
        JSONObject object = new JSONObject();
        User exsitUser = UserService.findUser(user);
        if(exsitUser != null){
            HttpSession session = req.getSession();
            session.setAttribute(User.NAMESPACE, exsitUser.getUsername());
            session.setAttribute("totalSize", exsitUser.getTotalSize());
            //object.put("result", exsitUser);
            object.put("ret", "1000");
            object.put("msg", "登录成功");
            object.put("data", exsitUser);
        } else {
            //object.put("result", "fail");
            object.put("ret", "1001");
            object.put("msg", "登录失败");
        }
        writer.println(object.toString());
        writer.flush();
        writer.close();
    }

    /**
     * APP端注册接口
     * @param req
     * @param rep
     * @throws Exception
     */
    @RequestMapping("/registForApp")
    public void registForApp(HttpServletRequest req, HttpServletResponse rep)
            throws Exception {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        System.out.println("安卓端注册中..............");
        PrintWriter writer = rep.getWriter();
        JSONObject object = new JSONObject();

        if(username == null || password == null){
            //object.put("result", "error");//填写有误
            object.put("ret", "1003");
            object.put("msg", "填写有误");
        }else{
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            boolean isSuccess = UserService.addUser(user);
            if(isSuccess){
                //根据名字创建文件目录
                FileService.addNewNameSpace(req, user.getUsername());
                user.setPassword(password);
                User exsitUser = UserService.findUser(user);
                HttpSession session = req.getSession();
                session.setAttribute(User.NAMESPACE, exsitUser.getUsername());//
                session.setAttribute("totalSize", exsitUser.getTotalSize());
                //object.put("result", exsitUser);
                object.put("ret", "1000");
                object.put("msg", "注册成功");
                object.put("data", exsitUser);
            }else{
                //object.put("result", "fail");//注册失败
                object.put("ret", "1001");
                object.put("msg", "注册失败");
            }
        }
        writer.println(object.toString());
        writer.flush();
        writer.close();
    }


}



