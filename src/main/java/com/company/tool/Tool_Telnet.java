package com.company.tool;

import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by Leon on 2016/12/23.
 */
public class Tool_Telnet {

    private static final Logger LOGGER = Logger.getLogger(Tool_Telnet.class);

    /**
     * 使用telnet判断存活
     * @param address
     * @param port
     * @return
     */
    public static boolean ping(String address, int port) {
        boolean isOk = false;
        TelnetClient telnetClient = new TelnetClient();
        try {
            telnetClient.connect(address, port);
            isOk = telnetClient.isConnected();
            telnetClient.disconnect();
        } catch (IOException e) {
            LOGGER.warn("使用telnet判断存活异常", e);
        }
        return isOk;
    }

    public static void main(String[] args) {
        //System.out.println(ping("192.168.1.125", 2181));
    }

}
