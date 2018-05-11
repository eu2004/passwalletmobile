package ro.group305.passwalletandroidclient.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class PasswalletPreferencesUtils {

    private PasswalletPreferencesUtils() {
    }

    public static void saveSelectedFileToPreferences(Activity activity, Uri selectedWalletURI) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("selectedWalletURI", selectedWalletURI.toString());
        editor.commit();
    }

    public static String loadLastSelectedFile(Activity activity) {
        SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
        return preferences.getString("selectedWalletURI", "");
    }
}
