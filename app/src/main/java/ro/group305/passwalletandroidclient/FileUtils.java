package ro.group305.passwalletandroidclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    private FileUtils() {
    }

    public static byte[] loadFileContentFromStream(InputStream in) throws IOException {
        ByteArrayOutputStream xmlFileContent = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int count;
        while ((count = in.read(buffer)) != -1) {
            xmlFileContent.write(buffer, 0, count);
        }
        xmlFileContent.close();
        return xmlFileContent.toByteArray();
    }
}