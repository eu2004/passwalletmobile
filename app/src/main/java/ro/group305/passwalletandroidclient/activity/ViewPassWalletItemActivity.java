package ro.group305.passwalletandroidclient.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ro.group305.passwalletandroidclient.R;

public class ViewPassWalletItemActivity extends EditPassWalletItemActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setViewUIConstraints();
    }

    private void setViewUIConstraints() {
        EditText nickNameEditText = findViewById(R.id.nick_name_EditText);
        nickNameEditText.setEnabled(false);
        EditText nameEditText = findViewById(R.id.name_EditText);
        nameEditText.setEnabled(false);
        EditText passwordEditText = findViewById(R.id.password_EditText);
        passwordEditText.setEnabled(false);
        EditText siteEditText = findViewById(R.id.site_EditText);
        siteEditText.setEnabled(false);
        EditText descriptionEditText = findViewById(R.id.description_EditText);
        descriptionEditText.setEnabled(false);

        Button savePasswalletItemButton = findViewById(R.id.save_passwallet_item_button);
        savePasswalletItemButton.setVisibility(View.INVISIBLE);

        TextView editAccountLabelTextView = findViewById(R.id.edit_account_label_textView);
        editAccountLabelTextView.setVisibility(View.INVISIBLE);

        Button generatePassButton = findViewById(R.id.generate_password_button);
        generatePassButton.setVisibility(View.INVISIBLE);
    }
}
