package ro.group305.passwalletandroidclient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ro.eu.passwallet.model.UserAccount;

public class UserAccountDAO {
    private static final String TAG = "PassWallet";

    private List<UserAccount> userAccounts;

    public UserAccountDAO(byte[] decryptedWalletFile) throws IOException, XmlPullParserException {
        userAccounts = loadUsersAccounts(decryptedWalletFile);
    }

    public UserAccount getUserAccountById(long id) {
        for (UserAccount userAccount : userAccounts) {
            if (id == userAccount.getId()) {
                return userAccount;
            }
        }
        return null;
    }

    public byte[] createUserAccount(UserAccount userAccount) throws UnsupportedEncodingException {
        Integer maxId = getMaxId();
        synchronized (userAccounts) {
            userAccount.setId(maxId);
            userAccounts.add(userAccount);
            return toByteArray();
        }
    }

    public byte[] deleteUserAccount(UserAccount userAccount) throws UnsupportedEncodingException {
        synchronized (userAccounts) {
            if (userAccounts.remove(userAccount)) {
                return toByteArray();
            }
            return null;
        }
    }

    public byte[] updateUserAccount(UserAccount updatedUserAccount) throws UnsupportedEncodingException {
        synchronized (userAccounts) {
            Integer id = updatedUserAccount.getId();
            UserAccount existingUserAccount = this.getUserAccountById(id);
            if (existingUserAccount != null) {
                int index = userAccounts.indexOf(existingUserAccount);
                if (index != -1) {
                    userAccounts.set(index, updatedUserAccount);
                    return toByteArray();
                }
            }
            return null;
        }
    }

    public List<UserAccount> getUserAccounts() {
        return Collections.unmodifiableList(userAccounts);
    }

    private byte[] toByteArray() throws UnsupportedEncodingException {
        StringBuilder xmlContent = new StringBuilder();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        xmlContent.append("<USERSACCOUNTS>");
        for (UserAccount userAccount : userAccounts) {
            toXMLElement(xmlContent, userAccount);
        }
        xmlContent.append("</USERSACCOUNTS>");
        return xmlContent.toString().getBytes("UTF-8");
    }

    private void toXMLElement(StringBuilder xmlContent, UserAccount userAccount) {
        xmlContent.append("<userAccount>");
        xmlContent.append("<description>");
        xmlContent.append(userAccount.getDescription());
        xmlContent.append("</description>");
        xmlContent.append("<id>");
        xmlContent.append(userAccount.getId());
        xmlContent.append("</id>");
        xmlContent.append("<name>");
        xmlContent.append(userAccount.getName());
        xmlContent.append("</name>");
        xmlContent.append("<nickName>");
        xmlContent.append(userAccount.getNickName());
        xmlContent.append("</nickName>");
        xmlContent.append("<password>");
        xmlContent.append(userAccount.getPassword());
        xmlContent.append("</password>");
        xmlContent.append("<siteURL>");
        xmlContent.append(userAccount.getSiteURL());
        xmlContent.append("</siteURL>");
        xmlContent.append("</userAccount>");
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

    public Integer getMaxId() {
        synchronized (userAccounts) {
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
