package ro.group305.passwalletandroidclient;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Map;

import ro.group305.passwallet.model.UserAccount;
import ro.group305.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.utils.WalletFileURI;

/**
 * Created by emilu on 2/25/2018.
 */

public class ManagePassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pass_wallet);

        try {
            Intent intent = getIntent();
            String encryptedWalletFileURIStr = intent.getStringExtra("encryptedWalletFileURI");
            String key = new String(intent.getByteArrayExtra("key"));
            byte[] encryptedWalletFile = loadEncryptedWalletFileURI(encryptedWalletFileURIStr);
            createSearchView(new UserAccountDAO(decrypt(encryptedWalletFile, key)));
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage(), exception);
        }
    }

    private byte[] loadEncryptedWalletFileURI(String encryptedWalletFileURIStr) {
        return new WalletFileURI(Uri.parse(encryptedWalletFileURIStr), getContentResolver()).getContent();
    }

    private void createSearchView(UserAccountDAO userAccountDAO) {
        UserAccountsListAdapter userAccountsAdapter = new UserAccountsListAdapter(this.getApplicationContext(),
                R.layout.accounts_list_items, R.id.list_item, userAccountDAO.getUserAccounts(), new String[]{"nickName"});
        SearchView search = findViewById(R.id.search);
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

        createSearchListView(userAccountDAO, userAccountsAdapter);
    }

    private void createSearchListView(UserAccountDAO userAccountDAO, UserAccountsListAdapter userAccountsAdapter) {
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(userAccountsAdapter);
        AdapterView.OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, String> selectedItem = (Map<String, String>) userAccountsAdapter.getItem(position);
                if (selectedItem == null) {
                    return;
                }
                UserAccount userAccount = userAccountDAO.getUserAccountById(Integer.parseInt(selectedItem.get("id")));
                copyInfoToClipboard(userAccount);
            }

            private void copyInfoToClipboard(UserAccount userAccount) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("user", userAccount.toString());
                clipboard.setPrimaryClip(clip);
            }
        };
        listView.setOnItemClickListener(itemListener);
    }

    private byte[] decrypt(byte[] encryptedWalletFile, String key) {
        CryptographyService cryptographyService = new CryptographyService(key);
        return cryptographyService.decrypt(encryptedWalletFile);
    }
}
