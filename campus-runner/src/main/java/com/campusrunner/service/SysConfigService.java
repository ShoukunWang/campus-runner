package com.campusrunner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusrunner.entity.SysConfig;

import java.util.List;
import java.util.Map;

public interface SysConfigService extends IService<SysConfig> {

    String getConfigValue(String key);

    Map<String, String> getMatchWeights();

    List<SysConfig> getAllConfigs();

    SysConfig updateConfig(String key, String value);
}
