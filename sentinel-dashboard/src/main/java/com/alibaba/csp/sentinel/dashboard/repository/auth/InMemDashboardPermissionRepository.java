package com.alibaba.csp.sentinel.dashboard.repository.auth;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.permission.DashboardPermissionEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * @Author: qiang.li
 * @Date: 2024/9/30 9:43
 * @Description: 内存持久化控制台权限配置类
 */
@Repository
public class InMemDashboardPermissionRepository implements DashboardPermissionRepository<DashboardPermissionEntity> {

    private final AtomicReference<DashboardPermissionEntity> dashboardPermissionEntity = new AtomicReference<>();

    @Autowired
    @Qualifier("dashboardPermissionNacosProvider")
    private DynamicRuleProvider<DashboardPermissionEntity> ruleProvider;

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    public void save(DashboardPermissionEntity dashboardPermission) {
        dashboardPermissionEntity.set(dashboardPermission);
    }

    @Override
    public DashboardPermissionEntity get() {
        DashboardPermissionEntity permission = dashboardPermissionEntity.get();
        if (permission == null) {
            synchronized (this) {
                permission = dashboardPermissionEntity.get(); // 再次检查
                if (permission == null) {
                    try {
                        permission = ruleProvider.getRules(applicationName);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to retrieve rules", e);
                    }
                    if (permission != null) {
                        dashboardPermissionEntity.set(permission);
                    }
                }
            }
        }
        return permission;
    }
}
