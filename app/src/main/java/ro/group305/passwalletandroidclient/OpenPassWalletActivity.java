package ro.group305.passwalletandroidclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class OpenPassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private static final int READ_REQUEST_CODE = 42;
    private Uri selectedWalletURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pass_wallet);

        createBrowsePasswalletButton();

        createOpenSelectedPasswalletButton();

        createCreateNewPasswalletLink();

        initSelectedWalletURI();
    }

    private void createCreateNewPasswalletLink() {
        TextView createNewPasswalletTextView = findViewById(R.id.create_new_passwallet_textview);
        createNewPasswalletTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Create new passwallet");
                Intent intent = new Intent(OpenPassWalletActivity.this, CreatePassWalletActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initSelectedWalletURI() {
        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
        String lastWalletURI = getPreferences(Context.MODE_PRIVATE).getString("selectedWalletURI", "");
        selectedWalletName.setText(lastWalletURI);
        if (lastWalletURI.length() > 0) {
            selectedWalletURI = Uri.parse(lastWalletURI);
        }
    }

    private void createOpenSelectedPasswalletButton() {
        Button openSelectedPasswalletButton = findViewById(R.id.open_passwallet_button);
        openSelectedPasswalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!WalletFileURI.isValid(selectedWalletURI)) {
                    Log.e(TAG, "No wallet file is selected or file path is incorrect!");
                    return;
                }

                Intent intent = new Intent(OpenPassWalletActivity.this, ManagePassWalletActivity.class);
                intent.putExtra("encryptedWalletFileURI", selectedWalletURI.toString());
                EditText password = findViewById(R.id.walletKey);
                intent.putExtra("key", password.getText().toString().getBytes());
                startActivity(intent);
            }
        });
    }

    private void createBrowsePasswalletButton() {
        Button selectLocalPasswalletButton = findViewById(R.id.browse_passwallet_file_button);
        selectLocalPasswalletButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    browseForEncryptedWalletFile();
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case READ_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        selectedWalletURI = data.getData();
                        Log.i(TAG, "Uri: " + selectedWalletURI.toString());
                        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("selectedWalletURI", selectedWalletURI.toString());
                        editor.commit();
                        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
                        selectedWalletName.setText(selectedWalletURI.toString());
                    } else {
                        Log.e(TAG, "Data is null, resultCode " + resultCode);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void browseForEncryptedWalletFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
