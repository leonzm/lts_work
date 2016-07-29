package com.company.tool;

import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.company.annotation.SysDatasourceWork;

/**
 * Groovy 加载工具类
 *
 */
public class Tool_Groovy {
	
	private static final Logger LOG = Logger.getLogger(Tool_Groovy.class);
	
	private static GroovyClassLoader groovyClassLoader = null;
	// 解析类 映射map < 数据源key , 数据源 解析类>
	public static final Map<String, Class<?>> groovyMapping = new HashMap<>();
	
	public static final String PATH_CLASSES = File.separatorChar + "classes";
	public static String WORK_PATH = Tool_Path.get("src/main/groovy/com/company/work/impl").toString().replace(PATH_CLASSES, "");
	
	// 重新部署的历史记录
	public static final List<History> historys = new LinkedList<History>();
	// 加载指定 groovy 时，使用该变量记录上次加载的 groovy 文件的最后修改时间
	private static Timestamp last_modify_temp = null;
	
	/**
	 * 初始化GroovyClassLoader，使用 UTF-8 加载 groovy 脚本，加载所有的groovy解析脚本
	 *
	 */
	public static void initDatasourceConfig() {
		CompilerConfiguration config = new CompilerConfiguration();
		config.setSourceEncoding("UTF-8");
		groovyClassLoader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(), config);
		initGroovyMapping("lts_work", "项目启动");
	}

	/**
	 * 加载所有Groovy类
	 *
	 * @throws IOException
	 * @throws CompilationFailedException
	 *
	 */
	public static int initGroovyMapping(String userName, String msg) {
		int count = 0;
		groovyClassLoader.clearCache();
		History history = History.getInstance(userName, msg);

		String groovy_class_directory_path = null;
		GroovyFileFilter groovyFileFilter = new GroovyFileFilter();
		Class<?> groovyClass = null;

		// work.impl
		groovy_class_directory_path = WORK_PATH;
		File groovy_class_directory = new File(groovy_class_directory_path);
		File[] groovy_files = groovy_class_directory.listFiles(groovyFileFilter);
		if (groovy_files != null && groovy_files.length > 0) {
			for (File groovy_file : groovy_files) {
				try {
					groovyClass = groovyClassLoader.parseClass(groovy_file);
					if (groovyClass != null && groovyClass.getAnnotation(SysDatasourceWork.class) != null) {
						String datasource = groovyClass.getAnnotation(SysDatasourceWork.class).datasource();
						groovyMapping.put(datasource, groovyClass);

						count ++;
						history.addHistory(datasource, true, new Timestamp(groovy_file.lastModified()), History.Load_msg.SUCCESS.name());
						LOG.info("加载 " + datasource + " groovy 解析成功，修改日期：" + (new Timestamp(groovy_file.lastModified()).toString()));
					}
				} catch (Exception e) {
					history.addHistory(groovy_file.getName(), false, new Timestamp(groovy_file.lastModified()), History.Load_msg.FAILE.name().concat(":").concat(e.getMessage()));
					LOG.warn("加载 " + groovy_file.getName() + " groovy 解析失败: " + e.getMessage());
				}
			}
		}

		historys.add(history);
		LOG.info("共加载了 " + count + "个  groovy 解析脚本");
		return count;
	}

	/**
	 * 加载Groovy类
	 * @param userName
	 * @param msg
	 * @param datasources 要加载 groovy类对应的datasource
	 * @return
	 */
	public static int initGroovyMapping(String userName, String msg, List<String> datasources) {
		int count = 0;
		groovyClassLoader.clearCache();
		History history = History.getInstance(userName, msg);

		if (datasources != null && datasources.size() > 0) {
			for (String datasource : datasources) {
				try {
					Class<?> groovyClass = getGroovyClass(datasource);
					if (groovyClass != null) {
						groovyMapping.put(datasource, groovyClass);

						count ++;
						history.addHistory(datasource, true, last_modify_temp, History.Load_msg.SUCCESS.name());
						LOG.info("加载 " + datasource + " groovy 解析成功，修改日期：" + last_modify_temp.toString());
					} else {
						history.addHistory(datasource, false, null, History.Load_msg.FAILE.name().concat(": 该 datasource 对应的 Groovy  没有找到"));
						LOG.warn("加载 " + datasource + " groovy 解析失败");
					}
				} catch (Exception e) {
					history.addHistory(datasource, false, null, History.Load_msg.FAILE.name().concat(":").concat(e.getMessage()));
					LOG.warn("加载 " + datasource + " groovy 解析失败: " + e.getMessage());
				}
			}
		}

		historys.add(history);
		LOG.info("共加载了 " + count + "个  groovy 解析脚本");
		return count;
	}

	/**
	 * 根据 datasource 查找对应的  class，如果没有则返回 null
	 *
	 * @param datasource
	 *
	 * @return  class
	 * @throws IOException
	 * @throws CompilationFailedException
	 */
	public static Class<?> getGroovyClass(String datasource) {
		Class<?> groovyClass = null;

		if (datasource == null || datasource.equals("")) return null;

		String groovy_class_directory_path = null;

		GroovyFileFilter groovyFileFilter = new GroovyFileFilter();

		// 根据datasource判断该groovy在哪个文件夹中
		groovy_class_directory_path = WORK_PATH;
		
		//todo
		File groovy_class_directory = new File(groovy_class_directory_path);
		File[] groovy_files = groovy_class_directory.listFiles(groovyFileFilter);

		for (File groovy_file : groovy_files) {
			try {
				groovyClass = groovyClassLoader.parseClass(groovy_file); // 需要加tryCatch，防止加载B时，因A有问题导致无法加载到B
				if (groovyClass != null && groovyClass.getAnnotation(SysDatasourceWork.class) != null) {
					if (datasource.equals(groovyClass.getAnnotation(SysDatasourceWork.class).datasource())) {
						last_modify_temp = new Timestamp(groovy_file.lastModified());
						return groovyClass;
					}
				}
			} catch (CompilationFailedException | IOException e) {
				LOG.warn("热加载 " + groovy_file.getName() + " groovy 解析失败: " + e.getMessage());
			}
		}

		return null;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		Tool_Groovy.initDatasourceConfig();
		Tool_Groovy.initGroovyMapping("zhangsan", "test");
		Tool_Groovy.initGroovyMapping("zhangsan", "test", Arrays.asList("hello", "bye"));
	}

}

class GroovyFileFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.getName().endsWith(".groovy")) {
			return true;
		} else {
			return false;
		}
	}

}

class History {
	public enum Load_msg {
		SUCCESS, FAILE
	}

	private String name; //姓名
	private String msg; // 重新部署 groovy 解析的原因
	private List<Datasource> datasources = new LinkedList<Datasource>(); // 此次重新加载的datasource的信息
	private Timestamp update_time; // 重新部署时间

	private History(){}

	public static History getInstance(String name, String msg) {
		History history = new History();
		history.setName(name);
		history.setMsg(msg);
		history.setUpdate_time(new Timestamp(System.currentTimeMillis()));
		return history;
	}

	public void addHistory(String name, boolean is_success, Timestamp last_modified, String msg) {
		datasources.add(new Datasource(name, is_success, last_modified, msg));
	}

	public class Datasource {
		private String name; // datasource name
		private boolean is_success; // 是否加载成功
		private Timestamp last_modified; // 该 groovy 的最后一次修改时间
		private String msg; // 加载成功、失败的错误信息

		public Datasource(String name, boolean is_success, Timestamp last_modified, String msg) {
			this.name = name;
			this.is_success = is_success;
			this.last_modified = last_modified;
			this.msg = msg;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean isIs_success() {
			return is_success;
		}
		public void setIs_success(boolean is_success) {
			this.is_success = is_success;
		}
		public Timestamp getLast_modified() {
			return last_modified;
		}
		public void setLast_modified(Timestamp last_modified) {
			this.last_modified = last_modified;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public List<Datasource> getDatasources() {
		return datasources;
	}

	public void setDatasources(List<Datasource> datasources) {
		this.datasources = datasources;
	}

	public Timestamp getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(Timestamp update_time) {
		this.update_time = update_time;
	}

}