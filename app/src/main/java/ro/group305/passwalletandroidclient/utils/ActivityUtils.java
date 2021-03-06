package ro.group305.passwalletandroidclient.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class ActivityUtils {
    private ActivityUtils(){
    }

    public static String appendStrings(String ... args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg);
        }
        return stringBuilder.toString();
    }

    public static void displayErrorMessage(Activity activity, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    public static void saveSelectedFileToPreferences(Activity activity, Uri selectedWalletURI) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedWalletURI", selectedWalletURI.toString());
        editor.apply();
    }

    public static String loadLastSelectedFile(Activity activity) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return sharedPreferences.getString("selectedWalletURI", "");
    }
}
