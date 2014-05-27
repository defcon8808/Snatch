package com.factorysoft.snatch;

import android.location.Location;

/**
 * Created by defcon8808 on 14. 5. 27.
 */
public class Utils {
    public static String format(Location location) {
        return String.format("%s,%s", location.getLatitude(), location.getLongitude());
    }
}
