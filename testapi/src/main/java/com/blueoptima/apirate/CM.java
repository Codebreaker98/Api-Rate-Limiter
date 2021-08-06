  package com.blueoptima.apirate;

import com.blueoptima.apirate.Models.ApiRecord;
import com.blueoptima.apirate.Models.ApiResp.CheckLimitResp;
import com.blueoptima.apirate.Models.ApiResp.NewOrgResp;
import com.blueoptima.apirate.Models.ApiResp.SetEndpointResp;
import com.blueoptima.apirate.Models.EndpointModel;
import com.blueoptima.apirate.Models.OrgModel;

import java.util.HashMap;
import java.util.Map;

import static com.blueoptima.apirate.Constants.*;

/**
 * Common Methods class
 */
public class CM {

    public static long secToMillis(long sec) {
        return sec * 1000;
    }

    /**
     * Creates a API Response for {@code "/addOrUpdateEndpoint"} and
     * {@code "/deleteEndpoint"} endpoint in the below format
     * <pre>
     *
     * {
     *     "status" : "success",
     *     "statCode":  400,
     *     "epName" : "endpoint_name",
     *     "apiLimit" : "100",
     *     "apiTimeWin" : "60",
     *     "msg" : "created",
     * }
     * </pre>
     *
     * @param ep      pass the {@link EndpointModel} object to print else {@code null}
     * @param msg     pass the message to be displayed in API Response
     * @param success pass {@code true} on success else {@code false}
     * @return returns a formatted JSON API Response
     */
    public static SetEndpointResp getApiResp(EndpointModel ep, String msg, boolean success) {

        SetEndpointResp res = new SetEndpointResp();

        res.msg = msg;
        res.status = (success ? "success" : "failure");
        res.statCode = (success ? OK_CODE : BAD_REQUEST_CODE);
        res.epName = (ep != null ? ep.getEndpoint() : "");
        res.apiLimit = (ep != null ? ep.getApiMaxLimitPerWindow() : -1);
        res.apiTimeWin = (ep != null ? ep.getApiTimeWindowInSec() : -1);

        return res;
    }

    /**
     * Creates a API Response for {@code "/addOrg"} endpoint in the below format
     * <pre>
     *
     * {
     *     "status" : "success",
     *     "statCode":  200,
     *     "orgId" : "12.34.56.78",
     *     "orgName" : "paytm",
     *     "msg" : "org added",
     *
     * }
     * </pre>
     *
     * @param org     pass the {@link OrgModel} object to print else {@code null}
     * @param msg     pass the message to be displayed in API Response
     * @param success pass {@code true} on success else {@code false}
     * @return returns a formatted JSON API Response
     */
    public static NewOrgResp getOrgResp(OrgModel org, String msg, boolean success) {

        NewOrgResp res = new NewOrgResp();

        res.status = (success ? "success" : "failure");
        res.statCode = (success ? OK_CODE : BAD_REQUEST_CODE);
        res.orgId = (org != null ? org.getOrgId() : "");
        res.orgName = (org != null ? org.getOrgName() : "");
        res.msg = msg;

        return res;
    }

    /**
     * Creates a API Response for {@code "/checkLimit"} endpoint in the below format
     * <pre>
     *
     * {
     *     "status" : "success",
     *     "msg" : "allow",
     *     "statCode" : 200,
     *     "epStatus" : "allow",
     *     "epStatCode" : 200,
     *     "callsMade" : 2,
     *     "apiLimit" : 5,
     *     "apiWinSec" : 60,
     *     "limitResetMillis" : "1627471072981"
     * }
     * </pre>
     *
     * @param apiRec   pass the {@link ApiRecord} object to print else {@code null}
     * @param epStatus pass {@link Constants.Status} of
     *                 the {@code "/checkLimit"} request.
     * @return returns a formatted JSON API Response
     */
    public static CheckLimitResp getLimitResp(ApiRecord apiRec, Constants.Status epStatus, boolean status, String msg) {

        CheckLimitResp res = new CheckLimitResp();

        res.status = (status ? "success" : "failure");
        res.msg = msg;
        res.statCode = (status ? OK_CODE : BAD_REQUEST_CODE);
        res.epStatus = epStatus.toString();
        res.epStatCode = CM.getStatusCode(epStatus);
        res.callsMade = (apiRec != null ? apiRec.getCallCount() : -1);
        res.apiLimit = (apiRec != null ? apiRec.getMaxLim() : -1);
        res.apiWinSec = (apiRec != null ? (apiRec.getApiCallQuantum() / 1000L) : -1);
        res.limitResetMillis = (apiRec != null ?
                (apiRec.getCallWindowStart() + apiRec.getApiCallQuantum()) : -1);

        return res;
    }

    private static int getStatusCode(Constants.Status status) {
        switch (status) {
            case ALLOW:
                return OK_CODE;
            case LIMIT_EXCEEDED:
                return TOO_MANY_REQ_CODE;
            case NOT_FOUND:
                return NOT_FOUND_CODE;
        }
        return BAD_REQUEST_CODE;
    }

}
