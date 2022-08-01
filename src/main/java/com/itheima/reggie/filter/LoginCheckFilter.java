package com.itheima.reggie.filter;

//检查用户是否已经登录的过滤器

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebFault;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
//      1.获取本次请求的URI
//      2.判断本次请求是否需要处理
//      3.不需要则直接放行
//      4.判断登陆状态，已登录则放行
//      5.未登录则返回未登录结果
        String requestURI = request.getRequestURI();
        log.info("拦截到请求{}",requestURI);
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",//静态资源不拦截
                "/front/**",//静态资源不拦截
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean check = check(urls, requestURI);
        if (check) {
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户ID为：{}",request.getSession().getAttribute("employee"));
            Long empId=(Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            long id=Thread.currentThread().getId();
            log.info("线程id为：{}",id);
            filterChain.doFilter(request, response);
            return;
        }
        //移动端
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户ID为：{}",request.getSession().getAttribute("user"));
            Long userId=(Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            long id=Thread.currentThread().getId();
            log.info("线程id为：{}",id);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("用户未登录");
        //未登录通过输出流的方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }
    //路径匹配器
    public boolean check(String[] urls,String requestURI){
        for(String url:urls ){
            boolean match =PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
