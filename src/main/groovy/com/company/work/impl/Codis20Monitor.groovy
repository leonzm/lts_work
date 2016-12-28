package com.company.work.impl

import com.company.annotation.SysDatasourceWork
import com.company.extend.Codis20MonitorExtend
import com.company.extend.HttpMonitorExtend
import com.company.model.CodisProxyInfo
import com.company.model.CodisServer
import com.company.service.SmsService
import com.company.service.impl.SmsServiceImpl
import com.company.tool.Tool_Email
import com.company.work.LtsWork
import com.github.ltsopensource.core.commons.utils.CollectionUtils
import com.github.ltsopensource.core.domain.Action
import com.github.ltsopensource.core.domain.JobResult
import com.github.ltsopensource.tasktracker.Result
import com.github.ltsopensource.tasktracker.logger.BizLogger
import com.github.ltsopensource.tasktracker.runner.JobContext
import com.google.common.base.Strings
import org.apache.log4j.Logger

import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
 * Created by Leon on 2016/12/26.
 * Codis 2.0 监控 <br/>
 * 参数:  <br/>
 * name 监控命名 <br/>
 * environment 环境 <br/>
 * dashboard dashboard地址，格式: host:port <br/>
 * proxy 代理节点地址，格式: host:port，多个proxy的话，用逗号隔开 <br/>
 * auto 是否开启Redis的主备自动切换，"true"表示自动 <br/>
 */
@SysDatasourceWork(datasource="codis2.0Monitor")
public class Codis20Monitor extends LtsWork {

    private static final Logger LOG = Logger.getLogger(Codis20Monitor.class);

    private static Map<String, Integer> instanceInfo = new HashMap<>(); // <address, 0>, 记录挂掉的实例

    private SmsService smsService = new SmsServiceImpl();

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        String msg = "";

