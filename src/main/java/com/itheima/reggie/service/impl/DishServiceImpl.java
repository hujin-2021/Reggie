package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    //保存菜品和口味
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品到菜品表dish
        this.save(dishDto);
        //此时dishDto中的Dish_id已经自动赋值，需要给到flavor中去
        Long dishId=dishDto.getId();
        //保存口味
        List<DishFlavor> flavors=dishDto.getFlavors();
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //批量保存
        dishFlavorService.saveBatch(flavors);

    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //从dish表查基本信息
        Dish dish=this.getById(id);
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //从flavor表查口味
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors=dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        //查两表后，整合靠复制对象
        return dishDto;
    }


    //更新口味不能直接调用save方法，因为dish_Flavor表里同一个菜品(DishId)可能存在
    //多条记录，口味发生变化并不好直接覆盖，应该先删再加。
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理口味信息
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);//不能直接removeById,因为dishid不是dishflavor表的主键
        //重新添加口味
        List<DishFlavor> flavors=dishDto.getFlavors();
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        //批量保存
        dishFlavorService.saveBatch(flavors);
    }
}