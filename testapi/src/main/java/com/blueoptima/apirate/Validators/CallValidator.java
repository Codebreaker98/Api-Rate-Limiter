package com.blueoptima.apirate.Validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import com.blueoptima.apirate.CM;
import com.blueoptima.apirate.Models.ApiRecord;
import com.blueoptima.apirate.Constants;
import com.blueoptima.apirate.Models.ApiResp.CheckLimitResp;
import org.springframework.stereotype.Service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;

@Service
public class CallValidator {

    // {org_id},
    //     {{api_key},
    //         {{endpoint}, {ApiRecord}
    //     }
    // }
    private static HashMap<String, HashMap<String, HashMap<String, ApiRecord>>> cacheHm = new HashMap<>();
    private static long lastDbRefresh = System.currentTimeMillis();
  
    private static boolean localDataStructureInitialized = false;


    /**
     * Fetches the API Keys, their Endpoints and limits from the Database
     * for all the Organizations and caches it optimally in the Memory
     * to reduce the latency of {@link #isValidApiCall(String, String, String)}
     */
    @PostConstruct
    public static void initializeCallValidator() {
        try {
            System.out.println("initializing local data structure");
            initLocalDS();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public CheckLimitResp isValidApiCall(String orgId, String apiKey, String endpoint) {

        // Initialized Local DS in case it doesn't get created beforehand due to any programming error
        // Although, this condition will always be false in normal scenarios
        if (!localDataStructureInitialized) {
            initializeCallValidator();
        }

        ApiRecord apiRecord = getApiRecord(orgId, apiKey, endpoint);
        if (apiRecord == null) {
            return CM.getLimitResp(null, Constants.Status.NOT_FOUND, true, "not_found");
        }

        // General Logs
        System.out.println("----");

        System.out.println("maxLim  : " + apiRecord.getMaxLim());
        System.out.println("callQnt : " + apiRecord.getApiCallQuantum());
        System.out.println("callCnt : " + apiRecord.getCallCount());
        System.out.println("winStrt : " + apiRecord.getCallWindowStart());
        System.out.println();

        boolean res = isApiCallUnderLimit(apiRecord);

        if (res)
            return CM.getLimitResp(apiRecord, Constants.Status.ALLOW, true, "allow");
        else
            return CM.getLimitResp(apiRecord, Constants.Status.LIMIT_EXCEEDED, true, "exceeded");
    }

    private static boolean isApiCallUnderLimit(ApiRecord apiRecord) {

        if (System.currentTimeMillis() - apiRecord.getCallWindowStart() > apiRecord.getApiCallQuantum()) {
            apiRecord.setCallCount(1);
            apiRecord.setCallWindowStart(System.currentTimeMillis());
            return true;
        } else {
            if (apiRecord.getCallCount() < apiRecord.getMaxLim()) {
                apiRecord.setCallCount(apiRecord.getCallCount() + 1);
                return true;
            } else {
                return false;
            }
        }
    }

    private static ApiRecord getApiRecord(String orgId, String apiKey, String endpoint) {

        boolean foundInCache = false;

        if (cacheHm.containsKey(orgId)) {
            if (cacheHm.get(orgId).containsKey(apiKey)) {
                if (cacheHm.get(orgId).get(apiKey).containsKey(endpoint)) {
                    foundInCache = true;
                }
            }
        }

        if (foundInCache) {
            ApiRecord apiRecord = cacheHm.get(orgId).get(apiKey).get(endpoint);

            long currT = System.currentTimeMillis();
            if (currT - apiRecord.getLastDbRefresh() < Constants.DB_REFRESH_QUANTUM) {
                // General logs
                System.out.println("curr : " + currT);
                System.out.println("last : " + apiRecord.getLastDbRefresh());
                System.out.println("diff : " + (currT - apiRecord.getLastDbRefresh()));
                System.out.println("DBqt : " + Constants.DB_REFRESH_QUANTUM);
                return apiRecord;
            } else {
                System.out.println("curr : " + currT);
                System.out.println("last : " + apiRecord.getLastDbRefresh());
                System.out.println("diff : " + (currT - apiRecord.getLastDbRefresh()));
                System.out.println("DBqt : " + Constants.DB_REFRESH_QUANTUM);
                return getApiRecordFromDB(orgId, apiKey, endpoint, true);
            }
        } else {
            return getApiRecordFromDB(orgId, apiKey, endpoint, false);
        }
    }

    /**
     * gets {@link ApiRecord} obj from DB
     *
     * @param orgId orgId
     * @param apiKey apiKey
     * @param endpoint endpoint
     * @param updation if false, doesn't allow to access DB atleast before
     *                 {@link Constants#DB_REFRESH_QUANTUM} millis to prevent any infinite DB hits
     *                 for some unknown endpoint as everytime the system will keep searching the DB
     *                 as it'll never be present in thr Local Cache.
     * @return
     */
    private static ApiRecord getApiRecordFromDB(String orgId, String apiKey, String endpoint, boolean updation) {

        if (System.currentTimeMillis() - lastDbRefresh < Constants.DB_REFRESH_QUANTUM && !updation) {
            System.out.println("N");
            return null;
        }

        System.out.println("C");
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection("apis").get();


        QuerySnapshot queryDocumentSnapshots;

        try {
            queryDocumentSnapshots = db.collection("apis")
                    .whereEqualTo("orgId", orgId)
                    .whereEqualTo("apiKey", apiKey)
                    .get().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        if (queryDocumentSnapshots.size() <= 0) {
            System.out.println("D");
            lastDbRefresh = System.currentTimeMillis();
            return null;
        } else {

            // Only 1 combination of "orgId" and "apiKey" will match.
            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

            System.out.println("E");
            Map<String, List<Long>> mapFromDb = (Map<String, List<Long>>) doc.get("endpoints");

            if (updation) {
                // fetches the oldApiRecord, updates that and returns it
                ApiRecord oldApiRecord = cacheHm.get(orgId).get(apiKey).get(endpoint);
                oldApiRecord.setLastDbRefresh(System.currentTimeMillis());

                oldApiRecord.setMaxLim(mapFromDb.get(endpoint).get(0));

                oldApiRecord.setApiCallQuantum(CM.secToMillis(mapFromDb.get(endpoint).get(1)));

                System.err.println("F");
                System.out.println("dbRefr : " + oldApiRecord.getLastDbRefresh());
                return oldApiRecord;
            } else {
                // fetches new ApiRecord from DB and adds it to the Local Cache
                HashMap<String, ApiRecord> hmForEndpoint = new HashMap<>();

                // IMPORTANT
                // updates lastDbRefresh only if it was NOT an updation request and
                // data was found in the DB.
                lastDbRefresh = System.currentTimeMillis();

                mapFromDb.forEach((currEndpoint, epVals) -> {
                    hmForEndpoint.put(currEndpoint, new ApiRecord(epVals.get(0), epVals.get(1)));
                });

                HashMap<String, HashMap<String, ApiRecord>> hmForOrg = new HashMap<>();
                hmForOrg.put(apiKey, hmForEndpoint);

                if (cacheHm.containsKey(orgId)) {
                    cacheHm.get(orgId).put(apiKey, hmForEndpoint);
                } else {
                    cacheHm.put(orgId, hmForOrg);
                }

                return hmForEndpoint.get(endpoint);
            }
        }
    }

    /**
     * Initializes the Local Data Structure when the Server starts
     */
    private static void initLocalDS() throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection("orgs").get();

        List<QueryDocumentSnapshot> allOrgs = query.get().getDocuments();

        for (QueryDocumentSnapshot orgDoc : allOrgs) {

            String currOrgId = orgDoc.getString("id");

            List<QueryDocumentSnapshot> apis = db.collection("apis")
                    .whereEqualTo("orgId", currOrgId)
                    .get()
                    .get()
                    .getDocuments();

            HashMap<String, HashMap<String, ApiRecord>> hmForOrg = new HashMap<>();

            for (QueryDocumentSnapshot apiDoc : apis) {

                String apiKey = apiDoc.getString("apiKey");
                HashMap<String, ApiRecord> hmForEndpoint = new HashMap<>();

                Map<String, List<Long>> mapFromDb = (Map<String, List<Long>>) apiDoc.get("endpoints");
                if (mapFromDb == null) {
                    System.err.println("Map is empty");
                    continue;
                }
                mapFromDb.forEach((currEndpoint, epVals) -> {
                    hmForEndpoint.put(currEndpoint, new ApiRecord(epVals.get(0), epVals.get(1)));
                });

                hmForOrg.put(apiKey, hmForEndpoint);
            }

            cacheHm.put(currOrgId, hmForOrg);
        }

        localDataStructureInitialized = true;
    }
}
