/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.discovery;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService.AuthUser;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.permission.DashboardPermissionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.permission.DashboardPermissionGroupEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo.Auth;
import com.alibaba.csp.sentinel.dashboard.repository.auth.DashboardPermissionRepository;
import com.alibaba.nacos.common.utils.CollectionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author leyou
 */
@Component
public class SimpleMachineDiscovery implements MachineDiscovery {

    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();

    @Resource
    private DashboardPermissionRepository<DashboardPermissionEntity> dashboardPermissionRepository;

    private final Logger logger = LoggerFactory.getLogger(SimpleMachineDiscovery.class);
    /**
     * 超级管理员账户
     */
    @Value("${auth.username:sentinel}")
    private String authUsername;
    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        AppInfo appInfo = apps.computeIfAbsent(machineInfo.getApp(), o -> new AppInfo(machineInfo.getApp(), machineInfo.getAppType()));
        appInfo.addMachine(machineInfo);
        return 1;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = apps.get(app);
        if (appInfo != null) {
            return appInfo.removeMachine(ip, port);
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(apps.keySet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        return apps.get(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return new HashSet<>(apps.values());
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        apps.remove(app);
    }
    @Override
    public Set<AppInfo> getBriefApps(AuthUser authUser) {
        Set<AppInfo> briefApps = getBriefApps();
        if (CollectionUtils.isEmpty(briefApps)) {
            return briefApps;
        }

        //权限过滤
        briefApps = filterByAuthUser(briefApps, authUser);
        return briefApps;
    }

    private Set<AppInfo> filterByAuthUser(Set<AppInfo> briefApps, AuthUser authUser) {
        //超级管理员账户不过滤数据权限
        if (authUser.getId().equals(authUsername)) {
            return briefApps;
        }
        DashboardPermissionEntity permission = null;
        try {
            permission = dashboardPermissionRepository.get();
        } catch (Exception e) {
            logger.error("#118 filterByAuthUser error authUser={}", authUser, e);
        }
        if (permission == null || CollectionUtils.isEmpty(permission.getAuthGroupList())) {
            return Collections.emptySet();
        }
        final DashboardPermissionEntity finalPermission = permission;
        return briefApps.stream().filter(c -> {
            Set<MachineInfo> machines = c.getMachines();
            for (MachineInfo machine : machines) {
                Auth auth = machine.getAuth();
                //优先以服务上报的auth为准
                if (auth != null) {
                    return serverAuthCheck(auth, authUser);
                }
                //获取此服务配置的权限组
                List<DashboardPermissionGroupEntity> permisionGroupList = finalPermission.getAuthGroupList().stream()
                    .filter(authGroup -> CollectionUtils.isNotEmpty(authGroup.getServiceNameList())
                        && authGroup.getServiceNameList().contains(machine.getApp())).collect(
                        Collectors.toList());
                //判断当前用户是否有权限
                return permisionGroupList.stream().anyMatch(d -> {
                    if (CollectionUtils.isNotEmpty(d.getTagList()) && d.getTagList().contains(authUser.getTagId())) {
                        return true;
                    } else {
                        return CollectionUtils.isNotEmpty(d.getUserList()) && d.getUserList()
                            .contains(authUser.getId());
                    }
                });

            }
            return false;
        }).collect(Collectors.toSet());
    }

    private boolean serverAuthCheck(MachineInfo.Auth auth, AuthUser authUser) {
        if (Objects.isNull(auth)) {
            return false;
        }
        if (CollectionUtils.isNotEmpty(auth.getTagList()) && auth.getTagList().contains(authUser.getTagId())) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(auth.getUserList()) && auth.getUserList().contains(authUser.getId())) {
            return true;
        }
        return false;
    }
}
