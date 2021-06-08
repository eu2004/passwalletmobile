package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.activity.fingerprint.CryptoHelper;
import ro.group305.passwalletandroidclient.activity.fingerprint.KeyPreference;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class OpenPassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private static final int READ_REQUEST_CODE = 42;
    private Uri selectedPassWalletURI;
    private TextView selectedPassWalletName;
    private KeyPreference keyPreference;
    private CryptoHelper cryptoHelper;
    private boolean biometricFeatureActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pass_wallet);
        selectedPassWalletName = findViewById(R.id.selected_wallet_name_textView);

        createBrowsePasswalletButton();
        createOpenSelectedPasswalletButton();
        creatOpenPasswalletFingerprintButton();
        createCreateNewPasswalletLink();
        createImportPasswalletLink();

        initSelectedWalletURI();
    }

    private void checkBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.i(TAG, "App can authenticate using biometrics.");
                biometricFeatureActive = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "BIOMETRIC_ERROR_NONE_ENROLLED");
                break;
        }

    }

    private void promptBiometricFingerprint() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException {
        //https://developer.android.com/training/sign-in/biometric-auth
        BiometricPrompt biometricPrompt = createBiometricPrompt();

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        Cipher cipher = getCryptoHelper().getCipher(cryptoHelper.getSecretKey());
        biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
    }

    private KeyPreference getKeyPreference() {
        if (keyPreference == null) {
            keyPreference = new KeyPreference(OpenPassWalletActivity.this.getPreferences(Context.MODE_PRIVATE), selectedPassWalletURI.getPath());
        }
        return keyPreference;
    }

    private CryptoHelper getCryptoHelper() {
        if (cryptoHelper == null) {
            cryptoHelper = new CryptoHelper(getKeyPreference());
        }
        return cryptoHelper;
    }

    private BiometricPrompt createBiometricPrompt() {
        return new BiometricPrompt(OpenPassWalletActivity.this,
                ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                byte[] password;
                try {
                    password = getPasswordForFingerPrint(Objects.requireNonNull(result.getCryptoObject()).getCipher());
                    if (password == null) {
                        Toast.makeText(getApplicationContext(), "Authentication failed! " +
                                        "No password seems to be registered with the finger print: " +
                                        "Try to fill-in the password field with it, and try again.",
                                Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                } catch (GeneralSecurityException e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(getApplicationContext(), e.getMessage(),
                            Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                //if all good, proceed to search list
                goToSearchList(password);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        errString, Toast.LENGTH_LONG)
                        .show();
            }
        });
    }

    private byte[] getPasswordForFingerPrint(Cipher cipher) throws BadPaddingException, IllegalBlockSizeException {
        String keyFromPrefs = getKeyPreference().getKeyFromPrefs();
        if (keyFromPrefs.length() == 0) {
            EditText password = findViewById(R.id.walletKey);
            boolean succeeded = registerPasswordForFingerPrint(cipher, password.getText().toString());
            if (!succeeded) {
                return null;
            }
            return password.getText().toString().getBytes(StandardCharsets.UTF_8);
        } else {
            //decrypt pass
            return cipher.doFinal(getKeyPreference().getKeyEncryptedPassFromPrefs());
        }
    }

    private boolean registerPasswordForFingerPrint(Cipher encryptCipher, String password) throws BadPaddingException, IllegalBlockSizeException {
        if (password.length() == 0) {
            return false;
        }

        try {
            //validate decryption
            new CryptographyService(password).decrypt(UriUtils.getUriContent(selectedPassWalletURI, getContentResolver()));
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage(), exception);
            Toast.makeText(getApplicationContext(), "Authentication failed",
                    Toast.LENGTH_LONG)
                    .show();
            return false;
        }

        //save encrypted pass
        getKeyPreference().savePassToPrefs(encryptCipher, password.getBytes(StandardCharsets.UTF_8));
        return true;
    }

    private boolean resetKey() {
        return getCryptoHelper().resetKey() && getKeyPreference().resetKey();
    }

    private void createCreateNewPasswalletLink() {
        TextView createNewPasswalletTextView = findViewById(R.id.create_new_passwallet_textview);
        createNewPasswalletTextView.setOnClickListener(v -> {
            Log.d(TAG, "Create new passwallet");
            Intent intent = new Intent(OpenPassWalletActivity.this, CreatePassWalletActivity.class);
            startActivity(intent);
        });
    }

    private void createImportPasswalletLink() {
        TextView createNewPasswalletTextView = findViewById(R.id.import_passwallet_textview);
        createNewPasswalletTextView.setOnClickListener(v -> {
            Log.d(TAG, "Import passwallet");
            Intent intent = new Intent(OpenPassWalletActivity.this, ImportPassWalletActivity.class);
            startActivity(intent);
        });
    }

    private void initSelectedWalletURI() {
        String lastWalletURI = ActivityUtils.loadLastSelectedFile(this);
        if (lastWalletURI != null && lastWalletURI.trim().length() > 0) {
            selectedPassWalletURI = Uri.parse(lastWalletURI.trim());
            setSelectedPassWalletNameLabel();
        }
    }

    private void setSelectedPassWalletNameLabel() {
        Cursor returnCursor;
        try {
            returnCursor =
                    getContentResolver().query(selectedPassWalletURI, null, null, null, null);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return;
        }

        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if (nameIndex > -1) {
            returnCursor.moveToFirst();
            String name = returnCursor.getString(nameIndex);
            selectedPassWalletName.setText(ActivityUtils.appendStrings(name, " [", selectedPassWalletURI.getPath(), "]"));
            returnCursor.close();
        } else {
            selectedPassWalletName.setText(ActivityUtils.appendStrings(" [", selectedPassWalletURI.getPath(), "]"));
        }

        keyPreference = null;
        cryptoHelper = null;
    }

    private void creatOpenPasswalletFingerprintButton() {
        checkBiometric();

        Button openSelectedPasswalletButton = findViewById(R.id.open_passwallet_fingerprint_button);
        openSelectedPasswalletButton.setOnClickListener(v -> {
            if (!biometricFeatureActive) {
                Toast.makeText(getApplicationContext(), "Biometric feature not available on this device!",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            if (selectedPassWalletURI == null) {
                Toast.makeText(getApplicationContext(), "Passwallet file not selected! Please browse to a valid one first.",
                        Toast.LENGTH_LONG)
                        .show();
                return;
            }

            try {
                promptBiometricFingerprint();
            } catch (GeneralSecurityException | IOException ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
        });

        openSelectedPasswalletButton.setOnLongClickListener(v -> {
            if (!biometricFeatureActive) {
                Toast.makeText(getApplicationContext(), "Biometric feature not available on this device!",
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }

            if (selectedPassWalletURI == null) {
                Toast.makeText(getApplicationContext(), "Passwallet file not selected! Please browse to a valid one first.",
                        Toast.LENGTH_LONG)
                        .show();
                return false;
            }

            boolean result = resetKey();
            if (result) {
                Toast.makeText(getApplicationContext(), "Finger print authentication reset!" +
                                "To enable it again, please fill-in the password field and authenticate again.",
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Finger print authentication failed to reset! Please try again or check the logs.",
                        Toast.LENGTH_LONG)
                        .show();
            }
            return result;
        });
    }

    private void createOpenSelectedPasswalletButton() {
        Button openSelectedPasswalletButton = findViewById(R.id.open_passwallet_button);
        openSelectedPasswalletButton.setOnClickListener(v -> {
            //validate selected URI
            if (!UriUtils.isUriValid(selectedPassWalletURI)) {
                Log.e(TAG, "No passwallet file is selected or file path is incorrect!");
                ActivityUtils.displayErrorMessage(OpenPassWalletActivity.this, "Error opening passwallet", "No \"passwallet\" file is selected or file path is incorrect!");
                return;
            }

            //validate key
            EditText password = findViewById(R.id.walletKey);
            if (password.getText().toString().length() == 0) {
                password.setError("Key is mandatory!");
                return;
            }

            try {
                //validate decryption
                new CryptographyService(password.getText().toString()).decrypt(UriUtils.getUriContent(selectedPassWalletURI, getContentResolver()));
            } catch (Exception exception) {
                ActivityUtils.displayErrorMessage(OpenPassWalletActivity.this, "Error opening passwallet", exception.getMessage());
                return;
            }

            //go to search list
            goToSearchList(password.getText().toString().getBytes(StandardCharsets.UTF_8));
        });
    }

    private void goToSearchList(byte[] password) {
        Intent intent = new Intent(OpenPassWalletActivity.this, ManagePassWalletActivity.class);
        intent.putExtra("encryptedWalletFileURI", selectedPassWalletURI.toString());
        intent.putExtra("key", password);
        startActivity(intent);
    }

    private void createBrowsePasswalletButton() {
        Button selectLocalPasswalletButton = findViewById(R.id.browse_passwallet_file_button);
        selectLocalPasswalletButton.setOnClickListener(v -> {
            try {
                browseForEncryptedWalletFile();
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    selectedPassWalletURI = data.getData();
                    assert selectedPassWalletURI != null;
                    Log.i(TAG, selectedPassWalletURI.toString());
                    ActivityUtils.saveSelectedFileToPreferences(this, selectedPassWalletURI);
                    setSelectedPassWalletNameLabel();
                } else {
                    Log.e(TAG, ActivityUtils.appendStrings("Data is null, resultCode ", String.valueOf(resultCode)));
                }
            }
        }
    }

    private void browseForEncryptedWalletFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
