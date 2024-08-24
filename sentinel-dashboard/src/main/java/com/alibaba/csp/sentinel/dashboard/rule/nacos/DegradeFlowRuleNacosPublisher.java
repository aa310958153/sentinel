package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author shuyouyu
 * @date 2022/3/2 18:22
 */
@Component("degradeFlowRuleNacosPublisher")
public class DegradeFlowRuleNacosPublisher implements DynamicRulePublisher<List<DegradeRuleEntity>> {
    @Autowired
    private ConfigService configService;

    @Autowired
    private Converter<List<DegradeRuleEntity>, String> converter;

    @Override
    public boolean publish(String app, List<DegradeRuleEntity> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return false;
        }
        configService.publishConfig(app + NacosConfigUtil.DEGRADE_FLOW_DATA_ID_POSTFIX, NacosConfigUtil.GROUP_ID, converter.convert(rules));
        return false;
    }
}
