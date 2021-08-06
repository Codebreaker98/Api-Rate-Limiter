package com.blueoptima.apirate.Controllers;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import com.blueoptima.apirate.CM;
import com.blueoptima.apirate.Models.ApiResp.CheckLimitResp;
import com.blueoptima.apirate.Models.ApiResp.NewOrgResp;
import com.blueoptima.apirate.Models.ApiResp.SetEndpointResp;
import com.blueoptima.apirate.Validators.DbDataUpdater;
import com.blueoptima.apirate.Models.OrgModel;
import com.blueoptima.apirate.Constants;
import com.blueoptima.apirate.Validators.CallValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blueoptima.apirate.Models.EndpointModel;

import static com.blueoptima.apirate.Constants.FAILURE;

@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private CallValidator callValidator;

    @Autowired
    private DbDataUpdater dbDataUpdater;

    /**
     * {@code "/addOrg"} endpoint is to be called by the API Rate Limiter Team with
     * valid credentials. It's ONLY called by the system to add a new Organisation
     * to the DB so that, later on new Endpoints can be added by Organisation itself
     * through the {@code "/addOrUpdateEndpoint"} endpoint using their unique
     * {@code orgId}
     *
     * @param newOrgModel pass {@link OrgModel} object
     * @return formatted JSON Response
     */
    @PostMapping("/addOrg")
    public NewOrgResp addOrg(@RequestBody OrgModel newOrgModel) {

        NewOrgResp response = CM.getOrgResp(null, FAILURE, false);

        try {   
            response = dbDataUpdater.addOrg(newOrgModel);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * {@code "/addOrUpdateEndpoint"} endpoint is to be called by the Organisation
     * who'll be taking the service of API Rate Limiter to add an endpoint
     * along with the limits. Refer API Docs for body format
     *
     * @param newEpModel the new {@link EndpointModel} object received from the
     *                   API request
     * @return formatted JSON Response
     */
    @PostMapping("/addOrUpdateEndpoint")
    public SetEndpointResp addEndpoint(@RequestBody EndpointModel newEpModel) {

        SetEndpointResp response = CM.getApiResp(null, FAILURE, false);

        try {
            response = dbDataUpdater.addOrUpdateEndpoint(newEpModel);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * {@code "/deleteEndpoint"} endpoint is to be called by the Organisation
     * who'll be taking the service of API Rate Limiter to delete an endpoint.
     * Refer API Docs for body format
     *
     * @param delEpModel the {@link EndpointModel} object to be deleted
     * @return formatted JSON Response
     */
    @DeleteMapping("/deleteEndpoint")
    public SetEndpointResp deleteEndpoint(@RequestBody   EndpointModel delEpModel) {

        SetEndpointResp response = CM.getApiResp(null, FAILURE, false);

        try {
            response = dbDataUpdater.deleteEndpoint(delEpModel);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * {@code "/checkLimit"} endpoint is to be called by the Organisation everytime
     * they receive a API call from their Client. While the Organisation calls our
     * <b>API Rate Limiter</b> endpoint (i.e. {@code "/checkLimit"}), they need to
     * plug in 3 params where the {@code orgId} identifies the caller Organisation
     * uniquely in our system preventing any exploitation
     *
     * @param orgId    unique private ID assigned to an Organisation (works as an
     *                 API Key for our service)
     * @param apiKey   unique identifier the Organisation must have assigned to their
     *                 Clients to identify them
     * @param endpoint the endpoint being hit by the Client on the Organisation's API
     *                 Gateway which the Organisation must have already set in out system
     * @return formatted JSON Response
     */
    @GetMapping("/checkLimit")
    public CheckLimitResp checkLimit(@RequestParam String orgId,
                                     @RequestParam String apiKey,
                                     @RequestParam String endpoint,HttpServletRequest request) {
        return callValidator.isValidApiCall(orgId, apiKey, endpoint);
    }
}
