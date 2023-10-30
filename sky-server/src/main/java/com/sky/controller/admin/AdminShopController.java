package com.sky.controller.admin;

import cn.hutool.core.util.BooleanUtil;
import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api("相关接口")
@RequestMapping("/admin/shop")
public class AdminShopController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final String shopKey = "shop_status";

    /**
     * 更改营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("修改店铺状态")
    public Result setStatus(@PathVariable Integer status){

        stringRedisTemplate.opsForValue().set(shopKey,status.toString());

        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus(){

        String status = stringRedisTemplate.opsForValue().get(shopKey);

        if (status == null){
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }

        return Result.success(Integer.parseInt(status));

    }

}
