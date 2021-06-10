package ro.group305.passwalletandroidclient.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.activity.resulthandler.CreateXmlFileActivityResult;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class CreatePassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private byte[] encryptedDefaultPasswalletContent;
    private ActivityResultLauncher<String> createXmlFieActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pass_wallet);
        createBrowsePasswalletButton();

        createXmlFieActivity = registerForActivityResult(new CreateXmlFileActivityResult(), selectedWalletURI -> {
            if (selectedWalletURI != null) {
                try {
                    UriUtils.saveUriContent(selectedWalletURI, this.getContentResolver(), encryptedDefaultPasswalletContent);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    ActivityUtils.displayErrorMessage(this, "Fatal Error", e.getMessage());
                    return;
                }
                ActivityUtils.saveSelectedFileToPreferences(this, selectedWalletURI);
                startManagePassWalletActivity(selectedWalletURI);
            }
        });
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
        selectLocalPasswalletButton.setOnClickListener(v -> {
            try {
                if (!validatePasswalletKey()) {
                    Log.e(TAG, "Passwallet key is empty!");
                    ActivityUtils.displayErrorMessage(CreatePassWalletActivity.this, "Error creating wallet", "Key is empty!");
                    return;
                }
                byte[] defaultPasswallet = loadDefaultPasswalletFromTemplate();
                EditText password = findViewById(R.id.walletKey);
                encryptedDefaultPasswalletContent = encrypt(defaultPasswallet, password.getText().toString());
                createXmlFieActivity.launch("");
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
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
}
