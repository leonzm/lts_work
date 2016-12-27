package com.company.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Http 请求工具类
 *
 */
public class Tool_HttpClient {

	// 当使用长轮循时需要注意不能超过此时间
	private static Integer socket_timeout = 10_000;// 数据传输时间
	private static Integer connect_timeout = 10_000;// 连接时间
	private CloseableHttpClient httpClient;
	private RequestConfig requestConfig = RequestConfig.custom()
			.setConnectTimeout(connect_timeout)
			.setSocketTimeout(socket_timeout)
			.build();

	private Tool_HttpClient() {
		httpClient = HttpClients.createDefault();
	}

	private Tool_HttpClient(String proxyHost, int proxyPort) {
		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
		httpClient = HttpClients.custom().setRoutePlanner(routePlanner).build();
	}

	public static Tool_HttpClient getInstance() {
		return new Tool_HttpClient();
	}

	public static Tool_HttpClient getProxyInstance(String ip, int port) {
		return new Tool_HttpClient(ip, port);
	}

	private static String concat_params(Map<String, String> params) {
		if (params == null) {
			params = new HashMap<>();
		}
		StringBuilder do_get_sbf = new StringBuilder();
		String[] keys = new String[params.size()];
		params.keySet().toArray(keys);
		for (int j = 0; j < keys.length; j++) {
			if (j != 0) {
				do_get_sbf.append("&");
			}
			String key = keys[j];
			String value = params.get(key);
			do_get_sbf.append(key).append("=").append(value);
		}
		return do_get_sbf.toString();
	}

	///////////////////////////////// get 请求  /////////////////////////////////

	public String do_get(String url, Map<String, String> params) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url.concat("?").concat(concat_params(params)));
		httpGet.setConfig(requestConfig);

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public String do_get(String url, Map<String, String> params, Map<String, String> headers) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url.concat("?").concat(concat_params(params)));
		httpGet.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			headers.forEach((name, value) -> {
				httpGet.addHeader(name, value);
			});
		}

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public HTML do_get_for_all(String url, Map<String, String> params, Map<String, String> headers) throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url.concat("?").concat(concat_params(params)));
		httpGet.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			headers.forEach((name, value) -> {
				httpGet.addHeader(name, value);
			});
		}

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);

			HTML html = new HTML();
			Header[] allHeaders = response.getAllHeaders();
			Map<String, String> headesMap = new HashMap<String, String>();
			for (Header h : allHeaders) {
				headesMap.put(h.getName(), h.getValue());
			}
			html.setHeaders(headesMap);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html.setBody(EntityUtils.toString(entity));
			}

			return html;
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	///////////////////////////////// post 请求  /////////////////////////////////

	public String do_post(String url, Map<String, String> params) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);

		if (params != null && params.size() > 0) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			params.forEach((k, v) -> {
				formparams.add(new BasicNameValuePair(k, v));
			});
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
			httpPost.setEntity(entity);
		}

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public String do_post(String url, Map<String, String> params, Map<String, String> headers) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			headers.forEach((name, value) -> {
				httpPost.addHeader(name, value);
			});
		}

		if (params != null && params.size() > 0) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			params.forEach((k, v) -> {
				formparams.add(new BasicNameValuePair(k, v));
			});
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
			httpPost.setEntity(entity);
		}

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public HTML do_post_for_all(String url, Map<String, String> params, Map<String, String> headers) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			headers.forEach((name, value) -> {
				httpPost.addHeader(name, value);
			});
		}

		if (params != null && params.size() > 0) {
			List<NameValuePair> formparams = new ArrayList<NameValuePair>();
			params.forEach((k, v) -> {
				formparams.add(new BasicNameValuePair(k, v));
			});
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
			httpPost.setEntity(entity);
		}

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);

			HTML html = new HTML();
			Header[] allHeaders = response.getAllHeaders();
			Map<String, String> headesMap = new HashMap<String, String>();
			for (Header h : allHeaders) {
				headesMap.put(h.getName(), h.getValue());
			}
			html.setHeaders(headesMap);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html.setBody(EntityUtils.toString(entity));
			}

			return html;
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	///////////////////////////////// post json 请求  /////////////////////////////////

	public String do_post_json(String url, String json) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);

		StringEntity s = new StringEntity(json);
		s.setContentEncoding("UTF-8");
		s.setContentType("application/json");
		httpPost.setEntity(s);

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public String do_post_json(String url, String json, Map<String, String> headers) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			headers.forEach((name, value) -> {
				httpPost.addHeader(name, value);
			});
		}

		StringEntity s = new StringEntity(json);
		s.setContentEncoding("UTF-8");
		s.setContentType("application/json");
		httpPost.setEntity(s);

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toString(entity);
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public HTML do_post_json_for_all(String url, String json, Map<String, String> headers) throws ClientProtocolException, IOException {
		HttpPost httpPost = new HttpPost(url);
		httpPost.setConfig(requestConfig);
		if (headers != null && headers.size() > 0) {
			headers.forEach((name, value) -> {
				httpPost.addHeader(name, value);
			});
		}

		StringEntity s = new StringEntity(json);
		s.setContentEncoding("UTF-8");
		s.setContentType("application/json");
		httpPost.setEntity(s);

		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);

			HTML html = new HTML();
			Header[] allHeaders = response.getAllHeaders();
			Map<String, String> headesMap = new HashMap<String, String>();
			for (Header h : allHeaders) {
				headesMap.put(h.getName(), h.getValue());
			}
			html.setHeaders(headesMap);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				html.setBody(EntityUtils.toString(entity));
			}

			return html;
		} catch (ClientProtocolException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	////////////////////// HTML ///////////////////////

	public static class HTML {

		private Map<String, String> headers;
		private String body;
		public Map<String, String> getHeaders() {
			return headers;
		}
		public void setHeaders(Map<String, String> headers) {
			this.headers = headers;
		}
		public String getBody() {
			return body;
		}
		public void setBody(String body) {
			this.body = body;
		}

	}

}