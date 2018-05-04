package ro.group305.passwalletandroidclient.dao;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ro.group305.passwallet.model.UserAccount;
import ro.group305.passwallet.model.dao.IUserAccountDAO;

import static java.util.stream.Collectors.toList;

/**
 * Created by emilu on 2/24/2018.
 */

public class XMLDao implements IUserAccountDAO{
    private List<UserAccount> accounts;

    public XMLDao(List<UserAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public UserAccount findUserAccountById(Integer integer) {

        return null;
    }

    @Override
    public Collection<UserAccount> findAllUsersAccounts() {
        return Collections.unmodifiableCollection(accounts);
    }

    @Override
    public Collection<UserAccount> findUsersAccountsByName(String text) {
        return null;
    }

    @Override
    public Integer createUserAccount(UserAccount userAccount) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean deleteUserAccountById(Integer integer) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean updateUserAccount(UserAccount userAccount) {
        throw new RuntimeException("Not implemented yet");
    }
}
