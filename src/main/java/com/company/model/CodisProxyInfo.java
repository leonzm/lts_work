package com.company.model;

/**
 * Created by Leon on 2016/12/26.
 * Codis 代理节点信息封装
 */
public class CodisProxyInfo {

    private String id;
    private String addr;
    private String last_event;
    private Integer last_event_ts;
    private String state;
    private String description;
    private String debug_var_addr;
    private Integer pid;
    private String start_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getLast_event() {
        return last_event;
    }

    public void setLast_event(String last_event) {
        this.last_event = last_event;
    }

    public Integer getLast_event_ts() {
        return last_event_ts;
    }

    public void setLast_event_ts(Integer last_event_ts) {
        this.last_event_ts = last_event_ts;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDebug_var_addr() {
        return debug_var_addr;
    }

    public void setDebug_var_addr(String debug_var_addr) {
        this.debug_var_addr = debug_var_addr;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getStart_at() {
        return start_at;
    }

    public void setStart_at(String start_at) {
        this.start_at = start_at;
    }

}
