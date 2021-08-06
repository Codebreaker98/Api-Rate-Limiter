package com.blueoptima.apirate.Models.DB;

import java.util.List;
import java.util.Map;

public class ApiModel {

    private String apiKey;
    private Map<String, List<Long>> endpoints;
    private String orgId;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Map<String, List<Long>> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map<String, List<Long>> endpoints) {
        this.endpoints = endpoints;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }
}
