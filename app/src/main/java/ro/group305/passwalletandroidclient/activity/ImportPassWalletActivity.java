package ro.group305.passwalletandroidclient.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.activity.resulthandler.CreateXmlFileActivityResult;
import ro.group305.passwalletandroidclient.activity.resulthandler.OpenXmlFileActivityResult;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class ImportPassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private ActivityResultLauncher<String> createXmlFieActivity;
    private ActivityResultLauncher<String> openXmlFieActivity;

    private void startManagePassWalletActivity(Uri selectedWalletURI) {
        Intent intent = new Intent(this, ManagePassWalletActivity.class);
        intent.putExtra("encryptedWalletFileURI", selectedWalletURI.toString());
        EditText password = findViewById(R.id.walletKey);
        intent.putExtra("key", password.getText().toString().getBytes());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_pass_wallet);
        createImportNotEncryptedPasswalletButton();
        createNotEncryptedLocalPasswalletButton();
        createHowToImportButton();

        createXmlFieActivity = registerForActivityResult(new CreateXmlFileActivityResult(), selectedWalletURI -> {
            try {
                UriUtils.saveUriContent(selectedWalletURI, this.getContentResolver(), loadDefaultPasswalletFromTemplate());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                ActivityUtils.displayErrorMessage(this, "Fatal Error", e.getMessage());
            }
        });

        openXmlFieActivity = registerForActivityResult(new OpenXmlFileActivityResult(), selectedWalletURI -> {
            try {
                EditText password = findViewById(R.id.walletKey);
                byte[] passwalletContent = UriUtils.getUriContent(selectedWalletURI, this.getContentResolver());
                CryptographyService cryptographyService = new CryptographyService(password.getText().toString());
                UriUtils.saveUriContent(selectedWalletURI, this.getContentResolver(), cryptographyService.encrypt(passwalletContent));
                ActivityUtils.saveSelectedFileToPreferences(this, selectedWalletURI);
                startManagePassWalletActivity(selectedWalletURI);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                ActivityUtils.displayErrorMessage(this, "Fatal Error", e.getMessage());
            }
        });
    }

    private void createHowToImportButton() {
        TextView howToImportButton = findViewById(R.id.how_to_import_button);
        howToImportButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(ImportPassWalletActivity.this, HowToImportPasswalletActivity.class);
                startActivity(intent);
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        });
    }

    private void createNotEncryptedLocalPasswalletButton() {
        TextView createLocalSamplePasswalletButton = findViewById(R.id.create_not_encrypted_local_passwallet_button);
        createLocalSamplePasswalletButton.setOnClickListener(v -> {
            try {
                createXmlFieActivity.launch("");
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        });
    }

    private byte[] loadDefaultPasswalletFromTemplate() throws IOException {
        try (InputStream passwalletTemplate = getResources().getAssets().open("passwallet_xml_file_template.xml")) {
            return UriUtils.loadInputStreamToByteArray(passwalletTemplate);
        }
    }

    private void createImportNotEncryptedPasswalletButton() {
        Button selectLocalPasswalletButton = findViewById(R.id.import_passwallet_button);
        selectLocalPasswalletButton.setOnClickListener(v -> {
            try {
                if (!validatePasswalletKey()) {
                    Log.e(TAG, "Passwallet key is empty!");
                    ActivityUtils.displayErrorMessage(ImportPassWalletActivity.this, "Error importing not encrypted wallet", "Key is empty!");
                    return;
                }
                openXmlFieActivity.launch("");
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        });
    }

    private boolean validatePasswalletKey() {
        EditText password = findViewById(R.id.walletKey);
        return password.getText().toString().length() != 0;
    }
}
