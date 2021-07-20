package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.service.PasswordGenerator;
import ro.group305.passwalletandroidclient.R;

public class EditPassWalletItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pass_wallet_item);
        createSavePasswalletItemButton();
        initUserAccountFields((UserAccount) getIntent().getSerializableExtra("selectedUserAccount"));
        createGeneratePasswordButton();
    }

    private void createGeneratePasswordButton() {
        Button createPasswalletItemButton = findViewById(R.id.generate_password_button);
        createPasswalletItemButton.setOnClickListener(v -> {
            PasswordGenerator passwordGenerator = GeneratePasswordActivity.getGeneratorFromPrefs(this);
            EditText passwordEditText = findViewById(R.id.password_EditText);
            passwordEditText.setText(passwordGenerator.generate());
        });
    }

    private void createSavePasswalletItemButton() {
        Button savePasswalletItemButton = findViewById(R.id.save_passwallet_item_button);
        savePasswalletItemButton.setOnClickListener(v -> {
            UserAccount userAccount = buildUserAccount();
            UserAccountUIValidator validator = new UserAccountUIValidator(EditPassWalletItemActivity.this);
            if (validator.isNotValid(userAccount)) {
                return;
            }

            Intent userAccountIntent = new Intent();
            userAccountIntent.putExtra("updatedUserAccount", userAccount);
            setResult(Activity.RESULT_OK, userAccountIntent);
            finish();
        });
    }

    private UserAccount buildUserAccount() {
        EditText nickNameEditText = findViewById(R.id.nick_name_EditText);
        EditText nameEditText = findViewById(R.id.name_EditText);
        EditText passwordEditText = findViewById(R.id.password_EditText);
        EditText siteEditText = findViewById(R.id.site_EditText);
        EditText descriptionEditText = findViewById(R.id.description_EditText);
        UserAccount updatedUserAccount = new UserAccount();
        updatedUserAccount.setId(((UserAccount) getIntent().getSerializableExtra("selectedUserAccount")).getId());
        updatedUserAccount.setNickName(nickNameEditText.getText().toString());
        updatedUserAccount.setName(nameEditText.getText().toString());
        updatedUserAccount.setPassword(passwordEditText.getText().toString());
        updatedUserAccount.setSiteURL(siteEditText.getText().toString());
        updatedUserAccount.setDescription(descriptionEditText.getText().toString());
        return updatedUserAccount;
    }

    private void initUserAccountFields(UserAccount userAccount) {
        EditText nickNameEditText = findViewById(R.id.nick_name_EditText);
        nickNameEditText.setText(userAccount.getNickName());
        EditText nameEditText = findViewById(R.id.name_EditText);
        nameEditText.setText(userAccount.getName());
        EditText passwordEditText = findViewById(R.id.password_EditText);
        passwordEditText.setText(userAccount.getPassword());
        EditText siteEditText = findViewById(R.id.site_EditText);
        siteEditText.setText(userAccount.getSiteURL());
        EditText descriptionEditText = findViewById(R.id.description_EditText);
        descriptionEditText.setText(userAccount.getDescription());
    }
}
