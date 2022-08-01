package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    @Autowired
    CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }


    //分页查询菜品
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> pageInfo=new Page<Dish>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);//仅仅这样输出是不够的，无法展示菜品分类的名称，只能展示菜品分类的ID，还需要对应才可
        //对象拷贝，一个Page里返回的的list(名为records)里的数据类型是Dish，另一个Page里返回的list里数据类型是Dto，先拷贝除records外的信息。
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records=pageInfo.getRecords();//取出原来的records，包含很多Dish对象
        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //先将Dish和Dto对象里相同的属性拷贝过去
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId=item.getCategoryId();//拿到分类Id
            Category category=categoryService.getById(categoryId);//查名字
            if(category!=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);//名字赋给records里的每个新产生的Dto对象
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //回显菜品
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto=dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    //根据条件来查询菜品信息
//    @GetMapping("/list")
//    private R<List<Dish>> list(Dish dish){
//        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        queryWrapper.eq(Dish::getStatus,1);//启售状态
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list=dishService.list(queryWrapper);
//        return R.success(list);
//    }
    //此处重构函数适应移动端查询
    @GetMapping("/list")
    private R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);//启售状态
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list=dishService.list(queryWrapper);
        List<DishDto> dishDtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            //先将Dish和Dto对象里相同的属性拷贝过去
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId=item.getCategoryId();//拿到分类Id
            Category category=categoryService.getById(categoryId);//查名字
            if(category!=null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);//名字赋给records里的每个新产生的Dto对象
            }
            //当前菜品的Id
            Long dishId=item.getId();
            //查口味
            LambdaQueryWrapper<DishFlavor> queryWrapper2=new LambdaQueryWrapper<>();
            queryWrapper2.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList=dishFlavorService.list(queryWrapper2);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtoList);
    }
}
