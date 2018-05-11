package ro.group305.passwalletandroidclient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.PasswalletPreferencesUtils;
import ro.group305.passwalletandroidclient.utils.WalletFileURI;

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
        String lastWalletURI = PasswalletPreferencesUtils.loadLastSelectedFile(this);
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
                try {
                    //validate first the password
                    EditText password = findViewById(R.id.walletKey);
                    CryptographyService cryptographyService = new CryptographyService(password.getText().toString());
                    WalletFileURI walletFileURI = new WalletFileURI(selectedWalletURI, getContentResolver());
                    cryptographyService.decrypt(walletFileURI.getContent());

                    //continue with the search list
                    Intent intent = new Intent(OpenPassWalletActivity.this, ManagePassWalletActivity.class);
                    intent.putExtra("encryptedWalletFileURI", selectedWalletURI.toString());
                    intent.putExtra("key", password.getText().toString().getBytes());
                    startActivity(intent);
                } catch (Exception exception) {
                    ActivityUtils.displayErrorMessage(OpenPassWalletActivity.this, "Error loading wallet", exception.getMessage());
                }

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
                        PasswalletPreferencesUtils.saveSelectedFileToPreferences(this, selectedWalletURI);
                        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
                        selectedWalletName.setText(selectedWalletURI.toString());
                    } else {
                        Log.e(TAG, "Data is null, resultCode " + resultCode);
                    }
                }
                break;
        }
    }

    private void browseForEncryptedWalletFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
