package com.campusrunner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusrunner.entity.SysConfig;
import com.campusrunner.mapper.SysConfigMapper;
import com.campusrunner.service.SysConfigService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements SysConfigService {

    @Override
    public String getConfigValue(String key) {
        SysConfig config = baseMapper.selectByKey(key);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Map<String, String> getMatchWeights() {
        Map<String, String> weights = new HashMap<>();
        String[] keys = {"match.weight.distance", "match.weight.credit", "match.weight.reward", "match.weight.completion"};
        
        for (String key : keys) {
            String value = getConfigValue(key);
            if (value != null) {
                weights.put(key.replace("match.weight.", ""), value);
            }
        }
        
        return weights;
    }

    @Override
    public List<SysConfig> getAllConfigs() {
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysConfig::getConfigKey);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public SysConfig updateConfig(String key, String value) {
        SysConfig config = baseMapper.selectByKey(key);
        if (config == null) {
            throw new RuntimeException("配置项不存在");
        }
        
        config.setConfigValue(value);
        config.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(config);
        
        return config;
    }
}
