package com.qst.yunpan.interceptor;

import com.qst.yunpan.utils.UserUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoignInterceptor implements HandlerInterceptor {                         //实现拦截器接口的类

    // 请求处理完成后调用的方法
    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {

    }

    // 请求处理之后，生成视图之前调用的方法
    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
            throws Exception {

    }

    // 请求处理之前调用的方法
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
        String url = request.getRequestURI();

        // 判断请求的URL是否包含以下特定字符串
        if (url.indexOf("login.action") >= 0 || url.indexOf("regist.action") >= 0 || url.indexOf("share.action") >= 0 || url.indexOf("getShareFiles.action") >= 0 || url.indexOf("download.action") >= 0 || url.indexOf("loginForApp.action") >= 0 || url.indexOf("getAppFiles.action") >= 0|| url.indexOf("uploadForApp.action") >= 0) {
            return true;// 如果包含特定字符串，则放行请求
        }

        // 获取当前登录用户名
        String username = UserUtils.getUsername(request);

        if (username != null) {
            return true;    // 如果用户名不为空，则放行请求
        }

        // 如果不满足条件，则重定向到登录页面
        response.sendRedirect("user/login.action");
        return false;// 阻止请求继续执行
    }

}