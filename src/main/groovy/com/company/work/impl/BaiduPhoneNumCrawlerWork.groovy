package com.company.work.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.company.annotation.SysDatasourceWork;
import com.company.model.DmMobile;
import com.company.tool.Tool_HttpClient;
import com.company.tool.Tool_Mongo;
import com.company.tool.Tool_Register;
import com.company.work.LtsWork;
import com.github.ltsopensource.core.commons.utils.CollectionUtils;
import com.github.ltsopensource.core.domain.Action;
import com.github.ltsopensource.core.domain.Job;
import com.github.ltsopensource.core.domain.JobResult;
import com.github.ltsopensource.jobclient.domain.Response;
import com.github.ltsopensource.tasktracker.Result;
import com.github.ltsopensource.tasktracker.logger.BizLogger;
import com.github.ltsopensource.tasktracker.runner.JobContext;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@SysDatasourceWork(datasource="baiduphonenumcrawler")
public class BaiduPhoneNumCrawlerWork extends LtsWork {
	
	private static final Logger LOG = Logger.getLogger(BaiduPhoneNumCrawlerWork.class);
	
	private Tool_HttpClient httpClient = new Tool_HttpClient();
	private JsonParser jsonparser = new JsonParser();

	@Override
	public Result run(JobContext jobContext) throws Throwable {
    	
        BizLogger bizLogger = jobContext.getBizLogger();
        try {

            bizLogger.info("开始执行 BaiduPhoneNumCrawlerWork");
            
            String phone = jobContext.getJob().getParam("phone");
            // 检查参数
            if (Strings.isNullOrEmpty(phone)) {
            	return new Result(Action.EXECUTE_FAILED, "参数 phone 不存在");
            } else if (!phone.matches("\\d{7}")) {
            	return new Result(Action.EXECUTE_FAILED, "参数 phone 必须是7位数的号码段");
            }
            
            String returnMsg = httpClient.do_get("https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?query={phone}+%E6%89%8B%E6%9C%BA%E5%8F%B7%E6%AE%B5&co=&resource_id=6004&t={timestamp}&ie=utf8&oe=gbk&cb=op_aladdin_callback&format=json".replace("{phone}", phone).replace("{timestamp}", "" + System.currentTimeMillis()));
            if (!Strings.isNullOrEmpty(returnMsg) && returnMsg.contains("data") && returnMsg.contains("key")) {
            	returnMsg = "{" + returnMsg.split("\\(\\{")[1].replace(");", "");
            	
            	
            	JsonElement jsonElement = jsonparser.parse(returnMsg);
				jsonElement = jsonElement.getAsJsonObject().get("data").getAsJsonArray().get(0).getAsJsonObject();
				
				DmMobile mobile  = new DmMobile();
				mobile.setKey(jsonElement.getAsJsonObject().get("key").getAsString());
				mobile.setProv(jsonElement.getAsJsonObject().get("prov").getAsString());
				mobile.setCity(jsonElement.getAsJsonObject().get("city").getAsString());
				mobile.setType(jsonElement.getAsJsonObject().get("type").getAsString());
				
				Tool_Mongo.get_mongo_collection_dm_mobile().save(mobile);
				
				return new Result(Action.EXECUTE_SUCCESS, phone + " 号码段查询成功");
            }
            
            return new Result(Action.EXECUTE_FAILED, phone + " 号码段不存在");
        } catch (Exception e) {
        	LOG.warn("执行 BaiduPhoneNumCrawlerWork 异常", e);
        	bizLogger.equals("执行 BaiduPhoneNumCrawlerWork 异常" + e.getMessage());
            return new Result(Action.EXECUTE_EXCEPTION, e.getMessage());
        }
	}

	@Override
	public void onComplete(List<JobResult> jobResults) {
		// 任务执行反馈结果处理
        if (CollectionUtils.isNotEmpty(jobResults)) {
            for (JobResult jobResult : jobResults) {
                System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " BaiduPhoneNumCrawlerWork 任务执行完成：" + jobResult);
                
                // 在这次任务成功的前提下，提交新任务
                if (jobResult.isSuccess()) {
                	String phone = jobResult.getJob().getParam("phone");
                	if (phone.matches("\\d{7}") && Integer.parseInt(phone) < 2000000) {
                		phone = (Integer.parseInt(phone) + 1) + "";
                		
                		Job job = new Job();
                        job.setTaskId("baiduphonenumcrawler_realtime_jobclient_" + phone);
                        job.setParam("phone", phone);
                        job.setTaskTrackerNodeGroup("baiduphonenumcrawler_TaskTracker");
                        job.setNeedFeedback(true);
                        job.setReplaceOnExist(true);        // 当任务队列中存在这个任务的时候，是否替换更新
                        job.setMaxRetryTimes(3);
                        Response response = Tool_Register.jobClients.get("baiduphonenumcrawler").submitJob(job);
                        
                        LOG.info("自动提交任务：" + response);
                	}
                }
                
            }
        }
	}
	
}
