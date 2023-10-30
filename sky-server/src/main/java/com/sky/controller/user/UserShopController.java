package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@Api("店铺相关接口")
@RequestMapping("/user/shop")
public class UserShopController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final String shopKey = "shop_status";


    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result<Integer> getStatus(){

        String status = stringRedisTemplate.opsForValue().get(shopKey);
        return Result.success(Integer.parseInt(status));

    }

}
