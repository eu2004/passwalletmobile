package ro.group305.passwalletandroidclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.Serializable;
import java.util.Map;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;
import ro.group305.passwalletandroidclient.utils.WalletFileURI;

/**
 * Created by emilu on 2/25/2018.
 */

public class ManagePassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";
    private static final int EDIT_ITEM_ACTION_RESULT = 0;
    private static final int ADD_ITEM_ACTION_RESULT = 1;

    private UserAccountDAO userAccountDAO;
    private WalletFileURI walletFileURI;
    private CryptographyService cryptographyService;

    private UserAccountsListAdapter userAccountsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pass_wallet);

        try {
            Intent intent = getIntent();
            String encryptedWalletFileURIStr = intent.getStringExtra("encryptedWalletFileURI");
            String key = new String(intent.getByteArrayExtra("key"));
            cryptographyService = new CryptographyService(key);
            walletFileURI = new WalletFileURI(Uri.parse(encryptedWalletFileURIStr), getContentResolver());
            userAccountDAO = new UserAccountDAO(cryptographyService.decrypt(walletFileURI.getContent()));
            createSearchView();
            createAddButton();
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage(), exception);
            ActivityUtils.displayErrorMessage(this, "Error loading wallet", exception.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_ITEM_ACTION_RESULT:
                onAddItemActionResult(resultCode, data);
                break;
            case EDIT_ITEM_ACTION_RESULT:
                onEditItemActionResult(resultCode, data);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_view) {
            menu.add("Copy");
            menu.add("Edit");
            menu.add("Delete");
            menu.add("Add New");
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = findViewById(R.id.list_view);
        Map<String, String> selectedItem = (Map<String, String>) listView.getItemAtPosition(menuInfo.position);
        UserAccount userAccount = userAccountDAO.getUserAccountById(Integer.parseInt(selectedItem.get("id")));

        if ("Copy".equals(item.getTitle())) {
            Log.d(TAG, "Copy " + userAccount.getNickName());
            copyInfoToClipboard(userAccount);
        } else if ("Edit".equals(item.getTitle())) {
            Log.d(TAG, "Edit " + userAccount.getNickName());
            editUserAccount(userAccount);
        } else if ("Delete".equals(item.getTitle())) {
            Log.d(TAG, "Delete " + userAccount.getNickName());
            deleteUserAccount(userAccount);
        } else if ("Add New".equals(item.getTitle())) {
            Log.d(TAG, "Add New ...");
            addUserAccount();
        }

        return super.onContextItemSelected(item);
    }

    private void onEditItemActionResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserAccount updatedUserAccount = (UserAccount) data.getSerializableExtra("updatedUserAccount");
            try {
                byte[] newContent = userAccountDAO.updateUserAccount(updatedUserAccount);
                if (newContent != null) {
                    walletFileURI.saveFileContent(cryptographyService.encrypt(newContent));
                    userAccountsAdapter.updateUserAccountsList(userAccountDAO.getUserAccounts());
                }
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
                ActivityUtils.displayErrorMessage(this, "Error updating passwallet item", exception.getMessage());
            }
        }
    }

    private void deleteUserAccount(UserAccount userAccount) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        try {
                            byte[] newContent = userAccountDAO.deleteUserAccount(userAccount);
                            if (newContent != null) {
                                walletFileURI.saveFileContent(cryptographyService.encrypt(newContent));
                                userAccountsAdapter.updateUserAccountsList(userAccountDAO.getUserAccounts());
                            }
                        } catch (Exception exception) {
                            Log.e(TAG, exception.getMessage(), exception);
                            ActivityUtils.displayErrorMessage(ManagePassWalletActivity.this, "Error creating passwallet item", exception.getMessage());
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to delete this entry?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void onAddItemActionResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            UserAccount newUserAccount = (UserAccount) data.getSerializableExtra("newUserAccount");
            try {
                byte[] newContent = userAccountDAO.createUserAccount(newUserAccount);
                if (newContent != null) {
                    walletFileURI.saveFileContent(cryptographyService.encrypt(newContent));
                    userAccountsAdapter.updateUserAccountsList(userAccountDAO.getUserAccounts());
                }
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
                ActivityUtils.displayErrorMessage(this, "Error creating passwallet item", exception.getMessage());
            }
        }
    }

    private void addUserAccount() {
        Intent intent = new Intent(this, AddPassWalletItemActivity.class);
        startActivityForResult(intent, ADD_ITEM_ACTION_RESULT);
    }

    private void editUserAccount(UserAccount userAccount) {
        Intent intent = new Intent(this, EditPassWalletItemActivity.class);
        intent.putExtra("selectedUserAccount", (Serializable) userAccount);
        startActivityForResult(intent, EDIT_ITEM_ACTION_RESULT);
    }

    private void copyInfoToClipboard(UserAccount userAccount) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("user", userAccount.toString());
        clipboard.setPrimaryClip(clip);
    }

    private void createAddButton() {
        Button addButton = findViewById(R.id.create_passwallet_item_button);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ManagePassWalletActivity.this.addUserAccount();
            }
        });
    }

    private void createSearchView() {
        userAccountsAdapter = new UserAccountsListAdapter(this.getApplicationContext(),
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

        createSearchListView();
    }

    private void createSearchListView() {
        ListView listView = findViewById(R.id.list_view);
        this.registerForContextMenu(listView);
        listView.setAdapter(userAccountsAdapter);
        listView.setLongClickable(true);
    }
}
