package com.alibaba.csp.sentinel.dashboard.datasource.entity.permission;

import java.util.List;

/**
 * @Author: qiang.li
 * @Date: 2024/9/29 14:17
 * @Description: 仪表板相关的权限实体
 */
public class DashboardPermissionEntity {

    private List<DashboardPermissionGroupEntity> authGroupList;

    public List<DashboardPermissionGroupEntity> getAuthGroupList() {
        return authGroupList;
    }

    public void setAuthGroupList(
        List<DashboardPermissionGroupEntity> authGroupList) {
        this.authGroupList = authGroupList;
    }


}
