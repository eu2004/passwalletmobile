package ro.group305.passwalletandroidclient.activity.fingerprint;

import android.content.SharedPreferences;
import android.util.Base64;

import java.util.Arrays;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class KeyPreference {
    private final SharedPreferences sharedPref;
    private final String keyPrefName;

    public KeyPreference(SharedPreferences sharedPref, String keyPrefName) {
        Objects.requireNonNull(sharedPref, keyPrefName);
        this.sharedPref = sharedPref;
        this.keyPrefName = keyPrefName;
    }

    public boolean resetKey() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(keyPrefName, "");
        editor.apply();
        return true;
    }

    public String getKeyPrefName() {
        return keyPrefName;
    }

    public String getKeyFromPrefs() {
        return sharedPref.getString(keyPrefName, "");
    }

    public byte[] getKeyEncryptedPassFromPrefs() {
        String encryptedPassword = sharedPref.getString(keyPrefName, "");
        byte[] ivAndCipherText = Base64.decode(encryptedPassword, Base64.NO_WRAP);
        return Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.length);
    }

    public byte[] getKeyIvParameterFromPrefs() {
        String keyFromPrefs = getKeyFromPrefs();
        byte[] ivAndCipherText = Base64.decode(keyFromPrefs, Base64.NO_WRAP);
        return Arrays.copyOfRange(ivAndCipherText, 0, 16);
    }

    public void savePassToPrefs(Cipher encryptCipher, byte[] password) throws BadPaddingException, IllegalBlockSizeException {
        SharedPreferences.Editor editor = sharedPref.edit();
        byte[] cipherText = encryptCipher.doFinal(password);
        byte[] iv = encryptCipher.getIV();
        byte[] ivAndCipherText = getCombinedArray(iv, cipherText);
        editor.putString(keyPrefName, Base64.encodeToString(ivAndCipherText, Base64.NO_WRAP));
        editor.apply();
    }

    private static byte[] getCombinedArray(byte[] firstArray, byte[] secondArray) {
        byte[] result = new byte[firstArray.length + secondArray.length];
        System.arraycopy(firstArray, 0, result, 0, firstArray.length);
        System.arraycopy(secondArray, 0, result, firstArray.length, secondArray.length);
        return result;
    }
}
