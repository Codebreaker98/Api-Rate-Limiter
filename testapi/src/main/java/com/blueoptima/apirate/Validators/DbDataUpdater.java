 package com.blueoptima.apirate.Validators;

import com.blueoptima.apirate.CM;
import com.blueoptima.apirate.Models.ApiResp.NewOrgResp;
import com.blueoptima.apirate.Models.ApiResp.SetEndpointResp;
import com.blueoptima.apirate.Models.DB.ApiModel;
import com.blueoptima.apirate.Models.EndpointModel;
import com.blueoptima.apirate.Models.OrgModel;
import com.blueoptima.apirate.Constants;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.blueoptima.apirate.Constants.*;

@Service
public class DbDataUpdater {

    private final Firestore db = FirestoreClient.getFirestore();

    /**
     * ONLY for internal use. Api Rate Limiter will have access to add new Organisations.
     * Once added, orgs can add endpoints against their own OrgId
     *
     * @param newOrgModel pass an {@link OrgModel} object
     * @return Response to the operation
     */
    public NewOrgResp addOrg(OrgModel newOrgModel) throws ExecutionException, InterruptedException {

        DocumentSnapshot admnCred =
                db.collection("constants").document("admnCred").get().get();

        if (newOrgModel.getUserId().equals(admnCred.get("userId"))
                && newOrgModel.getPassword().equals(admnCred.get("pass"))
        ) {
            // to prevent leaking of Cred on passing
            newOrgModel.setUserId("");
            newOrgModel.setPassword("");

            if (db.collection("orgs").whereEqualTo("id", newOrgModel.getOrgId())
                    .get().get().size() > 0) {
                return CM.getOrgResp(null, Constants.ORG_ID_IN_USE, false);
            } else {

                Map<String, String> map = new HashMap<>();
                map.put("id", newOrgModel.getOrgId());
                map.put("orgName", newOrgModel.getOrgName());

                db.collection("orgs").add(map);

                return CM.getOrgResp(newOrgModel, Constants.ORG_ADD_SUCCESS, true);
            }
        } else {
            // to prevent leaking of Cred on passing
            newOrgModel.setUserId("");
            newOrgModel.setPassword("");

            return CM.getOrgResp(null, Constants.INVALID_CRED, false);
        }

    }

    /**
     * Adds or Updates a new API as provided by the organisation only if the
     * {@code orgId} is known to us. Or else API Rate Limiter Team needs to register that
     * new Organisation using userId and Password through {@code "/addOrg"} admin API
     *
     * @param epModel pass the {@link EndpointModel} object to be added or updated
     * @return formatted JSON Response
     */
    public SetEndpointResp addOrUpdateEndpoint(EndpointModel epModel)
            throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> orgDocs = db.collection("orgs")
                .whereEqualTo("id", epModel.getOrgId())
                .get().get().getDocuments();

        if (orgDocs.size() <= 0) {
            return CM.getApiResp(null, NO_SUCH_ORG, false);
        }

        List<QueryDocumentSnapshot> apiDocs = db.collection("apis")
                .whereEqualTo("orgId", epModel.getOrgId())
                .whereEqualTo("apiKey", epModel.getApiKey())
                .get().get().getDocuments();

        // Creating the ApiModel object to be set in DB
        ApiModel apiModel = new ApiModel();

        apiModel.setApiKey(epModel.getApiKey());
        apiModel.setOrgId(epModel.getOrgId());

        Map<String, List<Long>> map = new HashMap<>();
        // Index 0 stores the 'maxLimit' and index 1 stores the 'timeFrame' in seconds
        map.put(epModel.getEndpoint(),
                Arrays.asList(epModel.getApiMaxLimitPerWindow(), epModel.getApiTimeWindowInSec()));

        apiModel.setEndpoints(map);

        if (apiDocs.size() == 0) {
            db.collection("apis").add(apiModel);
            return CM.getApiResp(epModel, CREATED, true);
        } else {

            // Acc to the app's business logic, only ONE document will match 'orgId' and 'apiKey'
            // So, fetching index 0
            ApiModel dbApiModel = apiDocs.get(0).toObject(ApiModel.class);

            boolean epAlreadyPresent = dbApiModel.getEndpoints().containsKey(epModel.getEndpoint());

            dbApiModel.getEndpoints().put(epModel.getEndpoint(),
                    Arrays.asList(epModel.getApiMaxLimitPerWindow(), epModel.getApiTimeWindowInSec()));

            db.collection("apis").document(apiDocs.get(0).getId()).set(dbApiModel);
            if (epAlreadyPresent)
                return CM.getApiResp(epModel, UPDATED, true);
            else
                return CM.getApiResp(epModel, CREATED, true);
        }
    }

    /**
     * Deletes the specified API as provided by the organisation only if the
     * {@code orgId} is known to us. Or else API Rate Limiter Team needs to register that
     * new Organisation through {@code "/addOrg"} admin API using userId and Password
     *
     * @param epModel pass the {@link EndpointModel} object to be added or updated
     * @return formatted JSON Response
     */
    public SetEndpointResp deleteEndpoint(EndpointModel epModel) throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> orgDocs = db.collection("orgs")
                .whereEqualTo("id", epModel.getOrgId())
                .get().get().getDocuments();

        if (orgDocs.size() <= 0) {
            return CM.getApiResp(null, NO_SUCH_ORG, false);
        }

        List<QueryDocumentSnapshot> apiDocs = db.collection("apis")
                .whereEqualTo("orgId", epModel.getOrgId())
                .whereEqualTo("apiKey", epModel.getApiKey())
                .get().get().getDocuments();


        if (apiDocs.size() == 0) {
            return CM.getApiResp(null, ABSENT, true);
        } else {

            ApiModel dbApiModel = apiDocs.get(0).toObject(ApiModel.class);

            boolean epAlreadyPresent = dbApiModel.getEndpoints().containsKey(epModel.getEndpoint());

            if (epAlreadyPresent) {
                epModel.setApiMaxLimitPerWindow(dbApiModel.getEndpoints().get(epModel.getEndpoint()).get(0));
                epModel.setApiTimeWindowInSec(dbApiModel.getEndpoints().get(epModel.getEndpoint()).get(1));
            }
            dbApiModel.getEndpoints().remove(epModel.getEndpoint());

            if (dbApiModel.getEndpoints().size() == 0) {
                // after deletion, if no more endpoint is present in a document, we delete that document
                // to create more space in the DB
                db.collection("apis").document(apiDocs.get(0).getId()).delete();
            } else {
                db.collection("apis").document(apiDocs.get(0).getId()).set(dbApiModel);
            }

            if (epAlreadyPresent)
                return CM.getApiResp(epModel, DELETED, true);
            else
                return CM.getApiResp(null, ABSENT, true);
        }
    }
}
