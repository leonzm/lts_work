package com.company.work.impl

import com.company.annotation.SysDatasourceWork
import com.company.extend.HttpMonitorExtend
import com.company.extend.TelnetMonitorExtend
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
 * Telnet监控 <br/>
 * name 监控命名 <br/>
 * environment 环境 <br/>
 * address 监控host:port，多个用逗号分隔 <br/>
 */
@SysDatasourceWork(datasource="telnetMonitor")
public class TelnetMonitor extends LtsWork {

    private static final Logger LOG = Logger.getLogger(HiWork.class);

    @Override
    public Result run(JobContext jobContext) throws Throwable {
        try {
            BizLogger bizLogger = jobContext.getBizLogger();

            bizLogger.info("开始执行 TelnetMonitor");

            // 获取并检查参数
            String name = jobContext.getJob().getParam("name");
            if (Strings.isNullOrEmpty(name)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数name不能为空");
            }
            String environment = jobContext.getJob().getParam("environment");
            if (Strings.isNullOrEmpty(environment)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数environment不能为空");
            }
            String address = jobContext.getJob().getParam("address");
            if (Strings.isNullOrEmpty(address)) {
                return new Result(Action.EXECUTE_SUCCESS, "参数address不能为空");
            }
            String[] addresses = address.split(",");
            for (String addr : addresses) { // 逐个检查
                if (!TelnetMonitorExtend.telnetCheck(addr)) {
                    Tool_Email.sendEmail("zhaoman@daihoubang.com", "Telnet监控timeout", "name: ".concat(name)
                            .concat("\r\nenvironment: ").concat(environment)
                            .concat("\r\naddress: ").concat(addr)
                            .concat("\r\ntime: " + new Timestamp(System.currentTimeMillis())));
                    bizLogger.info(addr + "，telnet timeout, had send email.");
                }
            }

        } catch (Exception e) {
            LOG.warn("Run job failed!", e);
            return new Result(Action.EXECUTE_FAILED, e.getMessage());
        }

        return new Result(Action.EXECUTE_SUCCESS, "成功");
    }

    @Override
    public void onComplete(List<JobResult> jobResults) {
        // 任务执行反馈结果处理
        if (CollectionUtils.isNotEmpty(jobResults)) {
            for (JobResult jobResult : jobResults) {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " TelnetMonitor tash fished：" + jobResult);
            }
        }
    }

}