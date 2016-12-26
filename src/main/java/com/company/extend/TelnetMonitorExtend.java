package com.company.extend;

import com.company.tool.Tool_OkHttp;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.net.telnet.TelnetClient;

/**
 * Created by Leon on 2016/12/26.
 */
public class TelnetMonitorExtend {

    public static boolean telnetCheck(String addre) {
        Preconditions.checkNotNull(addre);
        boolean isOk = false;
        try {
            if (addre.contains(":")) {
                TelnetClient telnetClient = new TelnetClient();
                telnetClient.connect(addre.split(":")[0], Integer.parseInt(addre.split(":")[1]));
                if (telnetClient.isConnected()) {
                    isOk = true;
                }
            }
        } catch (Exception e) {}
        return isOk;
    }

    public static void main(String[] args) {
        System.out.println(telnetCheck("192.168.1.71:2181"));
    }

}
