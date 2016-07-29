package com.company.work.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.company.annotation.SysDatasourceWork;
import com.company.work.LtsWork;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.JobResult;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.logger.BizLogger;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.google.common.base.Strings;

@SysDatasourceWork(datasource="bye")
public class ByeWork extends LtsWork {
	
	private static final Logger LOG = Logger.getLogger(ByeWork.class);

	@Override
	public Result run(JobContext jobContext) throws Throwable {
        String msg = "";
    	
        try {
            BizLogger bizLogger = jobContext.getBizLogger();

            bizLogger.info("开始执行 ByeWork");
            
            String name = jobContext.getJob().getParam("name");
            if (Strings.isNullOrEmpty(name)) {
            	msg = "ByeWork 回复：再见，无名。";
            } else {
            	msg = "ByeWork 回复：再见，" + name;
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
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ByeWork任务执行完成：" + jobResult);
            }
        }
	}

}
