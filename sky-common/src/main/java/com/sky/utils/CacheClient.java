package com.sky.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
@Slf4j
public class CacheClient {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    // 插入数据到Redis，设置缓存TTL
    public void set(String key, Object value, Long time, TimeUnit unit) {

        if (value == null) {
            return;
        }
        String dataJSON = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, dataJSON, time, unit);

    }

    // 插入数据到Redis
    public void set(String key, Object value) {

        if (value == null) {
            return;
        }
        String dataJSON = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key, dataJSON);

    }



    public <T, ID> List<T> queryListInCache(String keyPrefix, ID id, Class<T> dataType, Function<ID, List<T>> dbFallback,
                                                    Long time, TimeUnit unit) {


        // 1.接收id，到redis中查询缓存

        String key = keyPrefix + id;

        String json = stringRedisTemplate.opsForValue().get(key);


        // 2.缓存中取到数据，返回，取不到查询数据库
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.parseArray(json).toList(dataType);
        }

        // 3.数据库中取到数据返回并添加到缓存，取不到返回错误
        List<T> data = dbFallback.apply(id);

        if (time == null || unit == null){
            this.set(key,data);
            return data;
        }

        this.set(key, data, time, unit);

        return data;
    }


    public void cleanCache(String keyPrefix) {
        Set keys = stringRedisTemplate.keys(keyPrefix + "*");

        stringRedisTemplate.delete(keys);
    }

}
