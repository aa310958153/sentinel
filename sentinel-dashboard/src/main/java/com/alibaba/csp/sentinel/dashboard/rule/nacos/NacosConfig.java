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
package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.permission.DashboardPermissionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import java.util.Optional;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Configuration
public class NacosConfig {


    /**
     * 注⼊配置⽂件中的信息
     */
    @Value("${nacos.addr:localhost}")
    private String serverAddr;

    @Value("${nacos.namespace:f981fdb6-c9f2-455e-814e-c48bf33ced62}")
    private String namespace;

    @Value("${nacos.username:}")
    private String username;

    @Value("${nacos.password:}")
    private String password;

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 流控规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    /**
     * 限流规则的编码器
     * @return
     */
    @Bean
    public Converter<List<DegradeRuleEntity>, String> degradeFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 限流规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<DegradeRuleEntity>> degradeFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRuleEntity.class);
    }

    /**
     * 热点参数规则的编码器
     * @return
     */
    @Bean
    public Converter<List<ParamFlowRule>, String> paramFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 热点参数规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<ParamFlowRule>> paramFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, ParamFlowRule.class);
    }

    /**
     * 网关api规则的编码器
     * @return
     */
    @Bean
    public Converter<List<ApiDefinitionEntity>, String> gatewayApiRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 网关api规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<ApiDefinitionEntity>> gatewayApiRuleEntityDecoder() {
        return s -> JSON.parseArray(s, ApiDefinitionEntity.class);
    }

    /**
     * 网关流控规则的编码器
     * @return
     */
    @Bean
    public Converter<List<GatewayFlowRuleEntity>, String> gatewayFlowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 网关流控规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<GatewayFlowRuleEntity>> gatewayFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, GatewayFlowRuleEntity.class);
    }

    /**
     * 授权规则的编码器
     * @return
     */
    @Bean
    public Converter<List<AuthorityRule>, String> authorityRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 授权规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<AuthorityRule>> authorityFlowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, AuthorityRule.class);
    }

    /**
     * 系统规则的编码器
     * @return
     */
    @Bean
    public Converter<List<SystemRuleEntity>, String> systemRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 系统规则的解码器
     * @return
     */
    @Bean
    public Converter<String, List<SystemRuleEntity>> systemRuleEntityDecoder() {
        return s -> JSON.parseArray(s, SystemRuleEntity.class);
    }

    /**
     * 系统规则的编码器
     *
     * @return
     */
    @Bean
    public Converter<DashboardPermissionEntity, String> dashboardPermissionEntityEncoder() {
        return JSON::toJSONString;
    }

    /**
     * 系统规则的解码器
     *
     * @return
     */
    @Bean
    public Converter<String, DashboardPermissionEntity> dashboardPermissionEntityDecoder() {
        return s -> JSON.parseObject(s, DashboardPermissionEntity.class);
    }

    /**
     * 创建 Nacos 的配置服务
     * @return
     * @throws Exception
     */
    @Bean
    public ConfigService nacosConfigService() throws Exception {
        if(StringUtils.isBlank(namespace)&&StringUtils.isBlank(username)){
            return ConfigFactory.createConfigService(serverAddr);
        }
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        if(StringUtils.isNotBlank(namespace)) {
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
        }
        if(StringUtils.isNotBlank(username)) {
            properties.put(PropertyKeyConst.USERNAME, username);
        }
        if(StringUtils.isNotBlank(password)) {
            properties.put(PropertyKeyConst.PASSWORD, password);
        }

        return ConfigFactory.createConfigService(properties);
    }
}
