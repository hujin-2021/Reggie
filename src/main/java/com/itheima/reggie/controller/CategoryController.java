package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//分类管理
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){
        //新增分类
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize){
        Page<Category> pageinfo=new Page<>(page,pageSize);
        //设置排序条件,构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        //进行分页查询
        categoryService.page(pageinfo,queryWrapper);
        return R.success(pageinfo);

    }

    //根据ID删除分类
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除分类，id为：{}",ids);
        categoryService.remove(ids);
        return R.success("分类信息删除成功");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
         categoryService.updateById(category);
         return R.success("修改信息成功！");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> list=categoryService.list(queryWrapper);
        return R.success(list);
    }
}