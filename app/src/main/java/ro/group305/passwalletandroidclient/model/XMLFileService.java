package ro.group305.passwalletandroidclient.model;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ro.eu.passwallet.model.UserAccount;
import ro.eu.passwallet.service.crypt.CryptographyService;
import ro.eu.passwallet.service.xml.IXMLFileService;
import ro.group305.passwalletandroidclient.utils.UriUtils;

public class XMLFileService implements IXMLFileService {
    private static final String TAG = "PassWallet";
    private Uri encryptedWalletFileURI;
    private ContentResolver contentResolver;
    private CryptographyService cryptographyService;
    private UserAccountXMLSerializer userAccountXMLSerializer = new UserAccountXMLSerializer();

    public XMLFileService(Uri encryptedWalletFileURI, ContentResolver contentResolver, CryptographyService cryptographyService) {
        this.encryptedWalletFileURI = encryptedWalletFileURI;
        this.contentResolver = contentResolver;
        this.cryptographyService = cryptographyService;
    }

    @Override
    public void saveToXMLFile(Collection<UserAccount> userAccounts) {
        try {
            byte[] newXmlContent = userAccountXMLSerializer.marshal(userAccounts);
            UriUtils.saveUriContent(this.encryptedWalletFileURI, this.contentResolver, this.cryptographyService.encrypt(newXmlContent));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveXMLFile(byte[] newXmlContent) {
        try {
            UriUtils.saveUriContent(this.encryptedWalletFileURI, this.contentResolver, this.cryptographyService.encrypt(newXmlContent));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<UserAccount> getAllUsersAccountsFromXML() {
        try {
            byte[] decryptedWalletFile = cryptographyService.decrypt(UriUtils.getUriContent(this.encryptedWalletFileURI, this.contentResolver));
            return Collections.synchronizedList(new ArrayList<>(userAccountXMLSerializer.unmarshal(decryptedWalletFile)));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
