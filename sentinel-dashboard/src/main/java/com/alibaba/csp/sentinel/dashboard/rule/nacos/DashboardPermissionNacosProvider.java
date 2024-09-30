package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.permission.DashboardPermissionEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liqiang
 * @date 2024/09/29 18:24
 * @Description: 仪表板权限组权限Provider
 */

@Component("dashboardPermissionNacosProvider")
public class DashboardPermissionNacosProvider implements DynamicRuleProvider<DashboardPermissionEntity> {

    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<String, DashboardPermissionEntity> converter;

    @Override
    public DashboardPermissionEntity getRules(String appName) throws Exception {
        String rules = configService.getConfig(appName + NacosConfigUtil.DASHBOARD_PERMISSION_DATA_ID_POSTFIX,
            NacosConfigUtil.GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return null;
        }
        return converter.convert(rules);
    }
}
