package com.factorysoft.snatch;

/**
 * Created by defcon8808 on 14. 6. 2.
 */
public final class GeofenceUtils {

    /*
     * A log tag for the application
     */
    public static final String APPTAG = "Geofence Detection";

    // Intent actions
    public static final String ACTION_CONNECTION_ERROR =
            "com.factorysoft.snatch.ACTION_CONNECTION_ERROR";

    public static final String ACTION_CONNECTION_SUCCESS =
            "com.factorysoft.snatch.ACTION_CONNECTION_SUCCESS";

    public static final String ACTION_GEOFENCES_ADDED =
            "com.factorysoft.snatch.ACTION_GEOFENCES_ADDED";

    public static final String ACTION_GEOFENCES_REMOVED =
            "com.factorysoft.snatch.ACTION_GEOFENCES_DELETED";

    public static final String ACTION_GEOFENCE_ERROR =
            "com.factorysoft.snatch.ACTION_GEOFENCES_ERROR";

    public static final String ACTION_GEOFENCE_TRANSITION =
            "com.factorysoft.snatch.ACTION_GEOFENCE_TRANSITION";

    public static final String ACTION_GEOFENCE_TRANSITION_ERROR =
            "com.factorysoft.snatch.ACTION_GEOFENCE_TRANSITION_ERROR";

    // The Intent category used by all Location Services sample apps
    public static final String CATEGORY_LOCATION_SERVICES =
            "com.factorysoft.snatch.CATEGORY_LOCATION_SERVICES";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_CODE =
            "com.factorysoft.snatch.EXTRA_CONNECTION_CODE";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "com.factorysoft.snatch.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "com.factorysoft.snatch.EXTRA_CONNECTION_ERROR_MESSAGE";

    public static final String EXTRA_GEOFENCE_STATUS =
            "com.factorysoft.snatch.EXTRA_GEOFENCE_STATUS";

    /*
     * Keys for flattened geofences stored in SharedPreferences
     */
    public static final String KEY_LATITUDE = "com.factorysoft.snatch.KEY_LATITUDE";

    public static final String KEY_LONGITUDE = "com.factorysoft.snatch.KEY_LONGITUDE";

    public static final String KEY_RADIUS = "com.factorysoft.snatch.KEY_RADIUS";

    public static final String KEY_EXPIRATION_DURATION =
            "com.factorysoft.snatch.KEY_EXPIRATION_DURATION";

    public static final String KEY_TRANSITION_TYPE =
            "com.factorysoft.snatch.KEY_TRANSITION_TYPE";

    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX =
            "com.factorysoft.snatch.KEY";

    // Invalid values, used to test geofence storage when retrieving geofences
    public static final long INVALID_LONG_VALUE = -999l;

    public static final float INVALID_FLOAT_VALUE = -999.0f;

    public static final int INVALID_INT_VALUE = -999;

    /*
     * Constants used in verifying the correctness of input values
     */
    public static final double MAX_LATITUDE = 90.d;

    public static final double MIN_LATITUDE = -90.d;

    public static final double MAX_LONGITUDE = 180.d;

    public static final double MIN_LONGITUDE = -180.d;

    public static final float MIN_RADIUS = 1f;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // A string of length 0, used to clear out input fields
    public static final String EMPTY_STRING = new String();

    public static final CharSequence GEOFENCE_ID_DELIMITER = ",";
}
