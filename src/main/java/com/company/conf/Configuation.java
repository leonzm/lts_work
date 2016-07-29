package com.company.conf;

import org.apache.log4j.Logger;

import com.company.tool.Tool_Groovy;
import com.company.tool.Tool_Register;
import com.company.work.LtsWork;

public class Configuation {

	private static final Logger LOG = Logger.getLogger(Configuation.class);
	
	public static final int MAX_THREAD_NUM = 500; // 一台机器上爬虫的最多数量
	
	/*
	 * 项目初始化
	 */
	@SuppressWarnings("unchecked")
	public static void init() {
		try {
			LOG.info("#初始化");
			
			LOG.info("#加载groovy");
			Tool_Groovy.initDatasourceConfig();
			LOG.info("#注册服务");
			for (Class<?> workClass : Tool_Groovy.groovyMapping.values()) {
				Tool_Register.register((Class<? extends LtsWork>) workClass, MAX_THREAD_NUM / Tool_Groovy.groovyMapping.values().size());
			}
		} catch (Exception e) {
			LOG.error("项目初始化异常", e);
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		init();
	}
	
}
