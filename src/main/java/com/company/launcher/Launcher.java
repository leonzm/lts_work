package com.company.launcher;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import com.company.conf.Configuation;


public class Launcher {

	//private static final Logger LOG = Logger.getLogger(Launcher.class);
	
	public static final int PORT = 7070;
	public static final String CONTEXT = "/";
	private static final String DEFAULT_WEBAPP_PATH = "src/main/webapp";
	private static final String DESCRIPTOR = "src/main/webapp/WEB-INF/web.xml";
	
	/**
	 * 创建用于开发运行调试的Jetty Server, 以src/main/webapp为Web应用目录.
	 */
	public static Server createServerInSource(int port, String context, String default_webapp_path, String descriptor) {
		Server server = new Server();
		// 设置在JVM退出时关闭Jetty的钩子。
		server.setStopAtShutdown(true);

		// 这是http的连接器
		ServerConnector connector = new ServerConnector(server);
		connector.setPort(port);
		// 解决Windows下重复启动Jetty居然不报告端口冲突的问题.
		connector.setReuseAddress(false);
		server.setConnectors(new Connector[] { connector });

		WebAppContext webContext = new WebAppContext(default_webapp_path, context);
		// webContext.setContextPath("/");
		webContext.setDescriptor(descriptor);
		// 设置webapp的位置
		webContext.setResourceBase(default_webapp_path);
		webContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		server.setHandler(webContext);
		return server;
	}

	/**
	 * 启动jetty服务
	 */
	public void startJetty() {
		final Server server = Launcher.createServerInSource(PORT, CONTEXT, DEFAULT_WEBAPP_PATH, DESCRIPTOR);
		try {
			server.stop();
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String[] args) {
		Configuation.init();
		new Launcher().startJetty();
	}
	
	// 启动 jobtracker 专用
	public static void main2(String[] args) {
		com.company.jobtracker.MyJobTracker.registerJobTracker();
	}
	
}
