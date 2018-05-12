package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import ro.eu.passwallet.model.UserAccount;
import ro.group305.passwalletandroidclient.R;

class UserAccountUIValidator {
    private static final String TAG = "PassWallet";

    private Activity context;

    public UserAccountUIValidator(Activity context) {
        this.context = context;
    }

    public boolean isValid(UserAccount userAccount) {
        if (!isValueValid(userAccount.getNickName())) {
            Log.e(TAG, "Nickname is empty.");
            EditText nickNameEditText = context.findViewById(R.id.nick_name_EditText);
            if (nickNameEditText != null) {
                nickNameEditText.setError("Cannot be empty");
            }
            return false;
        }

        if (!isValueValid(userAccount.getName())) {
            Log.e(TAG, "Name is empty.");
            EditText nameEditText = context.findViewById(R.id.name_EditText);
            if (nameEditText != null) {
                nameEditText.setError("Cannot be empty");
            }
            return false;
        }

        if (!isValueValid(userAccount.getPassword())) {
            Log.e(TAG, "Password is empty.");
            EditText passwordEditText = context.findViewById(R.id.password_EditText);
            if (passwordEditText != null) {
                passwordEditText.setError("Cannot be empty");
            }
            return false;
        }

        return true;
    }

    private boolean isValueValid(String value) {
        return value != null && value.trim().length() > 0;
    }
}
