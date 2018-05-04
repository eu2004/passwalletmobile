package ro.group305.passwalletandroidclient;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ro.group305.passwallet.model.UserAccount;
import ro.group305.passwallet.service.crypt.CryptographyService;

/**
 * Created by emilu on 2/25/2018.
 */

public class ManagePassWallet extends AppCompatActivity {
    private static final String LOG_TAG = "ManagePassWallet";

    private List<UserAccount> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pass_wallet);

        try {
            Intent intent = getIntent();
            byte[] encryptedWalletFile = intent.getByteArrayExtra("encryptedWalletFile");
            String key = new String(intent.getByteArrayExtra("key"));
            byte[] decryptedWalletFile = decrypt(encryptedWalletFile, key);
            accounts = loadUsersAccounts(decryptedWalletFile);
            SearchView search = findViewById(R.id.search);
            ListView list_view = findViewById(R.id.list_view);

            UserAccountsAdapter userAccountsAdapter = new UserAccountsAdapter(this.getApplicationContext(),
                    R.layout.accounts_list_items, R.id.list_item, getAdapterData(accounts), new String[]{"nickName"});
            list_view.setAdapter(userAccountsAdapter);

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    userAccountsAdapter.getFilter().filter(newText);
                    return false;
                }
            });

            AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> selectedItem = (Map<String, String>) userAccountsAdapter.getItem(position);
                    if (selectedItem == null) {
                        return;
                    }
                    UserAccount userAccount = getUserAccountById(Integer.parseInt(selectedItem.get("id")));
                    copyInfoToClipboard(userAccount);
                }
            };
            list_view.setOnItemClickListener(itemListener);
        } catch (Exception exception) {
            Log.e(LOG_TAG, exception.getMessage(), exception);
        }
    }

    private void copyInfoToClipboard(UserAccount userAccount) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("user", userAccount.toString());
        clipboard.setPrimaryClip(clip);
    }

    private UserAccount getUserAccountById(long id) {
        for(UserAccount userAccount : accounts) {
            if (id == userAccount.getId()) {
                return userAccount;
            }
        }
        return null;
    }

    private List<Map<String,String>> getAdapterData(List<UserAccount> accounts) {
        List<Map<String,String>> data = new ArrayList<>();
        for (UserAccount userAccount : accounts) {
            data.add(transformUserAccountToMap(userAccount));
        }
        return data;
    }

    private Map<String,String> transformUserAccountToMap(UserAccount userAccount) {
        Map<String, String> data = new HashMap<>();
        data.put("id", userAccount.getId().toString().toLowerCase());
        data.put("description", userAccount.getDescription() == null ? "" : userAccount.getDescription().toLowerCase());
        data.put("name", userAccount.getName() == null ? "" : userAccount.getName().toLowerCase());
        data.put("nickName", userAccount.getNickName() == null ? "" : userAccount.getNickName().toLowerCase());
        data.put("pass", userAccount.getPassword() == null ? "" : userAccount.getPassword().toLowerCase());
        data.put("siteURL", userAccount.getSiteURL() == null ? "" : userAccount.getSiteURL().toLowerCase());
        return data;
    }

    private byte[] decrypt(byte[] encryptedWalletFile, String key) {
        CryptographyService cryptographyService = new CryptographyService(key);
        return cryptographyService.decrypt(encryptedWalletFile);
    }

    private List<UserAccount> loadUsersAccounts(byte[] decryptedWalletFile) throws XmlPullParserException, IOException {
        XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory.newInstance();
        XmlPullParser myParser = xmlFactoryObject.newPullParser();
        myParser.setInput(new ByteArrayInputStream(decryptedWalletFile), null);
        int event = myParser.getEventType();
        List<UserAccount> userAccounts = new ArrayList<>();
        UserAccount userAccount = null;
        while (event != XmlPullParser.END_DOCUMENT) {
            String name = myParser.getName();

            switch (event) {
                case XmlPullParser.START_TAG:
                    switch (name) {
                        case "userAccount":
                            userAccount = new UserAccount();
                            break;
                        case "description":
                            myParser.next();
                            userAccount.setDescription(myParser.getText());
                            break;
                        case "id":
                            myParser.next();
                            userAccount.setId(Integer.parseInt(myParser.getText()));
                            break;
                        case "name":
                            myParser.next();
                            userAccount.setName(myParser.getText());
                            break;
                        case "nickName":
                            myParser.next();
                            userAccount.setNickName(myParser.getText());
                            break;
                        case "password":
                            myParser.next();
                            userAccount.setPassword(myParser.getText());
                            break;
                        case "siteURL":
                            myParser.next();
                            userAccount.setSiteURL(myParser.getText());
                            break;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    switch (name) {
                        case "userAccount":
                            userAccounts.add(userAccount);
                    }
                    break;
            }
            event = myParser.next();
        }
        return userAccounts;
    }
}
