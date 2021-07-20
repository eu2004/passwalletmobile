package ro.group305.passwalletandroidclient.activity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.Map;
import java.util.Objects;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.activity.resulthandler.CreatePassWalletItemActivityResult;
import ro.group305.passwalletandroidclient.activity.resulthandler.EditPassWalletItemActivityResult;
import ro.group305.passwalletandroidclient.model.UserAccountXmlUriDAO;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;

public class ManagePassWalletActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    private UserAccountXmlUriDAO userAccountDAO;
    private UserAccountsListAdapter userAccountsAdapter;

    private ActivityResultLauncher<String> addItemActionResult;
    private ActivityResultLauncher<UserAccount> editItemActionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pass_wallet);

        try {
            Intent intent = getIntent();
            Uri encryptedWalletFileURI = Uri.parse(intent.getStringExtra("encryptedWalletFileURI"));
            ActivityUtils.saveSelectedFileToPreferences(this, encryptedWalletFileURI);

            String key = new String(intent.getByteArrayExtra("key"));
            CryptographyService cryptographyService = new CryptographyService(key);
            userAccountDAO = new UserAccountXmlUriDAO(encryptedWalletFileURI, getContentResolver(), cryptographyService);
            createSearchView();
            createAddButton();
            initAccountsCount();

            addItemActionResult = registerForActivityResult(new CreatePassWalletItemActivityResult(this), output -> {
                onAddItemActionResult(output);
                initAccountsCount();
            });
            editItemActionResult = registerForActivityResult(new EditPassWalletItemActivityResult(this), this::onEditItemActionResult);
        } catch (Exception exception) {
            Log.e(TAG, exception.getMessage(), exception);
            ActivityUtils.displayErrorMessage(this, "Error loading PassWallet", exception.getMessage());
            finish();
        } finally {
            getIntent().removeExtra("key");
        }
    }

    private void initAccountsCount() {
        TextView accountsCount = findViewById(R.id.accounts_count_textView);
        accountsCount.setText(String.valueOf(userAccountDAO.getUserAccountsCount()));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list_view) {
            Resources res = getResources();
            menu.add(res.getString(R.string.copy));
            menu.add(res.getString(R.string.copyKey));
            menu.add(res.getString(R.string.view));
            menu.add(res.getString(R.string.edit));
            menu.add(res.getString(R.string.delete));
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ListView listView = findViewById(R.id.list_view);
        Map<String, String> selectedItem = (Map<String, String>) listView.getItemAtPosition(menuInfo.position);
        UserAccount userAccount = userAccountDAO.findUserAccountById(Integer.parseInt(Objects.requireNonNull(selectedItem.get("id"))));
        Resources res = getResources();
        if (res.getString(R.string.copy).contentEquals(item.getTitle())) {
            Log.d(TAG, userAccount.getNickName());
            copyInfoToClipboard(userAccount);
        } else if (res.getString(R.string.copyKey).contentEquals(item.getTitle())) {
            Log.d(TAG, userAccount.getNickName());
            copyPasswordToClipboard(userAccount);
        } else if (res.getString(R.string.view).contentEquals(item.getTitle())) {
            Log.d(TAG, "View ...");
            viewUserAccount(userAccount);
        } else if (res.getString(R.string.edit).contentEquals(item.getTitle())) {
            Log.d(TAG, userAccount.getNickName());
            editItemActionResult.launch(userAccount);
        } else if (res.getString(R.string.delete).contentEquals(item.getTitle())) {
            Log.d(TAG, userAccount.getNickName());
            deleteUserAccount(userAccount);
        }

        return super.onContextItemSelected(item);
    }

    private void onEditItemActionResult(Intent data) {
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

    private void deleteUserAccount(final UserAccount userAccount) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
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
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.confirmDeletePasswalletItem)).setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
    }

    private void onAddItemActionResult(Intent data) {
        if (data != null && data.hasExtra("newUserAccount")) {
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

    private void viewUserAccount(UserAccount userAccount) {
        Intent intent = new Intent(this, ViewPassWalletItemActivity.class);
        intent.putExtra("selectedUserAccount", userAccount);
        startActivity(intent);
    }

    private void copyPasswordToClipboard(UserAccount userAccount) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("key", userAccount.getPassword());
        clipboard.setPrimaryClip(clip);
    }

    private void copyInfoToClipboard(UserAccount userAccount) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("user", userAccount.toString());
        clipboard.setPrimaryClip(clip);
    }

    private void createAddButton() {
        Button addButton = findViewById(R.id.create_passwallet_item_button);
        addButton.setOnClickListener(v -> ManagePassWalletActivity.this.addItemActionResult.launch(""));
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
