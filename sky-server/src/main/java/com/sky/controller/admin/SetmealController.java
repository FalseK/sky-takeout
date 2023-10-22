package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "套餐相关接口")
@Slf4j
@RequestMapping("/admin/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result saveSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐{}",setmealDTO);
        return setmealService.saveSetmeal(setmealDTO);

    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> setmealPageQuery(SetmealPageQueryDTO setmealPageQueryDTO){
        return setmealService.pageQuery(setmealPageQueryDTO);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐启售，停售")
    public Result statusChange(@PathVariable Integer status, Long id){
        return setmealService.statusChange(status,id);
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result deleteBatch(@RequestParam List<Long> ids){
        return setmealService.deleteBatch(ids);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getByIdWithDishes(@PathVariable Long id){
        return setmealService.getByIdWithDishes(id);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result updateWithDishes(@RequestBody SetmealDTO setmealDTO){
        return setmealService.updateWithDishes(setmealDTO);
    }

}
