package ro.group305.passwalletandroidclient.model;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.model.dao.IUserAccountDAO;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.group305.passwalletandroidclient.service.LoggerService;
import ro.group305.passwalletandroidclient.service.XMLFileService;

public class UserAccountXmlUriDAO implements IUserAccountDAO {
    private static final String TAG = "PassWallet";

    private ro.eu.passwallet.model.dao.UserAccountXMLDAO coreUserAccountXMLDAO;
    private List<UserAccount> userAccounts;

    public UserAccountXmlUriDAO(Uri encryptedWalletFileURI, ContentResolver contentResolver, CryptographyService cryptographyService) {
        coreUserAccountXMLDAO = new ro.eu.passwallet.model.dao.UserAccountXMLDAO(new XMLFileService(encryptedWalletFileURI, contentResolver, cryptographyService), new LoggerService(TAG));
        userAccounts = new ArrayList<>(coreUserAccountXMLDAO.findAllUsersAccounts());
    }

    public int getUserAccountsCount() {
        return userAccounts.size();
    }

    @Override
    public UserAccount findUserAccountById(Integer id) {
        return coreUserAccountXMLDAO.findUserAccountById(id);
    }

    @Override
    public Collection<UserAccount> findAllUsersAccounts() {
        return coreUserAccountXMLDAO.findAllUsersAccounts();
    }

    @Override
    public Collection<UserAccount> findUsersAccountsByName(String name) {
        return coreUserAccountXMLDAO.findUsersAccountsByName(name);
    }

    public Integer createUserAccount(UserAccount userAccount) {
        Integer id = coreUserAccountXMLDAO.createUserAccount(userAccount);
        userAccounts = new ArrayList<>(coreUserAccountXMLDAO.findAllUsersAccounts());
        return id;
    }

    @Override
    public boolean deleteUserAccountById(Integer id) {
        boolean deleted = coreUserAccountXMLDAO.deleteUserAccountById(id);
        userAccounts = new ArrayList<>(coreUserAccountXMLDAO.findAllUsersAccounts());
        return deleted;
    }

    public boolean updateUserAccount(UserAccount updatedUserAccount) {
        boolean updated = coreUserAccountXMLDAO.updateUserAccount(updatedUserAccount);
        userAccounts = new ArrayList<>(coreUserAccountXMLDAO.findAllUsersAccounts());
        return updated;
    }

    public List<UserAccount> getSortedUserAccounts(Comparator<UserAccount> comparator) {
        if (comparator == null) {
            return userAccounts;
        }
        List<UserAccount> sortedUserAccounts = new ArrayList<>(userAccounts);
        Collections.sort(sortedUserAccounts, comparator);
        return Collections.unmodifiableList(sortedUserAccounts);
    }
}
