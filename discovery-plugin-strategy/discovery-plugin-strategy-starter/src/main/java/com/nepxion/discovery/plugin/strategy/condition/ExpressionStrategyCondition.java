package com.nepxion.discovery.plugin.strategy.condition;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.common.entity.StrategyConditionEntity;
import com.nepxion.discovery.common.expression.DiscoveryExpressionResolver;
import com.nepxion.discovery.plugin.strategy.wrapper.StrategyWrapper;

public class ExpressionStrategyCondition extends AbstractStrategyCondition {
    private Pattern pattern = Pattern.compile(DiscoveryConstant.EXPRESSION_REGEX);

    @Autowired
    private StrategyWrapper strategyWrapper;

    @Override
    public boolean isTriggered(StrategyConditionEntity strategyConditionEntity) {
        Map<String, String> map = createMap(strategyConditionEntity);

        return isTriggered(strategyConditionEntity, map);
    }

    private Map<String, String> createMap(StrategyConditionEntity strategyConditionEntity) {
        String expression = strategyConditionEntity.getExpression();

        Map<String, String> map = new HashMap<String, String>();

        Matcher matcher = pattern.matcher(expression);
        while (matcher.find()) {
            String group = matcher.group();
            String name = StringUtils.substringBetween(group, DiscoveryConstant.EXPRESSION_SUB_PREFIX, DiscoveryConstant.EXPRESSION_SUB_SUFFIX);
            String value = null;

            // 从外置Header获取
            if (StringUtils.isBlank(value)) {
                value = strategyContextHolder.getHeader(name);
            }

            // 从内置Header获取
            if (StringUtils.isBlank(value)) {
                value = strategyWrapper.getHeader(name);
            }

            // 从外置Parameter获取
            if (StringUtils.isBlank(value)) {
                value = strategyContextHolder.getParameter(name);
            }

            // 从外置Cookie获取
            if (StringUtils.isBlank(value)) {
                value = strategyContextHolder.getCookie(name);
            }

            if (StringUtils.isNotBlank(value)) {
                map.put(name, value);
            }
        }

        return map;
    }

    @Override
    public boolean isTriggered(StrategyConditionEntity strategyConditionEntity, Map<String, String> map) {
        String expression = strategyConditionEntity.getExpression();

        return DiscoveryExpressionResolver.eval(expression, DiscoveryConstant.EXPRESSION_PREFIX, map, strategyTypeComparator);
    }
}