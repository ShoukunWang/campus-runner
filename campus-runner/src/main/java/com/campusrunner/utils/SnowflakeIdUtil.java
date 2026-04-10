package com.campusrunner.utils;

import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdUtil {

    public String nextIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    public Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    public String generateTaskNo() {
        return "TASK" + cn.hutool.core.date.DateUtil.format(cn.hutool.core.date.DateUtil.date(), "yyyyMMdd") + 
               String.format("%04d", (int)(Math.random() * 10000));
    }
}
