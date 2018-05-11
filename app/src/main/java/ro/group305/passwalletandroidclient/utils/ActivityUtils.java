package ro.group305.passwalletandroidclient.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ActivityUtils {
    private ActivityUtils(){
    }

    public static void displayErrorMessage(Context activity, String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
