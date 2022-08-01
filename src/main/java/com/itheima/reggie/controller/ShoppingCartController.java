package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}",shoppingCart);
//        1.拿到userid并赋值
//        2.判断是dish还是套餐
//        3.判断好后查看是否已经存在在购物车中，有就+1，没有的话保存新的
          Long userId= BaseContext.getCurrentId();
          shoppingCart.setUserId(userId);
          Long dishId=shoppingCart.getDishId();
          LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
          queryWrapper.eq(ShoppingCart::getUserId,userId);
          if(dishId!=null){
              queryWrapper.eq(ShoppingCart::getDishId,dishId);

          }else{
              queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
          }
          ShoppingCart shoppingCartOne=shoppingCartService.getOne(queryWrapper);
          if (shoppingCartOne!=null){
              Integer number=shoppingCartOne.getNumber();//number的类型用Integer而不是int
              shoppingCartOne.setNumber(number+1);
              shoppingCartService.updateById(shoppingCartOne);
          }else{
              shoppingCart.setNumber(1);//不要忘记保存时要设置userId(已经设置过了),还有number设为1
              shoppingCart.setCreateTime(LocalDateTime.now());
              shoppingCartService.save(shoppingCart);
              shoppingCartOne=shoppingCart;
          }
          return R.success(shoppingCartOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list=shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("购物车已清空！");

    }
}
