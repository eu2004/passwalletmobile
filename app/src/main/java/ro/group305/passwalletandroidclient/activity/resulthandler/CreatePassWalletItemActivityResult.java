package ro.group305.passwalletandroidclient.activity.resulthandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import ro.group305.passwalletandroidclient.activity.CreatePassWalletItemActivity;

public class CreatePassWalletItemActivityResult extends ActivityResultContract<String, Intent> {

    private final AppCompatActivity appCompatActivity;

    public CreatePassWalletItemActivityResult(AppCompatActivity appCompatActivity) {
        super();
        this.appCompatActivity = appCompatActivity;
    }

    @CallSuper
    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull String input) {
        return new Intent(appCompatActivity, CreatePassWalletItemActivity.class);
    }

    @Nullable
    @Override
    public final SynchronousResult<Intent> getSynchronousResult(@NonNull Context context,
                                                             @NonNull String input) {
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
