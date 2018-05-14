package ro.group305.passwalletandroidclient.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.model.dao.IUserAccountDAO;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class UserAccountXMLFileDAO implements IUserAccountDAO{
    private static final String TAG = "PassWallet";

    private List<UserAccount> userAccounts;
    private UserAccountXMLSerializer userAccountXMLSerializer = new UserAccountXMLSerializer();
    private Uri encryptedWalletFileURI;
    private ContentResolver contentResolver;
    private CryptographyService cryptographyService;

    public UserAccountXMLFileDAO(Uri encryptedWalletFileURI, ContentResolver contentResolver, CryptographyService cryptographyService) throws IOException, XmlPullParserException {
        this.encryptedWalletFileURI = encryptedWalletFileURI;
        this.contentResolver = contentResolver;
        this.cryptographyService = cryptographyService;
        byte[] decryptedWalletFile = cryptographyService.decrypt(UriUtils.getUriContent(this.encryptedWalletFileURI, this.contentResolver));
        userAccounts = Collections.synchronizedList(userAccountXMLSerializer.unmarshal(decryptedWalletFile));
    }

    public int getUserAccountsCount() {
        return userAccounts.size();
    }

    @Override
    public UserAccount findUserAccountById(Integer id) {
        for (UserAccount userAccount : userAccounts) {
            if (id == userAccount.getId()) {
                return userAccount;
            }
        }
        return null;
    }

    @Override
    public Collection<UserAccount> findAllUsersAccounts() {
        return Collections.unmodifiableList(userAccounts);
    }

    @Override
    public Collection<UserAccount> findUsersAccountsByName(String name) {
        throw new RuntimeException("Not implemented.");
    }

    public Integer createUserAccount(UserAccount userAccount) {
        synchronized (UserAccountXMLFileDAO.class) {
            Integer maxId = getMaxId();
            userAccount.setId(maxId);
            userAccounts.add(userAccount);
            try {
                byte[] newXmlContent = userAccountXMLSerializer.marshal(userAccounts);
                UriUtils.saveUriContent(this.encryptedWalletFileURI, this.contentResolver, this.cryptographyService.encrypt(newXmlContent));
                return maxId;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean deleteUserAccountById(Integer id) {
        synchronized (UserAccountXMLFileDAO.class) {
            for (UserAccount userAccount : userAccounts) {
                if (id.equals(userAccount.getId())) {
                    userAccounts.remove(userAccount);
                    try {
                        byte[] newXmlContent = userAccountXMLSerializer.marshal(userAccounts);
                        UriUtils.saveUriContent(this.encryptedWalletFileURI, this.contentResolver, this.cryptographyService.encrypt(newXmlContent));
                        return true;
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
            return false;
        }
    }

    public boolean updateUserAccount(UserAccount updatedUserAccount) {
        synchronized (UserAccountXMLFileDAO.class) {
            Integer id = updatedUserAccount.getId();
            for (UserAccount userAccount : userAccounts) {
                if (id.equals(userAccount.getId())) {
                    userAccount.setDescription(updatedUserAccount.getDescription());
                    userAccount.setPassword(updatedUserAccount.getPassword());
                    userAccount.setNickName(updatedUserAccount.getNickName());
                    userAccount.setName(updatedUserAccount.getName());
                    userAccount.setSiteURL(updatedUserAccount.getSiteURL());
                    try {
                        byte[] newXmlContent = userAccountXMLSerializer.marshal(userAccounts);
                        UriUtils.saveUriContent(this.encryptedWalletFileURI, this.contentResolver, this.cryptographyService.encrypt(newXmlContent));
                        return true;
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                }
            }

            return false;
        }
    }

    public List<UserAccount> getSortedUserAccounts(Comparator<UserAccount> comparator) {
        if (comparator == null) {
            return (List<UserAccount>) findAllUsersAccounts();
        }
        List<UserAccount> sortedUserAccounts = new ArrayList<>(userAccounts);
        Collections.sort(sortedUserAccounts, comparator);
        return Collections.unmodifiableList(sortedUserAccounts);
    }

    private Integer getMaxId() {
        Integer max = Integer.MIN_VALUE;
        for (UserAccount account : userAccounts) {
            if (account.getId() > max) {
                max = account.getId();
            }
        }
        return max + 1;
    }
}
