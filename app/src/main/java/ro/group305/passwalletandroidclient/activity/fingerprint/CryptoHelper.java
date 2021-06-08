package ro.group305.passwalletandroidclient.activity.fingerprint;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CryptoHelper {
    private static final String TAG = "PassWallet";
    private final KeyPreference keyPreference;

    public CryptoHelper(KeyPreference keyPreference) {
        Objects.requireNonNull(keyPreference);
        this.keyPreference = keyPreference;
    }

    public Cipher getCipher(SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        String encryptedPassword = keyPreference.getKeyFromPrefs();
        int encryptMode = Cipher.DECRYPT_MODE;
        if (encryptedPassword.length() == 0) {
            encryptMode = Cipher.ENCRYPT_MODE;
        }

        return getCipher(encryptMode, secretKey);
    }

    public boolean resetKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            keyStore.deleteEntry(keyPreference.getKeyPrefName());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public Cipher getCipher(int encryptMode, SecretKey secretKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        if (Cipher.DECRYPT_MODE == encryptMode) {
            cipher.init(encryptMode, secretKey, new IvParameterSpec(keyPreference.getKeyIvParameterFromPrefs()));
        } else {
            cipher.init(encryptMode, secretKey);
        }

        return cipher;
    }

    public SecretKey getSecretKey() throws KeyStoreException,
            CertificateException, NoSuchAlgorithmException,
            IOException, UnrecoverableKeyException,
            NoSuchProviderException,
            InvalidAlgorithmParameterException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        SecretKey secretKey;

        if (!keyStore.containsAlias(keyPreference.getKeyPrefName())) {
            secretKey = generateSecretKey();
        } else {
            secretKey = ((SecretKey) keyStore.getKey(keyPreference.getKeyPrefName(), null));
        }
        return secretKey;
    }

    public SecretKey generateSecretKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        keyGenerator.init(new KeyGenParameterSpec.Builder(
                Objects.requireNonNull(keyPreference.getKeyPrefName()),
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .setInvalidatedByBiometricEnrollment(true)
                .build());

        return keyGenerator.generateKey();
    }
}
