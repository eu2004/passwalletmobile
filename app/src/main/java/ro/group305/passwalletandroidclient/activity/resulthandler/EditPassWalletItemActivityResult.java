package ro.group305.passwalletandroidclient.activity.resulthandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ro.eu.passwallet.model.UserAccount;
import ro.group305.passwalletandroidclient.activity.EditPassWalletItemActivity;

public class EditPassWalletItemActivityResult  extends ActivityResultContract<UserAccount, Intent> {

    private final AppCompatActivity appCompatActivity;

    public EditPassWalletItemActivityResult(AppCompatActivity appCompatActivity) {
        super();
        this.appCompatActivity = appCompatActivity;
    }

    @CallSuper
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull UserAccount input) {
        Intent intent = new Intent(appCompatActivity, EditPassWalletItemActivity.class);
        intent.putExtra("selectedUserAccount", input);
        return intent;
    }

    @Nullable
    @Override
    public final SynchronousResult<Intent> getSynchronousResult(@NonNull Context context,
                                                                @NonNull UserAccount input) {
        return null;
    }

    @Nullable
    @Override
    public final Intent parseResult(int resultCode, @Nullable Intent intent) {
        if (intent == null || resultCode != Activity.RESULT_OK)
            return null;
        return intent;
    }
}
