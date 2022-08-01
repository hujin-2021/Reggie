package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional  //同时操控两张表，要保持数据的一致性
    public void saveWithDish(SetmealDto setmealDto) {
         //保存基本信息，操作setmeal，执行insert
         this.save(setmealDto);//这里会产生setmealid
         List<SetmealDish> setmealDishes=setmealDto.getSetmealDishes();
         //此时SetmealDish里只有Dishid，没有setmealid，需要赋值，用stream流处理
         setmealDishes.stream().map(((item)->{
             item.setSetmealId(setmealDto.getId());
             return item;
         })).collect(Collectors.toList());

         //保存套餐和菜品的关联信息，操作setmeal.dto，执行insert
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //先看是否有套餐还在售，不能删除
        LambdaQueryWrapper<Setmeal>  queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count=this.count(queryWrapper); //计数直接调用service的方法即可
        if(count>0){
            throw new CustomException("套餐未停售，不能删除");
        }
        this.removeByIds(ids);
        //删除关系表
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);
    }
}