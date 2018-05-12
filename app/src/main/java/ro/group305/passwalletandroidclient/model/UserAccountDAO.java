package ro.group305.passwalletandroidclient.model;

import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ro.eu.passwallet.model.UserAccount;

public class UserAccountDAO {
    private static final String TAG = "PassWallet";

    private List<UserAccount> userAccounts;
    private UserAccountXMLSerializer userAccountXMLSerializer = new UserAccountXMLSerializer();

    public UserAccountDAO(byte[] decryptedWalletFile) throws IOException, XmlPullParserException {
        userAccounts = Collections.synchronizedList(userAccountXMLSerializer.unmarshal(decryptedWalletFile));
    }

    public UserAccount getUserAccountById(long id) {
        for (UserAccount userAccount : userAccounts) {
            if (id == userAccount.getId()) {
                return userAccount;
            }
        }
        return null;
    }

    public int getUserAccountsCount() {
        return userAccounts.size();
    }

    public byte[] createUserAccount(UserAccount userAccount) {
        synchronized (UserAccountDAO.class) {
            Integer maxId = getMaxId();
            userAccount.setId(maxId);
            userAccounts.add(userAccount);
            try {
                return userAccountXMLSerializer.marshal(userAccounts);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    public byte[] deleteUserAccount(UserAccount userAccount) {
        synchronized (UserAccountDAO.class) {
            if (userAccounts.remove(userAccount)) {
                try {
                    return userAccountXMLSerializer.marshal(userAccounts);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }

    public byte[] updateUserAccount(UserAccount updatedUserAccount) {
        synchronized (UserAccountDAO.class) {
            Integer id = updatedUserAccount.getId();
            UserAccount existingUserAccount = this.getUserAccountById(id);
            if (existingUserAccount != null) {
                int index = userAccounts.indexOf(existingUserAccount);
                if (index != -1) {
                    userAccounts.set(index, updatedUserAccount);
                    try {
                        return userAccountXMLSerializer.marshal(userAccounts);
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
            return null;
        }
    }

    public List<UserAccount> getUserAccounts() {
        return Collections.unmodifiableList(userAccounts);
    }

    public List<UserAccount> getSortedUserAccounts(Comparator<UserAccount> comparator) {
        if (comparator == null) {
            return getUserAccounts();
        }
        List<UserAccount> sortedUserAccounts = new ArrayList<>(userAccounts);
        Collections.sort(sortedUserAccounts, comparator);
        return Collections.unmodifiableList(sortedUserAccounts);
    }

    public Integer getMaxId() {
        synchronized (UserAccountDAO.class) {
            Integer max = Integer.MIN_VALUE;
            for (UserAccount account : userAccounts) {
                if (account.getId() > max) {
                    max = account.getId();
                }
            }
            return max + 1;
        }
    }
}
