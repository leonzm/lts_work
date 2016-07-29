package com.company.tool;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.company.annotation.SysDatasourceWork;
import com.company.work.LtsWork;
import com.github.ltsopensource.jobclient.JobClient;
import com.github.ltsopensource.jobclient.JobClientBuilder;
import com.github.ltsopensource.tasktracker.TaskTracker;
import com.github.ltsopensource.tasktracker.TaskTrackerBuilder;

/*
 * 注册 TaskTracker
 * 
 */
@SuppressWarnings("rawtypes")
public class Tool_Register {
	
	private static final Logger LOG = Logger.getLogger(Tool_Register.class);
	
	public static final Map<String, TaskTracker> taskTrackers = new HashMap<String, TaskTracker>(); // <datasource, taskTracker>
	public static final Map<String, JobClient> jobClients = new HashMap<String, JobClient>(); // <datasource, jobClient>
	
	/**
	 * 将 workClass 上的 TaskTracker 和 JobClient 进行注册，线程数量默认为64
	 * @param workClass 该任务的class
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void register(Class<? extends LtsWork> workClass) throws InstantiationException, IllegalAccessException {
		register(workClass, null);
	}
	
	/**
	 * 将 workClass 上的 TaskTracker 和 JobClient 进行注册
	 * @param workClass 该任务的class
	 * @param workThreads 该任务的最大线程数量
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void register(Class<? extends LtsWork> workClass, Integer workThreads) throws InstantiationException, IllegalAccessException {
		String datasource = workClass.getAnnotation(SysDatasourceWork.class).datasource();
		
		// 注册 TaskTracker
		final TaskTracker taskTracker = new TaskTrackerBuilder()
        .setPropertiesConfigure("conf/tasktracker/lts.properties")
        .build();
		
		taskTracker.setJobRunnerClass(workClass);
		if (workThreads != null) {
			taskTracker.setWorkThreads(workThreads);
		}
		taskTracker.setNodeGroup(datasource.concat("_TaskTracker")); // datasource_TaskTracker
		
		taskTracker.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				taskTracker.stop();
			}
		}));
		taskTrackers.put(datasource, taskTracker);
		
		// 注册 JobClient
        JobClient jobClient = new JobClientBuilder()
                .setPropertiesConfigure("conf/jobclient/lts.properties")
                .setJobCompletedHandler(workClass.newInstance())
                .build();

        jobClient.setNodeGroup(datasource.concat("_JobClient")); // datasource_JobClient
        jobClient.start();
        jobClients.put(datasource, jobClient);
		
		LOG.info(datasource.concat(" 任务注册成功"));
	}
	
}
