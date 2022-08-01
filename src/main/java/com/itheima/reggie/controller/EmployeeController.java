package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    //员工登录方法
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
//        1.将密码做md5处理
//        2.根据用户名username查数据
//        3.查不到返回登陆失败
//        4.密码不一致返回登陆失败
//        5.员工状态禁用则返回被禁用
//        6.成功，id存入session，返回登陆成功结果
        String password =employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(queryWrapper);
        if(emp==null){
            return R.error("登陆失败");
        }
        if(!emp.getPassword().equals(password)){
            return R.error("登陆失败");
        }
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    //员工退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
          //清理当前员工信息
         request.getSession().removeAttribute("employee");
         return R.success("退出成功");
    }

    //新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee) {
        log.info("新增员工,员工信息：{}", employee.toString());
        //设置初始密码123456，进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //获得当前登录用户ID
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("/page={},pageSize={},name={}",page,pageSize,name);

        //构造分页器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);//姓名建议使用like
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    //根据id修改员工信息，不只是禁用启用
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        long id=Thread.currentThread().getId();
        log.info("线程id为：{}",id);
        Long empId=(Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
        //这里无法成功改过来，因为页面中数据精度只有16位，数据库里的id有19位，传到前端后丢失精度
        //给前端传数据要将Long转为字符串
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据Id查询员工信息");
        Employee employee=employeeService.getById(id);
        if(employee!=null){
        return R.success(employee);
        }
        return R.error("没有查询到相关信息");
    }
}
