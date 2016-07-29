package com.company.jobtracker;

import com.github.ltsopensource.jobtracker.JobTracker;
import com.github.ltsopensource.jobtracker.JobTrackerBuilder;

/**
 * @author Robert HG (254963746@qq.com) on 4/17/16.
 */
public class MyJobTracker {
	
	public static void registerJobTracker() {
//      final JobTracker jobTracker = new JobTracker();
//      // 节点信息配置
//      jobTracker.setRegistryAddress("zookeeper://10.255.253.30:2181,10.255.253.31:2181,10.255.253.32:2181");
////      jobTracker.setRegistryAddress("redis://127.0.0.1:6379");
//      jobTracker.setListenPort(35001); // 默认 35001
//      jobTracker.setClusterName("LeonCluster");
//
////      // 设置业务日志记录 mysql
////      jobTracker.addConfig("job.logger", "mysql");
////      // 任务队列用mysql
////      jobTracker.addConfig("job.queue", "mysql");
//      // mysql 配置
//      jobTracker.addConfig("jdbc.url", "jdbc:mysql://192.168.101.227:3306/lts?useUnicode=true&characterEncoding=UTF-8");
//      jobTracker.addConfig("jdbc.username", "root");
//      jobTracker.addConfig("jdbc.password", "");

      final JobTracker jobTracker = new JobTrackerBuilder()
              .setPropertiesConfigure("conf/jobtracker/lts.properties")
              .build();

      // 启动节点
      jobTracker.start();

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
          @Override
          public void run() {
              jobTracker.stop();
          }
      }));
	}

    public static void main(String[] args) {
    	registerJobTracker();
    }

}