        try {
            BizLogger bizLogger = jobContext.getBizLogger();

            bizLogger.info("开始执行 Codis20Monitor");

            // 获取并检查参数
            String name = jobContext.getJob().getParam("name");
            if (Strings.isNullOrEmpty(name)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数name不能为空");
            }
            String environment = jobContext.getJob().getParam("environment");
            if (Strings.isNullOrEmpty(environment)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数environment不能为空");
            }
            String dashboard = jobContext.getJob().getParam("dashboard"); // host:port
            if (Strings.isNullOrEmpty(environment)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数dashboard不能为空");
            }
            String proxy = jobContext.getJob().getParam("proxy"); // 为空的话不检查proxy
            String auto = jobContext.getJob().getParam("auto");
            if (Strings.isNullOrEmpty(environment)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数auto不能为空");
            }

            // dashborad不能挂，否则无法继续
            if (!HttpMonitorExtend.httpCheck("http://" + dashboard + "/admin/")) {
                if (!instanceInfo.containsKey(dashboard)) { // 第一次发现该实例挂了，发出报警；非第一次不发出报警
                    instanceInfo.put(dashboard, 0);

                    Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控: Dashboard timeout", "name: ".concat(name)
                            .concat("\r\nenvironment: ").concat(environment)
                            .concat("\r\ndashboard: ").concat(dashboard)
                            .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                    smsService.sendOneSmsToOnePhone("Cods Dashboard timeout: " + dashboard, "15001848348");
                    LOG.info("Cods Dashboard timeout: " + dashboard);
                }
                return new Result(Action.EXECUTE_FAILED, "Cods Dashboard timeout: " + dashboard);
            } else { // 该实例正常
                if (instanceInfo.containsKey(dashboard)) { // 发现之前挂掉的实例恢复
                    instanceInfo.remove(dashboard);

                    Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控: Dashboard return to normal", "name: ".concat(name)
                            .concat("\r\nenvironment: ").concat(environment)
                            .concat("\r\ndashboard: ").concat(dashboard)
                            .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                    smsService.sendOneSmsToOnePhone("Cods Dashboard return to normal: " + dashboard, "15001848348");
                    LOG.info("Cods Dashboard return to normal: " + dashboard);
                }
            }

            // 检查proxy，如果proxy不为空，则表示需要检查
            Map<String, CodisProxyInfo> proxyInfoMap = Codis20MonitorExtend.loadOnlineProxy(dashboard);
            if (!Strings.isNullOrEmpty(proxy)) { // host1:port1,host2:port2
                for (String prox : proxy.split(",")) {
                    if (!proxyInfoMap.containsKey(prox)) {
                        if (!instanceInfo.containsKey(prox)) { // 第一次发现该实例挂了，发出报警；非第一次不发出报警
                            instanceInfo.put(prox, 0);

                            Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控: Proxy timeout", "name: ".concat(name)
                                    .concat("\r\nenvironment: ").concat(environment)
                                    .concat("\r\nproxy: ").concat(prox)
                                    .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                            smsService.sendOneSmsToOnePhone("Cods Proxy timeout: " + prox, "15001848348");
                            LOG.info("Cods Proxy timeout: " + prox);
                        }
                    } else { // 该实例正常
                        if (instanceInfo.containsKey(prox)) { // 发现之前挂掉的实例恢复
                            instanceInfo.remove(prox);

                            Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控: Proxy return to normal", "name: ".concat(name)
                                    .concat("\r\nenvironment: ").concat(environment)
                                    .concat("\r\nproxy: ").concat(prox)
                                    .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                            smsService.sendOneSmsToOnePhone("Cods Proxy return to normal: " + prox, "15001848348");
                            LOG.info("Cods Proxy return to normal: " + prox);
                        }
                    }
                }
            }

            // 检查redis实例
            Map<Integer, Map<CodisServer, Boolean>> redisInfo = new HashMap<>(); // <group_id, <codisServer, available>>，记录各group中redis的存活情况，便于主备切换
            List<CodisServer> codisServers = Codis20MonitorExtend.loadRedisServer(dashboard);
            for (CodisServer codisServer : codisServers) {
                if (!redisInfo.containsKey(codisServer.getGroup_id())) {
                    redisInfo.put(codisServer.getGroup_id(), new HashMap<>());
                }

                if (!Codis20MonitorExtend.availableRedisServer(dashboard, codisServer.getAddr())) {
                    if (!instanceInfo.containsKey(codisServer.getAddr())) { // 第一次发现该实例挂了，发出报警；非第一次不发出报警
                        instanceInfo.put(codisServer.getAddr(), 0);

                        redisInfo.get(codisServer.getGroup_id()).put(codisServer, false);
                        Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控: Redis timeout", "name: ".concat(name)
                                .concat("\r\nenvironment: ").concat(environment)
                                .concat("\r\nredis: ").concat(codisServer.getAddr())
                                .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                        smsService.sendOneSmsToOnePhone("Cods Redis timeout: " + codisServer.getAddr(), "15001848348");
                        LOG.info("Cods Redis timeout: " + codisServer.getAddr());
                    }
                } else { // 该实例正常
                    if (instanceInfo.containsKey(codisServer.getAddr())) { // 发现之前挂掉的实例恢复
                        instanceInfo.remove(codisServer.getAddr());

                        redisInfo.get(codisServer.getGroup_id()).put(codisServer, false);
                        Tool_Email.sendEmail("zhaoman@daihoubang.com", "Codis2.0 监控: Redis return to normal", "name: ".concat(name)
                                .concat("\r\nenvironment: ").concat(environment)
                                .concat("\r\nredis: ").concat(codisServer.getAddr())
                                .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                        smsService.sendOneSmsToOnePhone("Cods Redis return to normal: " + codisServer.getAddr(), "15001848348");
                        LOG.info("Cods Redis return to normal: " + codisServer.getAddr());
                    }

                    redisInfo.get(codisServer.getGroup_id()).put(codisServer, true);
                }
            }

            // 主备切换
            if (auto.equals("true")){
                Codis20MonitorExtend.autoPromote(name, environment, dashboard, redisInfo);
            }


        } catch (Exception e) {
            LOG.warn("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }

        return new Result(Action.EXECUTE_SUCCESS, msg);
    }

    @Override
    public void onComplete(List<JobResult> jobResults) {
        // 任务执行反馈结果处理
        if (CollectionUtils.isNotEmpty(jobResults)) {
            for (JobResult jobResult : jobResults) {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " Codis 2.0 Monitor: " + jobResult);
            }
        }
    }

}