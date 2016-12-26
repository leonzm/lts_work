package com.company.extend;

import com.company.tool.Tool_OkHttp;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Created by Leon on 2016/12/26.
 */
public class HttpMonitorExtend {

    public static boolean httpCheck(String addre) {
        Preconditions.checkNotNull(addre);
        boolean isOk = false;
        try {
            String result = Tool_OkHttp.do_get(addre, null, null);
            if (!Strings.isNullOrEmpty(result)) {
                isOk = true;
            }
        } catch (Exception e) {}
        return isOk;
    }

    public static void main(String[] args) {
        System.out.println(httpCheck("http://192.168.1.71:11000/debug/vars"));
    }

}
