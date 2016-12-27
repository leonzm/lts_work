package com.company.conf;

import org.apache.log4j.Logger;

import com.company.tool.Tool_Groovy;
import com.company.tool.Tool_Register;
import com.company.work.LtsWork;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuation {

	private static final Logger LOG = Logger.getLogger(Configuation.class);
	
	public static final int MAX_THREAD_NUM = 500; // 一台机器上爬虫的最多数量

	/**
	 * 加载args配置文件
	 *
	 */
	private static Properties args_properties = new Properties();
	static {
		try (InputStream inputStream = Configuation.class.getResourceAsStream("/conf/args.properties")) {
			args_properties.load(inputStream);
		} catch (IOException e) {}

		LOG.info("#读取 args.properties 配置文件");
	}
	public static final String VERSION = args_properties.getProperty("lts_work.version"); // 系统版本
	public static final String ENVIRONMENT = args_properties.getProperty("lts_work.environment"); // 系统环境
	public static final String SMS_SERVICE_HOSTPORT = args_properties.getProperty("lts_work.sms.service.hostport"); // 短信服务的ip:port
	public static final String SMS_SERVICE_VERSION = args_properties.getProperty("lts_work.sms.service.version"); // 短信服务的version

	/*
	 * 项目初始化
	 */
	@SuppressWarnings("unchecked")
	public static void init() {
		try {
			LOG.info("#初始化");
			LOG.info("#当前版本：".concat(VERSION));
			LOG.info("#当前环境：".concat(ENVIRONMENT));
			LOG.info("#短信服务的ip:port：".concat(SMS_SERVICE_HOSTPORT));
			LOG.info("#短信服务的version：".concat(SMS_SERVICE_VERSION));
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
