package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Api(tags = "C端菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;


    @GetMapping("/list")
    public Result<List<DishVO>> listDishByCategoryId(Long categoryId){
        return dishService.getDishByCategoryIdWithFlavors(categoryId);
    }

}
