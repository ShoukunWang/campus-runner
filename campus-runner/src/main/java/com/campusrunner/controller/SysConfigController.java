package com.campusrunner.controller;

import com.campusrunner.entity.SysConfig;
import com.campusrunner.service.SysConfigService;
import com.campusrunner.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class SysConfigController {

    @Autowired
    private SysConfigService sysConfigService;

    @GetMapping("/match-weights")
    public Result<Map<String, String>> getMatchWeights() {
        Map<String, String> weights = sysConfigService.getMatchWeights();
        return Result.success(weights);
    }

    @GetMapping("/list")
    public Result<List<SysConfig>> getAllConfigs(HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        List<SysConfig> configs = sysConfigService.getAllConfigs();
        return Result.success(configs);
    }

    @PutMapping("/update")
    public Result<SysConfig> updateConfig(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        String userType = (String) request.getAttribute("userType");
        if (!"ADMIN".equals(userType)) {
            return Result.error(403, "无权访问");
        }

        try {
            String key = (String) params.get("configKey");
            String value = (String) params.get("configValue");

            if (key == null || value == null) {
                return Result.error(400, "配置键和值不能为空");
            }

            SysConfig config = sysConfigService.updateConfig(key, value);
            return Result.success("更新成功", config);
        } catch (RuntimeException e) {
            return Result.error(5001, e.getMessage());
        }
    }
}
