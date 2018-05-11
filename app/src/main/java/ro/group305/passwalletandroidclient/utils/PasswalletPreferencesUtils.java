package ro.group305.passwalletandroidclient.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class PasswalletPreferencesUtils {

    private PasswalletPreferencesUtils() {
    }

    public static void saveSelectedFileToPreferences(Activity activity, Uri selectedWalletURI) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedWalletURI", selectedWalletURI.toString());
        editor.commit();
    }

    public static String loadLastSelectedFile(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sharedPreferences.getString("selectedWalletURI", "");
    }
}
