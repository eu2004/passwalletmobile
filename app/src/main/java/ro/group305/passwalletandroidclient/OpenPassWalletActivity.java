package ro.group305.passwalletandroidclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenPassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private static final int READ_REQUEST_CODE = 42;
    private Uri selectedWalletURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_pass_wallet);

        final Button selectLocalPasswalletButton = findViewById(R.id.select_local_passwallet_button);
        selectLocalPasswalletButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    browseForEncryptedWalletFile();
                } catch (Exception exception) {
                    Log.e(TAG, exception.getMessage(), exception);
                }
            }
        });

        final Button openSelectedPasswalletButton = findViewById(R.id.open_passwallet_button);
        openSelectedPasswalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedWalletURI);
                    loadEncryptedWalletFile(inputStream);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "File not found: " + e.getMessage(), e);
                }
            }
        });

        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
        String lastWalletURI = getPreferences(Context.MODE_PRIVATE).getString("selectedWalletURI", "");
        selectedWalletName.setText(lastWalletURI);
        if (lastWalletURI.length() > 0) {
            selectedWalletURI = Uri.parse(lastWalletURI);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case READ_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        selectedWalletURI = data.getData();
                        Log.i(TAG, "Uri: " + selectedWalletURI.toString());
                        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("selectedWalletURI", selectedWalletURI.toString());
                        editor.commit();
                        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
                        selectedWalletName.setText(selectedWalletURI.toString());
                    } else {
                        Log.e(TAG, "Data is null, resultCode " + resultCode);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void browseForEncryptedWalletFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xml");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void loadEncryptedWalletFile(InputStream is) {
        byte[] encryptedWalletFile = null;
        try {
            encryptedWalletFile = loadEncryptedWalletFileContent(is);
        } catch (IOException exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }

        Intent intent = new Intent(OpenPassWalletActivity.this, ManagePassWalletActivity.class);
        intent.putExtra("encryptedWalletFile", encryptedWalletFile);
        EditText password = findViewById(R.id.walletKey);
        intent.putExtra("key", password.getText().toString().getBytes());
        startActivity(intent);
    }

    private byte[] loadEncryptedWalletFileContent(InputStream in) throws IOException {
        List<Byte> xmlFileContent = new ArrayList<>();
        byte[] buffer = new byte[256];
        int count = -1;
        while ((count = in.read(buffer)) != -1) {
            for (int i = 0; i < count; i++) {
                xmlFileContent.add(buffer[i]);
            }
        }
        Byte[] encryptedArray = xmlFileContent.toArray(new Byte[]{});
        byte[] encrypted = new byte[encryptedArray.length];
        int i = 0;
        for (byte e : encryptedArray) {
            encrypted[i++] = e;
        }
        return encrypted;
    }
}
