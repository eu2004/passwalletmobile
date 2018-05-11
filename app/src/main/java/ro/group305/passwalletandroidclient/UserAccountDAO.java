package ro.group305.passwalletandroidclient;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ro.group305.passwallet.model.UserAccount;

public class UserAccountDAO {
    private static final String TAG = "PassWallet";

    private List<UserAccount> userAccounts;

    public UserAccountDAO(byte[] decryptedWalletFile) throws IOException, XmlPullParserException {
        userAccounts = loadUsersAccounts(decryptedWalletFile);
    }

    public UserAccount getUserAccountById(long id) {
        for(UserAccount userAccount : userAccounts) {
            if (id == userAccount.getId()) {
                return userAccount;
            }
        }
        return null;
    }

    public List<UserAccount> getUserAccounts() {
        return Collections.unmodifiableList(userAccounts);
    }

    private List<UserAccount> loadUsersAccounts(byte[] decryptedWalletFile) throws XmlPullParserException, IOException {
        Log.i(TAG, new String(decryptedWalletFile));

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
