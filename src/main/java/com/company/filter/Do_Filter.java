/**
 * 
 */
package com.company.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class Do_Filter implements Filter {
	
	private static final Logger LOG = Logger.getLogger(Do_Filter.class);

	private String encoding = "UTF-8";
	private String allow = "*";

	/**
	 * 读取配置文件，初始化参数
	 * 
	 * @param filterConfig
	 * @exception ServletException
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		// 本过滤器默认编码是UTF-8，但也可以在web.xml配置文件里设置自己需要的编码
		if (filterConfig.getInitParameter("encoding") != null) {
			encoding = filterConfig.getInitParameter("encoding");
		}
		if (filterConfig.getInitParameter("allow") != null) {
			allow = filterConfig.getInitParameter("allow");
		}
	}

	/**
	 * 过滤时设置编码、跨域访问和处理未关闭的session
	 * 
	 * @param srequset
	 * @param sresponse
	 * @param filterChain
	 * @exception IOException
	 */
	public void doFilter(ServletRequest srequset, ServletResponse sresponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequset;
		HttpServletResponse response = (HttpServletResponse) sresponse;
		//
		request.setCharacterEncoding(encoding);
		// 必须否则前台会收到乱码
		response.setContentType("text/html;charset=".concat(encoding));
		// 指明当前页面支持跨越访问
		response.setHeader("Access-Control-Allow-Origin", allow);

		try {
			filterChain.doFilter(srequset, sresponse);
		} catch (Exception e) {
			LOG.error("过滤异常", e);
			// TODO 发邮件
		}
	}

	public void destroy() {
		this.encoding = null;
	}
}