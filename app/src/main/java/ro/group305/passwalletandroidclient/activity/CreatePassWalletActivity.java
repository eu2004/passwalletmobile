package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;

import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class CreatePassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private static final int CREATE_REQUEST_CODE = 1;
    private byte[] encryptedDefaultPasswalletContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pass_wallet);
        createBrowsePasswalletButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CREATE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri selectedWalletURI = data.getData();
                        try {
                            UriUtils.saveUriContent(selectedWalletURI, this.getContentResolver(), encryptedDefaultPasswalletContent);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                            ActivityUtils.displayErrorMessage(this, "Fatal Error", "Error saving passwallet file:" + e.getMessage());
                            return;
                        }
                        ActivityUtils.saveSelectedFileToPreferences(this, selectedWalletURI);
                        startManagePassWalletActivity(selectedWalletURI);
                    } else {
                        Log.e(TAG, "Data is null, resultCode " + resultCode);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startManagePassWalletActivity(Uri selectedWalletURI) {
        Intent intent = new Intent(this, ManagePassWalletActivity.class);
        intent.putExtra("encryptedWalletFileURI", selectedWalletURI.toString());
        EditText password = findViewById(R.id.walletKey);
        intent.putExtra("key", password.getText().toString().getBytes());
        startActivity(intent);
    }


    private void createBrowsePasswalletButton() {
        Button selectLocalPasswalletButton = findViewById(R.id.create_passwallet_button);
        selectLocalPasswalletButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (!validatePasswalletKey()) {
                        Log.e(TAG, "Passwallet key is empty!");
                        ActivityUtils.displayErrorMessage(CreatePassWalletActivity.this, "Error creating wallet", "Key is empty!");
                        return;
                    }
                    byte[] defaultPasswallet = loadDefaultPasswalletFromTemplate();
                    EditText password = findViewById(R.id.walletKey);
                    encryptedDefaultPasswalletContent = encrypt(defaultPasswallet, password.getText().toString());
                    browseForEncryptedWalletFile();
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        });
    }

    private byte[] encrypt(byte[] decryptedWalletFile, String key) {
        CryptographyService cryptographyService = new CryptographyService(key);
        return cryptographyService.encrypt(decryptedWalletFile);
    }

    private boolean validatePasswalletKey() {
        EditText password = findViewById(R.id.walletKey);
        return password.getText().toString().length() != 0;
    }

    private byte[] loadDefaultPasswalletFromTemplate() throws IOException {
        try (InputStream passwalletTemplate = getResources().getAssets().open("passwallet_xml_file_template.xml")) {
            return UriUtils.loadInputStreamToByteArray(passwalletTemplate);
        }
    }

    private void browseForEncryptedWalletFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, CREATE_REQUEST_CODE);
    }
}
