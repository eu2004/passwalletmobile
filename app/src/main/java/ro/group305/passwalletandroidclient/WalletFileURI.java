package ro.group305.passwalletandroidclient;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class WalletFileURI {
    private static final String TAG = "PassWallet";

    private Uri walletFileURI;
    private ContentResolver contentResolver;
    byte[] walletFileContent;

    public WalletFileURI(Uri walletFileURI, ContentResolver contentResolver) {
        this.walletFileURI = walletFileURI;
        this.contentResolver = contentResolver;
    }

    public synchronized byte[] getContent() {
        if (walletFileContent == null) {
            try (InputStream inputStream = contentResolver.openInputStream(walletFileURI)) {
                walletFileContent = loadEncryptedWalletFileContent(inputStream);
            } catch (IOException exception) {
                Log.e(TAG, exception.getMessage(), exception);
            }
        }
        return walletFileContent;
    }

    private byte[] loadEncryptedWalletFileContent(InputStream in) throws IOException {
        ByteArrayOutputStream xmlFileContent = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int count;
        while ((count = in.read(buffer)) != -1) {
            xmlFileContent.write(buffer, 0, count);
        }
        xmlFileContent.close();
        return xmlFileContent.toByteArray();
    }

    public static boolean isValid(Uri selectedWalletURI) {
        if (selectedWalletURI == null) {
            return false;
        }

        try {
            Uri.parse(selectedWalletURI.toString());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }

        return true;
    }
}
