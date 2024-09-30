package com.alibaba.csp.sentinel.dashboard.datasource.entity.permission;

import java.util.List;

/**
 * @Author: qiang.li
 * @Date: 2024/9/29 14:19
 * @Description: 仪表板权限组权限实体
 */
public class DashboardPermissionGroupEntity {

    /**
     * 权限组名字
     */
    private String name;

    /**
     * 权限组服务
     */
    private List<String> serviceNameList;

    /**
     * 哪些团队可以看到
     */

    private List<String> tagList;

    /**
     * 哪些用户可以看到
     */
    private List<String> userList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getServiceNameList() {
        return serviceNameList;
    }

    public void setServiceNameList(List<String> serviceNameList) {
        this.serviceNameList = serviceNameList;
    }

    public List<String> getTagList() {
        return tagList;
    }

    public void setTagList(List<String> tagList) {
        this.tagList = tagList;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }
}
