package GetterSettersAdapters;

import android.content.SharedPreferences;

/**
 * Created by shreyasingh on 4/29/18.
 */

public class UtilityFunctions {

    public static SharedPreferences.Editor putDouble(
            final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(
            SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }


}
