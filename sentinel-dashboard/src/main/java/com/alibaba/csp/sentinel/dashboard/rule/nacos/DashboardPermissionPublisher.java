package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.permission.DashboardPermissionEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author liqiang
 * @date 2024/09/29 18:24
 * @Description: 仪表板权限组权限Publisher
 */
@Component("dashboardPermissionPublisher")
public class DashboardPermissionPublisher implements DynamicRulePublisher<DashboardPermissionEntity> {

    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<DashboardPermissionEntity, String> converter;

    @Override
    public boolean publish(String app, DashboardPermissionEntity permission) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (permission == null) {
            return false;
        }
        configService.publishConfig(app + NacosConfigUtil.DASHBOARD_PERMISSION_DATA_ID_POSTFIX,
            NacosConfigUtil.GROUP_ID, converter.convert(permission));
        return true;
    }


}
