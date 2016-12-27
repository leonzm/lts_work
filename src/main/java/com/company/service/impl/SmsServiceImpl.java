package com.company.service.impl;

import com.company.conf.Configuation;
import com.company.service.SmsService;
import com.company.tool.Tool_HttpClient;
import com.company.util.JsonUtil;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon on 2016/12/27.
 * 发送短信服务
 * 调用Dubboi暴露的restful接口
 */
public class SmsServiceImpl implements SmsService {

    private static final Logger LOGGER = Logger.getLogger(SmsServiceImpl.class);

    private static final String SERVICE = "com.pengshu.alpha.rpc_service.SmsService";
    private static final String GROUP = "pengshu";
    private static final Map<String, String> headers = new HashMap<>();
    static {
        headers.put("Content-Type", "application/json;charset=utf-8");
    }

    private Tool_HttpClient httpClient = Tool_HttpClient.getInstance();

    @Override
    public boolean sendOneSmsToOnePhone(String message, String phone) throws UnsupportedEncodingException {
        if (Strings.isNullOrEmpty(message)) {
            throw new NullPointerException("Message cannot null");
        }
        if (Strings.isNullOrEmpty(phone)) {
            throw new NullPointerException("Phone cannot null");
        }
        if (!phone.matches("\\d{11}")) {
            throw new IllegalArgumentException("Phone must a 11 number");
        }

        Map<String, String> params = new HashMap<>();
        params.put("arg0", message);
        params.put("arg1", phone);
        return httpInvoke("sendOneSmsToOnePhone", JsonUtil.toJSON(params));
    }

    @Override
    public boolean sendOneSmsToMorePhone(String message, String[] phones) throws UnsupportedEncodingException {
        if (Strings.isNullOrEmpty(message)) {
            throw new NullPointerException("Message cannot null");
        }
        if (phones == null && phones.length == 0) {
            throw new NullPointerException("Phones cannot null");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("arg0", message);
        params.put("arg1", phones);
        return httpInvoke("sendOneSmsToMorePhone", JsonUtil.toJSON(params));
    }

    @Override
    public boolean sendMoreSmsToOnePhone(String[] messages, String phone) throws UnsupportedEncodingException {
        return false;
    }

    @Override
    public boolean sendMoreSmsToMorePhone(String[] messages, String[] phones) throws UnsupportedEncodingException {
        return false;
    }

    // 通用发送，内部发送，不检查参数
    private boolean httpInvoke(String method, String json) {
        // http://192.168.1.124:9090?service=com.pengshu.alpha.rpc_service.SmsService&method=sendOneSmsToOnePhone&version=V1.00.00R161129&group=pengshu
        String url = "http://".concat(Configuation.SMS_SERVICE_HOSTPORT).concat("?service=").concat(SERVICE)
                .concat("&method=").concat(method).concat("&version=").concat(Configuation.SMS_SERVICE_VERSION)
                .concat("&group=").concat(GROUP);

        boolean isSucc = false;
        try {
            String body = httpClient.do_post_json(url, json, headers);
            if (!Strings.isNullOrEmpty(body) && body.contains("\"data\":true")) {
                isSucc = true;
            }
        } catch (IOException e) {
            LOGGER.warn("调用发送短信服务异常", e);
        }
        return isSucc;
    }

}
