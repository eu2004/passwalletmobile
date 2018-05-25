package ro.group305.passwalletandroidclient.activity;

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
import android.widget.ListView;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.model.UserAccountXMLFileDAO;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;

public class ManagePassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";
    private static final int EDIT_ITEM_ACTION_RESULT = 0;
    private static final int ADD_ITEM_ACTION_RESULT = 1;
    private static final int VIEW_ITEM_ACTION_RESULT = 2;

    private UserAccountXMLFileDAO userAccountDAO;
    private CryptographyService cryptographyService;
    private UserAccountsListAdapter userAccountsAdapter;
    private Uri encryptedWalletFileURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pass_wallet);

        try {
            Intent intent = getIntent();
            encryptedWalletFileURI = Uri.parse(intent.getStringExtra("encryptedWalletFileURI"));
            String key = new String(intent.getByteArrayExtra("key"));
            cryptographyService = new CryptographyService(key);
            userAccountDAO = new UserAccountXMLFileDAO(encryptedWalletFileURI, getContentResolver(), cryptographyService);
            createSearchView();
            createAddButton();
            initAccountsCount();
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage(), exception);
            ActivityUtils.displayErrorMessage(this, "Error loading PassWallet", exception.getMessage());
            finish();
        }
    }

    private void initAccountsCount() {
        TextView accountsCount = findViewById(R.id.accounts_count_textView);
        accountsCount.setText(String.valueOf(userAccountDAO.getUserAccountsCount()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ADD_ITEM_ACTION_RESULT:
                onAddItemActionResult(resultCode, data);
                initAccountsCount();
                break;
            case EDIT_ITEM_ACTION_RESULT:
                onEditItemActionResult(resultCode, data);
                break;
            case VIEW_ITEM_ACTION_RESULT:
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_view) {
            menu.add("Copy");
            menu.add("View");
            menu.add("Edit");
            menu.add("Delete");
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = findViewById(R.id.list_view);
        Map<String, String> selectedItem = (Map<String, String>) listView.getItemAtPosition(menuInfo.position);
        UserAccount userAccount = userAccountDAO.findUserAccountById(Integer.parseInt(selectedItem.get("id")));

        if ("Copy".equals(item.getTitle())) {
            Log.d(TAG, "Copy " + userAccount.getNickName());
            copyInfoToClipboard(userAccount);
        } else if ("View".equals(item.getTitle())) {
            Log.d(TAG, "View ...");
            viewUserAccount(userAccount);
        }else if ("Edit".equals(item.getTitle())) {
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
                boolean updated = userAccountDAO.updateUserAccount(updatedUserAccount);
                if (updated) {
                    userAccountsAdapter.updateUserAccountsList();
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
                            boolean deleted = userAccountDAO.deleteUserAccountById(userAccount.getId());
                            if (deleted) {
                                userAccountsAdapter.updateUserAccountsList();
                                initAccountsCount();
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
                userAccountDAO.createUserAccount(newUserAccount);
                userAccountsAdapter.updateUserAccountsList();
            } catch (Exception exception) {
                Log.e(TAG, exception.getMessage(), exception);
                ActivityUtils.displayErrorMessage(this, "Error creating passwallet item", exception.getMessage());
            }
        }
    }

    private void addUserAccount() {
        Intent intent = new Intent(this, CreatePassWalletItemActivity.class);
        startActivityForResult(intent, ADD_ITEM_ACTION_RESULT);
    }

    private void viewUserAccount(UserAccount userAccount) {
        Intent intent = new Intent(this, ViewPassWalletItemActivity.class);
        intent.putExtra("selectedUserAccount", userAccount);
        startActivityForResult(intent, VIEW_ITEM_ACTION_RESULT);
    }

    private void editUserAccount(UserAccount userAccount) {
        Intent intent = new Intent(this, EditPassWalletItemActivity.class);
        intent.putExtra("selectedUserAccount", userAccount);
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
        userAccountsAdapter = new UserAccountsListAdapter(this,
                R.layout.accounts_list_items, R.id.list_item, userAccountDAO, new String[]{"nickName"});
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
