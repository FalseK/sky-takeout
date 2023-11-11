package com.sky.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.BaiduApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component

public class BaiduUtil {

    @Autowired
    private BaiduApiProperties baiduApiProperties;

    /**
     * 获取格式化地址的坐标数据
     * @param address
     * @return
     */
    public String getUserAddressLatLng(String address){
        String ak = baiduApiProperties.getAk();

        Map<String,String> geoParams = new LinkedHashMap<>();
        geoParams.put("address", address);
        geoParams.put("output", "json");
        geoParams.put("ak", ak);

        String geoResult = HttpClientUtil.doGet(baiduApiProperties.getGeoUrl(), geoParams);

        //解析返回数据
        JSONObject jsonObject = JSONUtil.parseObj(geoResult);

        if(!jsonObject.getStr("status").equals("0")){
            throw new OrderBusinessException("收货地址解析失败");
        }

        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");

        String userLat = location.getStr("lat");
        String userLng = location.getStr("lng");
        return userLat + "," + userLng;
    }

    /**
     * 获取地点origin到destination的距离
     *
     * @param origin
     * @param destination
     * @return
     */
    public Integer getRouteDistance(String origin,String destination){

        String ak = baiduApiProperties.getAk();

        Map<String,String> routPlanParams = new LinkedHashMap<>();
        routPlanParams.put("origin", origin);
        routPlanParams.put("destination", destination);
        routPlanParams.put("steps_info","0");
        routPlanParams.put("ak", ak);

        String routPlanResult = HttpClientUtil.doGet(baiduApiProperties.getRoutePlanUrl(), routPlanParams);

        JSONObject jsonObject = JSONUtil.parseObj(routPlanResult);

        if(!jsonObject.getStr("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        String routesJSON = JSONUtil.parseObj(routPlanResult).getJSONObject("result").getStr("routes");
        return  (Integer) JSONUtil.parseArray(routesJSON).get(0, JSONObject.class, true).get("distance");
    }

}
