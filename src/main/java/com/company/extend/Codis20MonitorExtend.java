package com.company.extend;

import com.company.model.CodisProxyInfo;
import com.company.model.CodisServer;
import com.company.model.CodisServerGroup;
import com.company.service.SmsService;
import com.company.service.impl.SmsServiceImpl;
import com.company.tool.Tool_Email;
import com.company.tool.Tool_HttpClient;
import com.company.tool.Tool_OkHttp;
import com.company.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon on 2016/12/26.
 */
public class Codis20MonitorExtend {

    private static final Logger LOGGER = Logger.getLogger(Codis20MonitorExtend.class);
    private static Tool_HttpClient httpClient = Tool_HttpClient.getInstance();
    private static SmsService smsService = new SmsServiceImpl();

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

    /**
     * 根据dashboard提供的接口，提交主备切换操作
     * @param dashboard
     * @param codisServer
     * @return
     */
    public static boolean promoteToMaster(String dashboard, CodisServer codisServer) {
        Preconditions.checkNotNull(dashboard);
        Preconditions.checkNotNull(codisServer);
        boolean isSucc = false;

        try {
            String body = httpClient.do_post_json("http://".concat(dashboard).concat("/api/server_group/") + codisServer.getGroup_id() + "/promote",
                    JsonUtil.toJSON(codisServer));
            if (!Strings.isNullOrEmpty(body) && body.equals("{\"msg\":\"OK\",\"ret\":0}")) {
                isSucc = true;
            }
        } catch (IOException e) {
            LOGGER.warn("根据dashboard提供的接口，提交主备切换操作异常", e);
        }

        return isSucc;
    }

    /**
     * 检查并进行主备切换操作
     * @param name
     * @param environment
     * @param dashboard
     * @param redisInfo
     * @return 无master节点挂，返回true；否则，只有当所有的已挂的master节点切换成功返回true
     */
    public static boolean autoPromote(String name, String environment, String dashboard, Map<Integer, Map<CodisServer, Boolean>> redisInfo) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(environment);
        Preconditions.checkNotNull(dashboard);
        Preconditions.checkNotNull(redisInfo);
        boolean isSucc = true;

        for (Integer group_id : redisInfo.keySet()) {
            Map<CodisServer, Boolean> availableRedisInfo = redisInfo.get(group_id);

            boolean masterAvailable = true; // 一个分组中mater节点是否存活
            CodisServer deadMasterRedis = null; // 记录挂了的master节点
            boolean slaveAvailable = true; // 一个分组中是否有存活的slave节点
            List<CodisServer> liveSlaves = new LinkedList<>(); // 存活的slave节点
            for (CodisServer codisServer : availableRedisInfo.keySet()) {
                if (codisServer.getType().equals("master") && !availableRedisInfo.get(codisServer)) { // 发现master节点挂了
                    masterAvailable = false;
                    deadMasterRedis = codisServer;
                }
                if (codisServer.getType().equals("slave") && availableRedisInfo.get(codisServer)) { // 发现存活的slave节点
                    liveSlaves.add(codisServer);
                }
            }

            // 如果出现master节点挂了，并且slave节点有存活的，就主备切换
            if (!masterAvailable && liveSlaves.size() > 0) {
                boolean oneSucc = promoteToMaster(dashboard, liveSlaves.get(0));
                Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控：Redis promote to master", "name: ".concat(name)
                        .concat("\r\nenvironment: ").concat(environment)
                        .concat("\r\ngroup id: " + group_id)
                        .concat("\r\nmaster dead: ").concat(deadMasterRedis.getAddr())
                        .concat("\r\nslave promote to master: ").concat(liveSlaves.get(0).getAddr())
                        .concat("\r\nresult: " + oneSucc)
                        .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                try {
                    smsService.sendOneSmsToOnePhone("Find group[" + group_id + "]master node[" + deadMasterRedis.getAddr() + "]dead, had do slave[" + liveSlaves.get(0).getAddr() + "]to master node, result" + oneSucc, "15001848348");
                } catch (UnsupportedEncodingException e) {}
                LOGGER.info("发现分组[" + group_id + "]中的master节点[" + deadMasterRedis.getAddr() + "]挂了，遂将slave节点[" + liveSlaves.get(0).getAddr() + "]提升为主节点，操作结果：" + oneSucc);
                isSucc = isSucc && oneSucc;
            }
        }

        return isSucc;
    }

    public static void main(String[] args) throws Exception {
        Codis20MonitorExtend.loadOnlineProxy("192.168.1.73:18087").forEach((addr, codisProxyInfo) -> {System.out.println(addr);});
        System.out.println(JsonUtil.toJSON(loadRedisServer("192.168.1.73:18087")));
        System.out.println(availableRedisServer("192.168.1.73:18087", "192.168.1.71:6001"));

        /*CodisServer codisServer = new CodisServer();
        codisServer.setGroup_id(1);
        codisServer.setAddr("192.168.1.72:6001");
        codisServer.setType("slave");
        System.out.println(promoteToMaster("192.168.1.73:18087", codisServer));*/

        Map<Integer, Map<CodisServer, Boolean>> redisInfo = new HashMap<>();
        Map<CodisServer, Boolean> availableRedisInfo = new HashMap<>();
        CodisServer masterServer = new CodisServer();
        masterServer.setGroup_id(1);
        masterServer.setAddr("192.168.1.71:6001");
        masterServer.setType("master");
        availableRedisInfo.put(masterServer, true); // 改为false可模拟切换操作
        CodisServer slaveServer = new CodisServer();
        slaveServer.setGroup_id(1);
        slaveServer.setAddr("192.168.1.72:6001");
        slaveServer.setType("slave");
        availableRedisInfo.put(slaveServer, true);
        redisInfo.put(1, availableRedisInfo);
        System.out.println(autoPromote("Codis监控", "uat", "192.168.1.73:18087", redisInfo));
    }

}
