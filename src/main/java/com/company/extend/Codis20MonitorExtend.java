package com.company.extend;

import com.company.model.CodisProxyInfo;
import com.company.model.CodisServer;
import com.company.model.CodisServerGroup;
import com.company.tool.Tool_OkHttp;
import com.company.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon on 2016/12/26.
 */
public class Codis20MonitorExtend {

    private static final Logger LOGGER = Logger.getLogger(Codis20MonitorExtend.class);

    /**
     * 根据dashboard提供的接口，获取所有在线的代理
     * @param dashboard
     * @return <proxyHost:proxyPort, CodisProxyInfo>
     */
    public static Map<String, CodisProxyInfo> loadOnlineProxy(String dashboard) {
        Preconditions.checkNotNull(dashboard);
        Map<String, CodisProxyInfo> result = new HashMap<>();

        try {
            String body = Tool_OkHttp.do_get("http://".concat(dashboard).concat("/api/proxy/list"), null, null);
            List<CodisProxyInfo> codisProxyInfos = JsonUtil.fromJSON(body, new TypeReference<List<CodisProxyInfo>>() {});
            if (codisProxyInfos != null && codisProxyInfos.size() > 0) {
                codisProxyInfos.stream().forEach(codisProxyInfo -> {
                    if ("online".equals(codisProxyInfo.getState())) {
                        result.put(codisProxyInfo.getAddr(), codisProxyInfo);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.warn("解析dashboard的获取代理节点信息异常", e);
        }

        return result;
    }

    /**
     * 根据dashboard提供的接口，获取所有的Redis实例
     * @param dashboard
     * @return
     */
    public static List<CodisServer> loadRedisServer(String dashboard) {
        Preconditions.checkNotNull(dashboard);
        List<CodisServer> codisServers = new LinkedList<>();

        try {
            String body = Tool_OkHttp.do_get("http://".concat(dashboard).concat("/api/server_groups"), null, null);
            List<CodisServerGroup> codisServerGroups = JsonUtil.fromJSON(body, new TypeReference<List<CodisServerGroup>>() {});
            codisServerGroups.stream().forEach(codisServerGroup -> {codisServers.addAll(codisServerGroup.getServers());});
        } catch (Exception e) {
            LOGGER.warn("解析dashboard的获取Redis节点信息异常", e);
        }

        return codisServers;
    }

    /**
     * 根据dashboard提供的接口，检查Redis实例是否可用
     * @param dashboard
     * @param redisAddress
     * @return
     */
    public static boolean availableRedisServer(String dashboard, String redisAddress) {
        Preconditions.checkNotNull(dashboard);
        Preconditions.checkNotNull(redisAddress);
        boolean available = false;

        try {
            String body = Tool_OkHttp.do_get("http://".concat(dashboard).concat("/api/redis/").concat(redisAddress).concat("/stat"), null, null);
            if (!body.contains("connection refused")) {
                available = true;
            }
        } catch (Exception e) {
            LOGGER.warn("根据dashboard提供的接口检查Redis实例是否可用异常", e);
        }

        return available;
    }

    public static void main(String[] args) throws Exception {
        Codis20MonitorExtend.loadOnlineProxy("192.168.1.73:18087").forEach((addr, codisProxyInfo) -> {System.out.println(addr);});
        System.out.println(JsonUtil.toJSON(loadRedisServer("192.168.1.73:18087")));
        System.out.println(availableRedisServer("192.168.1.73:18087", "192.168.1.71:6001"));
    }

}
