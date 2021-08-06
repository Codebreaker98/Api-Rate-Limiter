package com.blueoptima.apirate.Models.ApiResp;

public class CheckLimitResp {
    public String status;
    public String msg;
    public int statCode;
    public String epStatus;
    public int epStatCode;
    public long callsMade;
    public long apiLimit;
    public long apiWinSec;
    public long limitResetMillis;
}
