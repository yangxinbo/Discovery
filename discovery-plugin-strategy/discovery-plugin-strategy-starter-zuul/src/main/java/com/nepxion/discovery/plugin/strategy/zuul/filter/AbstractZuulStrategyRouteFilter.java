package com.nepxion.discovery.plugin.strategy.zuul.filter;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.wrapper.StrategyWrapper;
import com.nepxion.discovery.plugin.strategy.zuul.constant.ZuulStrategyConstant;
import com.nepxion.discovery.plugin.strategy.zuul.monitor.ZuulStrategyMonitor;
import com.netflix.zuul.context.RequestContext;

public abstract class AbstractZuulStrategyRouteFilter extends ZuulStrategyRouteFilter {
    @Autowired
    protected PluginAdapter pluginAdapter;

    @Autowired
    protected StrategyWrapper strategyWrapper;

    @Autowired(required = false)
    protected ZuulStrategyMonitor zuulStrategyMonitor;

    // 如果外界也传了相同的Header，例如，从Postman传递过来的Header，当下面的变量为true，以网关设置为优先，否则以外界传值为优先
    @Value("${" + ZuulStrategyConstant.SPRING_APPLICATION_STRATEGY_ZUUL_HEADER_PRIORITY + ":true}")
    protected Boolean zuulHeaderPriority;

    // 当以网关设置为优先的时候，网关未配置Header，而外界配置了Header，仍旧忽略外界的Header
    @Value("${" + ZuulStrategyConstant.SPRING_APPLICATION_STRATEGY_ZUUL_ORIGINAL_HEADER_IGNORED + ":true}")
    protected Boolean zuulOriginalHeaderIgnored;

    // Zuul上核心策略Header是否传递。当全局订阅启动时，可以关闭核心策略Header传递，这样可以节省传递数据的大小，一定程度上可以提升性能。核心策略Header，包含如下
    // 1. n-d-version
    // 2. n-d-region
    // 3. n-d-address
    // 4. n-d-version-weight
    // 5. n-d-region-weight
    // 6. n-d-id-blacklist
    // 7. n-d-address-blacklist
    // 8. n-d-env (不属于蓝绿灰度范畴的Header，只要外部传入就会全程传递)
    @Value("${" + ZuulStrategyConstant.SPRING_APPLICATION_STRATEGY_ZUUL_CORE_HEADER_TRANSMISSION_ENABLED + ":true}")
    protected Boolean zuulCoreHeaderTransmissionEnabled;

    @Value("${" + ZuulStrategyConstant.SPRING_APPLICATION_STRATEGY_ZUUL_ROUTE_FILTER_ORDER + ":" + ZuulStrategyConstant.SPRING_APPLICATION_STRATEGY_ZUUL_ROUTE_FILTER_ORDER_VALUE + "}")
    protected Integer filterOrder;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return filterOrder;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();

        String routeEnvironment = getRouteEnvironment();
        // 通过过滤器设置路由Header头部信息，并全链路传递到服务端
        if (StringUtils.isNotEmpty(routeEnvironment)) {
            ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_ENVIRONMENT, routeEnvironment, false);
        }

        if (zuulCoreHeaderTransmissionEnabled) {
            // 内置Header预先塞入
            Map<String, String> headerMap = strategyWrapper.getHeaderMap();
            if (MapUtils.isNotEmpty(headerMap)) {
                for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();

                    ZuulStrategyFilterResolver.setHeader(context, key, value, zuulHeaderPriority);
                }
            }

            String routeVersion = getRouteVersion();
            String routeRegion = getRouteRegion();
            String routeAddress = getRouteAddress();
            String routeVersionWeight = getRouteVersionWeight();
            String routeRegionWeight = getRouteRegionWeight();
            String routeIdBlacklist = getRouteIdBlacklist();
            String routeAddressBlacklist = getRouteAddressBlacklist();
            // 通过过滤器设置路由Header头部信息，并全链路传递到服务端
            if (StringUtils.isNotEmpty(routeVersion)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_VERSION, routeVersion, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_VERSION, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
            if (StringUtils.isNotEmpty(routeRegion)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_REGION, routeRegion, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_REGION, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
            if (StringUtils.isNotEmpty(routeAddress)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_ADDRESS, routeAddress, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_ADDRESS, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
            if (StringUtils.isNotEmpty(routeVersionWeight)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_VERSION_WEIGHT, routeVersionWeight, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_VERSION_WEIGHT, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
            if (StringUtils.isNotEmpty(routeRegionWeight)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_REGION_WEIGHT, routeRegionWeight, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_REGION_WEIGHT, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
            if (StringUtils.isNotEmpty(routeIdBlacklist)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_ID_BLACKLIST, routeIdBlacklist, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_ID_BLACKLIST, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
            if (StringUtils.isNotEmpty(routeAddressBlacklist)) {
                ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_ADDRESS_BLACKLIST, routeAddressBlacklist, zuulHeaderPriority);
            } else {
                ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_ADDRESS_BLACKLIST, zuulHeaderPriority, zuulOriginalHeaderIgnored);
            }
        } else {
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_VERSION);
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_REGION);
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_ADDRESS);
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_VERSION_WEIGHT);
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_REGION_WEIGHT);
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_ID_BLACKLIST);
            ZuulStrategyFilterResolver.ignoreHeader(context, DiscoveryConstant.N_D_ADDRESS_BLACKLIST);
        }

        // 对于服务A -> 网关 -> 服务B调用链
        // 域网关下(zuulHeaderPriority=true)，只传递网关自身的group，不传递服务A的group，起到基于组的网关端服务调用隔离
        // 非域网关下(zuulHeaderPriority=false)，优先传递服务A的group，基于组的网关端服务调用隔离不生效，但可以实现基于相关参数的熔断限流等功能
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_GROUP, pluginAdapter.getGroup(), zuulHeaderPriority);
        // 网关只负责传递服务A的相关参数（例如：serviceId），不传递自身的参数，实现基于相关参数的熔断限流等功能
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_TYPE, pluginAdapter.getServiceType(), false);
        String serviceAppId = pluginAdapter.getServiceAppId();
        if (StringUtils.isNotEmpty(serviceAppId)) {
            ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_APP_ID, serviceAppId, false);
        }
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_ID, pluginAdapter.getServiceId(), false);
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_ADDRESS, pluginAdapter.getHost() + ":" + pluginAdapter.getPort(), false);
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_VERSION, pluginAdapter.getVersion(), false);
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_REGION, pluginAdapter.getRegion(), false);
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_ENVIRONMENT, pluginAdapter.getEnvironment(), false);
        ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.N_D_SERVICE_ZONE, pluginAdapter.getZone(), false);

        // 调用链监控
        if (zuulStrategyMonitor != null) {
            zuulStrategyMonitor.monitor(context);
        }

        // 拦截侦测请求
        String path = context.getRequest().getServletPath();
        if (path.contains(DiscoveryConstant.INSPECTOR_ENDPOINT_URL)) {
            ZuulStrategyFilterResolver.setHeader(context, DiscoveryConstant.INSPECTOR_ENDPOINT_HEADER, pluginAdapter.getPluginInfo(null), true);
        }

        return null;
    }

    public PluginAdapter getPluginAdapter() {
        return pluginAdapter;
    }
}