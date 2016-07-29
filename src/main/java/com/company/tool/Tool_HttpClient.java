package com.company.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Http 请求工具类
 *
 */
public class Tool_HttpClient {
	
	private CloseableHttpClient httpclient = HttpClients.createDefault();

	/**
	 * get 方式请求
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public String do_get(String url) throws IOException {
		String result = null;
		
		try {
			// 创建httpget.
			HttpGet httpget = new HttpGet(url);
			// 执行get请求.
			CloseableHttpResponse response = httpclient.execute(httpget);
			try {
				// 获取响应实体
				HttpEntity entity = response.getEntity();
				// 打印响应状态
				System.out.println(response.getStatusLine());
				if (entity != null) {
					result = EntityUtils.toString(entity, "UTF-8");
				}
			} finally {
				response.close();
			}
		} catch (IOException e) {
			throw e;
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {}
		}
		
		return result;
	}
	
	/**
	 * post 方式请求
	 * @param url
	 * @param param
	 * @return
	 * @throws IOException
	 */
	public String do_post(String url, Map<String, String> param) throws IOException {
		String result = null;
		// 创建httppost
		HttpPost httppost = new HttpPost(url);
		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (param != null) {
			param.forEach((key, value) ->{
				formparams.add(new BasicNameValuePair(key, value));
			});
		}
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					result = EntityUtils.toString(entity, "UTF-8");
				}
			} finally {
				response.close();
			}
		} catch (IOException e) {
			throw e;
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {}
		}
		
		return result;
	}
	
	public static void main(String[] args) throws IOException {
		Tool_HttpClient httpClient = new Tool_HttpClient();
		
		//System.out.println(httpClient.do_get("http://sowm.cn/importnew/article/C781FF9364AEA7EC9D599641D3C0E37F.html"));
		System.out.println(httpClient.do_post("http://sowm.cn/importnew/article/C781FF9364AEA7EC9D599641D3C0E37F.html", null));
	}

}