package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveDish(@RequestBody DishDTO dishDTO) {
        return dishService.saveDish(dishDTO);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        return dishService.pageQuery(dishPageQueryDTO);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品状态")
    public Result changeStatus(@PathVariable Integer status, Long id) {

//        boolean flag = dishService.updateById(Dish.builder().status(status)
//                .id(id).build());
//
//        if (flag){
//            return Result.success();
//        }
//
//        return Result.error("更改失败");

        return dishService.changeStatus(status,id);


    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteBatch(@RequestParam List<Long> ids){
        return dishService.deleteBatch(ids);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getByIdWithFlavors(@PathVariable Long id){
        return dishService.getByIdWithFlavors(id);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        return dishService.updateDishWithFlavor(dishDTO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        return dishService.getByCategoryId(categoryId);
    }



}
