package ro.group305.passwalletandroidclient.service;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ro.eu.passwallet.model.UserAccount;

class UserAccountXMLSerializer {

    public byte[] marshal(Collection<UserAccount> userAccounts) throws IOException {
        //https://www.ibm.com/developerworks/opensource/library/x-android/
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        serializer.setOutput(writer);
        serializer.startDocument("UTF-8", true);
        serializer.startTag("", "USERSACCOUNTS");
        for (UserAccount userAccount : userAccounts) {
            toXMLElement(serializer, userAccount);
        }
        serializer.endTag("", "USERSACCOUNTS");
        serializer.endDocument();
        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void toXMLElement(XmlSerializer serializer, UserAccount userAccount) throws IOException {
        serializer.startTag("", "userAccount");

        serializer.startTag("", "description");
        serializer.text(userAccount.getDescription());
        serializer.endTag("", "description");

        serializer.startTag("", "id");
        serializer.text(String.valueOf(userAccount.getId()));
        serializer.endTag("", "id");

        serializer.startTag("", "name");
        serializer.text(userAccount.getName());
        serializer.endTag("", "name");

        serializer.startTag("", "nickName");
        serializer.text(userAccount.getNickName());
        serializer.endTag("", "nickName");

        serializer.startTag("", "password");
        serializer.text(userAccount.getPassword());
        serializer.endTag("", "password");

        serializer.startTag("", "siteURL");
        serializer.text(userAccount.getSiteURL());
        serializer.endTag("", "siteURL");

        serializer.endTag("", "userAccount");
    }

    public Collection<UserAccount> unmarshal(byte[] decryptedWalletFile) throws XmlPullParserException, IOException {
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
                            userAccount.setDescription(getStringValue(myParser.getText()));
                            break;
                        case "id":
                            myParser.next();
                            userAccount.setId(Integer.parseInt(myParser.getText()));
                            break;
                        case "name":
                            myParser.next();
                            userAccount.setName(getStringValue(myParser.getText()));
                            break;
                        case "nickName":
                            myParser.next();
                            userAccount.setNickName(getStringValue(myParser.getText()));
                            break;
                        case "password":
                            myParser.next();
                            userAccount.setPassword(getStringValue(myParser.getText()));
                            break;
                        case "siteURL":
                            myParser.next();
                            userAccount.setSiteURL(getStringValue(myParser.getText()));
                            break;
                    }
                    break;

                case XmlPullParser.END_TAG:
                    if ("userAccount".equals(name)) {
                        userAccounts.add(userAccount);
                    }
                    break;
            }
            event = myParser.next();
        }
        return userAccounts;
    }

    private String getStringValue(String text) {
        if (text == null) {
            return "";
        }
        return text;
    }
}
