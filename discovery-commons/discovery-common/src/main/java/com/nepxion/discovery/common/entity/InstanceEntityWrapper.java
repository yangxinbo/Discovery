package com.nepxion.discovery.common.entity;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nepxion.discovery.common.constant.DiscoveryMetaDataConstant;

public class InstanceEntityWrapper {
    public static String getContextPath(InstanceEntity instanceEntity) {
        Map<String, String> metadata = instanceEntity.getMetadata();
        String contextPath = metadata.get(DiscoveryMetaDataConstant.SPRING_APPLICATION_CONTEXT_PATH);
        if (StringUtils.isEmpty(contextPath)) {
            return "/";
        }

        return contextPath;
    }

    public static String getGroup(InstanceEntity instanceEntity) {
        Map<String, String> metadata = instanceEntity.getMetadata();
        String groupKey = metadata.get(DiscoveryMetaDataConstant.SPRING_APPLICATION_GROUP_KEY);
        if (StringUtils.isEmpty(groupKey)) {
            return StringUtils.EMPTY;
        }

        String group = metadata.get(groupKey);
        if (group == null) {
            return StringUtils.EMPTY;
        }

        return group;
    }

    public static String getPlugin(InstanceEntity instanceEntity) {
        Map<String, String> metadata = instanceEntity.getMetadata();
        String plugin = metadata.get(DiscoveryMetaDataConstant.SPRING_APPLICATION_DISCOVERY_PLUGIN);
        if (plugin == null) {
            return StringUtils.EMPTY;
        }

        return plugin;
    }

    public static boolean isRegisterControlEnabled(InstanceEntity instanceEntity) {
        Map<String, String> metadata = instanceEntity.getMetadata();
        String flag = metadata.get(DiscoveryMetaDataConstant.SPRING_APPLICATION_REGISTER_CONTROL_ENABLED);
        if (flag == null) {
            return true;
        }

        return Boolean.valueOf(flag);
    }

    public static boolean isDiscoveryControlEnabled(InstanceEntity instanceEntity) {
        Map<String, String> metadata = instanceEntity.getMetadata();
        String flag = metadata.get(DiscoveryMetaDataConstant.SPRING_APPLICATION_DISCOVERY_CONTROL_ENABLED);
        if (flag == null) {
            return true;
        }

        return Boolean.valueOf(flag);
    }

    public static boolean isConfigRestControlEnabled(InstanceEntity instanceEntity) {
        Map<String, String> metadata = instanceEntity.getMetadata();
        String flag = metadata.get(DiscoveryMetaDataConstant.SPRING_APPLICATION_CONFIG_REST_CONTROL_ENABLED);
        if (flag == null) {
            return true;
        }

        return Boolean.valueOf(flag);
    }
}