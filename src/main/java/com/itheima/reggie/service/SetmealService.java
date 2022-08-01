package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    //新增套餐需要同时保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除
    public void deleteWithDish(List<Long> ids);
}