package com.blueoptima.apirate.Models;

import static com.blueoptima.apirate.Constants.DEFAULT_ALLOWED_CALL_QUANTUM;
import static com.blueoptima.apirate.Constants.DEFAULT_API_CALL_LIMIT;

public class EndpointModel {

    private String orgId;
    private String apiKey;
    private long apiMaxLimitPerWindow = DEFAULT_API_CALL_LIMIT;
    private long apiTimeWindowInSec = DEFAULT_ALLOWED_CALL_QUANTUM;
    private String endpoint;
    private String orgName;

//    public EndpointModel() {
//
//    }

    public EndpointModel(String orgId, String apiKey, long apiMaxLimitPerWindow, long apiTimeWindowInSec, String endpoint) {
        this.orgId = orgId;
        this.apiKey = apiKey;

        // if 0 is passed, sets to default
        this.apiMaxLimitPerWindow = apiMaxLimitPerWindow <= 0 ? DEFAULT_API_CALL_LIMIT / 1000 : apiMaxLimitPerWindow;
        this.apiTimeWindowInSec = apiTimeWindowInSec <= 0 ? DEFAULT_ALLOWED_CALL_QUANTUM / 1000 : apiTimeWindowInSec;
        this.endpoint = endpoint;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public long getApiMaxLimitPerWindow() {
        return apiMaxLimitPerWindow;
    }

    public void setApiMaxLimitPerWindow(long apiMaxLimitPerWindow) {
        this.apiMaxLimitPerWindow = apiMaxLimitPerWindow;
    }

    public long getApiTimeWindowInSec() {
        return apiTimeWindowInSec;
    }

    public void setApiTimeWindowInSec(long apiTimeWindowInSec) {
        this.apiTimeWindowInSec = apiTimeWindowInSec;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
