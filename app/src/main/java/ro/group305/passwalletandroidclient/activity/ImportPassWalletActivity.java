package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class ImportPassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private static final int IMPORT_REQUEST_CODE = 1;
    private static final int CREATE_REQUEST_CODE = 2;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CREATE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri selectedWalletURI = data.getData();
                        try {
                            UriUtils.saveUriContent(selectedWalletURI, this.getContentResolver(), loadDefaultPasswalletFromTemplate());
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                            ActivityUtils.displayErrorMessage(this, "Fatal Error", "Error saving not encrypted passwallet file:" + e.getMessage());
                            return;
                        }
                    } else {
                        Log.e(TAG, "Data is null, resultCode " + resultCode);
                    }
                }
                break;
            case IMPORT_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Uri selectedWalletURI = data.getData();
                        try {
                            EditText password = findViewById(R.id.walletKey);
                            byte[] passwalletContent = UriUtils.getUriContent(selectedWalletURI, this.getContentResolver());
                            CryptographyService cryptographyService = new CryptographyService(password.getText().toString());
                            UriUtils.saveUriContent(selectedWalletURI, this.getContentResolver(), cryptographyService.encrypt(passwalletContent));
                            ActivityUtils.saveSelectedFileToPreferences(this, selectedWalletURI);
                            startManagePassWalletActivity(selectedWalletURI);
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage(), e);
                            ActivityUtils.displayErrorMessage(this, "Fatal Error", "Error saving passwallet file:" + e.getMessage());
                            return;
                        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_pass_wallet);
        createImportNotEncryptedPasswalletButton();
        createNotEncryptedLocalPasswalletButton();
        createHowToImportButton();
    }

    private void createHowToImportButton() {
        TextView howToImportButton = findViewById(R.id.how_to_import_button);
        howToImportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(ImportPassWalletActivity.this, HowToImportPasswalletActivity.class);
                    startActivity(intent);
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        });
    }

    private void createNotEncryptedLocalPasswalletButton() {
        TextView createLocalSamplePasswalletButton = findViewById(R.id.create_not_encrypted_local_passwallet_button);
        createLocalSamplePasswalletButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    createCreateNotEncryptedLocalWalletFile();
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        });
    }

    private void createCreateNotEncryptedLocalWalletFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, CREATE_REQUEST_CODE);
    }

    private byte[] loadDefaultPasswalletFromTemplate() throws IOException {
        try (InputStream passwalletTemplate = getResources().getAssets().open("passwallet_xml_file_template.xml")) {
            return UriUtils.loadInputStreamToByteArray(passwalletTemplate);
        }
    }

    private void createImportNotEncryptedPasswalletButton() {
        Button selectLocalPasswalletButton = findViewById(R.id.import_passwallet_button);
        selectLocalPasswalletButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (!validatePasswalletKey()) {
                        Log.e(TAG, "Passwallet key is empty!");
                        ActivityUtils.displayErrorMessage(ImportPassWalletActivity.this, "Error importing not encrypted wallet", "Key is empty!");
                        return;
                    }
                    browseForNotEncryptedWalletFile();
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        });
    }

    private void browseForNotEncryptedWalletFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/xml");
        startActivityForResult(intent, IMPORT_REQUEST_CODE);
    }

    private boolean validatePasswalletKey() {
        EditText password = findViewById(R.id.walletKey);
        return password.getText().toString().length() != 0;
    }
}
