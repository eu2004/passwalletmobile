package ro.group305.passwalletandroidclient.service;

import android.util.Log;

import ro.eu.passwallet.service.ILoggerService;
import ro.eu.passwallet.service.xml.XMLFileServiceException;

public class LoggerService implements ILoggerService {
    private final String TAG;

    public LoggerService(String tag) {
        this.TAG = tag;
    }

    @Override
    public void severe(String message, XMLFileServiceException e) {
        Log.e(TAG, message, e);
    }

    @Override
    public void severe(String message) {
        Log.e(TAG, message);
    }
}
