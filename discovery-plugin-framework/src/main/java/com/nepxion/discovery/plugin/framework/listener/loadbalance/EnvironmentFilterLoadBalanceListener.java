package com.nepxion.discovery.plugin.framework.listener.loadbalance;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.nepxion.discovery.plugin.framework.adapter.EnvironmentTransferAdapter;
import com.netflix.loadbalancer.Server;

public class EnvironmentFilterLoadBalanceListener extends AbstractLoadBalanceListener {
    @Autowired(required = false)
    private EnvironmentTransferAdapter environmentTransferAdapter;

    @Override
    public void onGetServers(String serviceId, List<? extends Server> servers) {
        applyEnvironmentFilter(serviceId, servers);
    }

    private void applyEnvironmentFilter(String providerServiceId, List<? extends Server> servers) {
        String environment = pluginAdapter.getEnvironment();
        boolean validated = validate(servers, environment);
        Iterator<? extends Server> iterator = servers.iterator();
        while (iterator.hasNext()) {
            Server server = iterator.next();
            String serverEnvironment = pluginAdapter.getServerEnvironment(server);
            if (validated) {
                // 环境隔离：调用端实例和提供端实例的元数据Metadata环境配置值相等才能调用
                if (!StringUtils.equals(serverEnvironment, environment)) {
                    iterator.remove();
                }
            } else {
                // 环境切流：环境隔离下，调用端实例找不到符合条件的提供端实例，把流量切到一个通用或者备份环境，例如：元数据Metadata环境配置值为common（该值可配置，但不允许为保留值default）
                if (environmentTransferAdapter != null && environmentTransferAdapter.isTransferred()) {
                    if (!StringUtils.equals(serverEnvironment, environmentTransferAdapter.getTransferredEnvironment())) {
                        iterator.remove();
                    }
                } else {
                    // 环境切流关闭，移除所有不匹配的实例
                    iterator.remove();
                }
            }
        }
    }

    // 判断环境是否要切流。只要调用端实例和至少一个提供端实例的元数据Metadata环境配置值相等，就不需要切流
    private boolean validate(List<? extends Server> servers, String environment) {
        for (Server server : servers) {
            String serverEnvironment = pluginAdapter.getServerEnvironment(server);
            if (StringUtils.equals(serverEnvironment, environment)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        // Highest priority
        return HIGHEST_PRECEDENCE + 3;
    }
}