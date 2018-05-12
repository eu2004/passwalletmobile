package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ro.eu.passwallet.model.UserAccount;
import ro.group305.passwalletandroidclient.R;

public class CreatePassWalletItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pass_wallet_item);
        createCreatePasswalletItemButton();
    }

    private void createCreatePasswalletItemButton() {
        Button createPasswalletItemButton = findViewById(R.id.create_passwallet_item_button);
        createPasswalletItemButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UserAccount userAccount = buildUserAccount();
                UserAccountUIValidator validator = new UserAccountUIValidator(CreatePassWalletItemActivity.this);
                if (!validator.isValid(userAccount)) {
                    return;
                }

                Intent userAccountIntent = new Intent();
                userAccountIntent.putExtra("newUserAccount", userAccount);
                setResult(Activity.RESULT_OK, userAccountIntent);
                finish();
            }
        });
    }

    private UserAccount buildUserAccount() {
        EditText nickNameEditText = findViewById(R.id.nick_name_EditText);
        EditText nameEditText = findViewById(R.id.name_EditText);
        EditText passwordEditText = findViewById(R.id.password_EditText);
        EditText siteEditText = findViewById(R.id.site_EditText);
        EditText descriptionEditText = findViewById(R.id.description_EditText);
        UserAccount newUserAccount = new UserAccount();
        newUserAccount.setNickName(nickNameEditText.getText().toString());
        newUserAccount.setName(nameEditText.getText().toString());
        newUserAccount.setPassword(passwordEditText.getText().toString());
        newUserAccount.setSiteURL(siteEditText.getText().toString());
        newUserAccount.setDescription(descriptionEditText.getText().toString());
        return newUserAccount;
    }
}
