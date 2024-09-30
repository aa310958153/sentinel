package com.alibaba.csp.sentinel.dashboard.repository.auth;

/**
 * @Author: qiang.li
 * @Date: 2024/9/30 9:43
 * @Description: 控制台权限Repository
 */
public interface DashboardPermissionRepository<T> {

    /**
     * 保存权限
     *
     * @param permission
     */
    void save(T permission);

    /**
     * 获取所有权限
     */
    T get();

}
