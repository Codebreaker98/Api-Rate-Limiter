  package com.blueoptima.apirate;

public class Constants {

    public enum Status {
        ALLOW,
        LIMIT_EXCEEDED,
        NOT_FOUND;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * Defines the Default Limit of Max API Calls
     */
    public static final long DEFAULT_API_CALL_LIMIT = 10L;

    /**
     * API calls are allowed till 1 min as per specified Limits
     */
    public static final long DEFAULT_ALLOWED_CALL_QUANTUM = 60000L;

    /**
     * Every 10 mins, the data will be refreshed from DB
     */
//    public static final long DB_REFRESH_QUANTUM = 600000L;
    public static final long DB_REFRESH_QUANTUM = 20000L;   // for testing, DB refresh
                                                            // set to 20sec

    public static final int OK_CODE = 200;
    public static final int NOT_FOUND_CODE = 404;
    public static final int BAD_REQUEST_CODE = 400;
    public static final int TOO_MANY_REQ_CODE = 429;

//    public static final String INVALID_CRED = "invalid credentials";
    public static final String INVALID_CRED = "invalid cred";
//    public static final String ORG_ADD_SUCCESS = "organisation added successfully";
    public static final String ORG_ADD_SUCCESS = "org added";
//    public static final String ORG_ID_IN_USE = "organisation id is already in use";
    public static final String ORG_ID_IN_USE = "org_id in use";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    public static final String NO_SUCH_ORG = "no such org";  
    public static final String CREATED = "created";
    public static final String UPDATED = "updated";
    public static final String ABSENT = "absent";
    public static final String DELETED = "deleted";
    public static final String SORRY_ERROR = "oops... something went wrong!";
}
