package ro.group305.passwalletandroidclient.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.EditText;

import ro.eu.passwallet.model.UserAccount;
import ro.group305.passwalletandroidclient.R;

public class UserAccountUIValidator {
    private Activity context;

    public UserAccountUIValidator(Activity context) {
        this.context = context;
    }

    public boolean isValid(UserAccount userAccount) {
        if (!isValueValid(userAccount.getNickName())) {
            EditText nickNameEditText = context.findViewById(R.id.nick_name_EditText);
            nickNameEditText.setError("Cannot be empty");
            return false;
        }

        return true;
    }

    public boolean isValueValid(String value) {
        if (value == null || value.trim().length() == 0)
            return false;
        return true;
    }
}
