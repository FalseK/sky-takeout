package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @MapKey("createTime")
    Map<String, Map<String,Object>> countUsersGroupByCreateTime(Map map);


    @MapKey("createTime")
    Map<String,Map<String,Object>> countSumUserGroupByCreateTime(Map map);
}
