package com.company.model;

import java.util.List;

/**
 * Created by Leon on 2016/12/26.
 */
public class CodisServerGroup {

    private Integer id;
    private String product_name;
    private List<CodisServer> servers;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public List<CodisServer> getServers() {
        return servers;
    }

    public void setServers(List<CodisServer> servers) {
        this.servers = servers;
    }
}
