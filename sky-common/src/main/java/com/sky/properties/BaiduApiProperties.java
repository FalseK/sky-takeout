package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.baidu-api")
@Data
public class BaiduApiProperties {

    //ak
    private String ak;

    //geoUrl
    private String geoUrl;

    //routPlanUrl
    private String routePlanUrl;
}
