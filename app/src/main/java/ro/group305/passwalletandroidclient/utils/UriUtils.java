package ro.group305.passwalletandroidclient.utils;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UriUtils {
    private static final String TAG = "PassWallet";

    private UriUtils() {
    }

    public static byte[] loadInputStreamToByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream xmlFileContent = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int count;
        while ((count = in.read(buffer)) != -1) {
            xmlFileContent.write(buffer, 0, count);
        }
        xmlFileContent.close();
        return xmlFileContent.toByteArray();
    }

    public static byte[] getUriContent(Uri walletFileURI, ContentResolver contentResolver) throws IOException {
        try (InputStream inputStream = contentResolver.openInputStream(walletFileURI)) {
            return UriUtils.loadInputStreamToByteArray(inputStream);
        }
    }

    public static void saveUriContent(Uri walletFileURI, ContentResolver contentResolver, byte[] content) throws IOException {
        try (ParcelFileDescriptor parcelFileDescriptor =
                     contentResolver.
                             openFileDescriptor(walletFileURI, "w")) {
            try (FileOutputStream fileOutputStream =
                         new FileOutputStream(parcelFileDescriptor.getFileDescriptor())) {
                fileOutputStream.write(content);
            }
        }
    }

    public static boolean isUriValid(Uri selectedWalletURI) {
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
