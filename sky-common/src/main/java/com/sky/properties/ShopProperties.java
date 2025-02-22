package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.shop")
@Data
public class ShopProperties {

    //商店地址
    private String address;
    //经度值
    private float lng;
    //纬度值
    private float lat;

}
