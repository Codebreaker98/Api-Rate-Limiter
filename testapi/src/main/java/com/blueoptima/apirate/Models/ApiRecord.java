package com.blueoptima.apirate.Models;

import com.blueoptima.apirate.CM;
import com.blueoptima.apirate.Constants;

public class ApiRecord {

    private long maxLim = Constants.DEFAULT_API_CALL_LIMIT;

    /**
     * API Call Rate per time quantum in milliseconds
     */
    private long apiCallQuantum = Constants.DEFAULT_ALLOWED_CALL_QUANTUM;

    private long lastDbRefresh;
    private long callWindowStart;
    private int callCount;

    public ApiRecord(long maxLim, long apiCallQuantum) {
        this.maxLim = maxLim;
        this.apiCallQuantum = CM.secToMillis(apiCallQuantum);
        this.lastDbRefresh = System.currentTimeMillis();
        this.callWindowStart = 0L;
        this.callCount = 0;
    }

    public long getMaxLim() {
        return maxLim;
    }

    public void setMaxLim(long maxLim) {
        this.maxLim = maxLim;
    }

    public long getApiCallQuantum() {
        return apiCallQuantum;
    }

    public void setApiCallQuantum(long apiCallQuantum) {
        this.apiCallQuantum = apiCallQuantum;
    }

    public long getLastDbRefresh() {
        return lastDbRefresh;
    }

    public void setLastDbRefresh(long lastDbRefresh) {
        this.lastDbRefresh = lastDbRefresh;
    }

    public long getCallWindowStart() {
        return callWindowStart;
    }

    public void setCallWindowStart(long callWindowStart) {
        this.callWindowStart = callWindowStart;
    }

    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }
}
