package ro.group305.passwalletandroidclient;

public class _OldOpenPassWallet  {
//    private static final String TAG = "OpenPassWallet";
//
//    private static final int REQUEST_CODE_OPEN_ITEM = 1;
//    private static final int REQUEST_CODE_SIGN_IN = 0;
//    private GoogleSignInClient mGoogleSignInClient;
//    private DriveClient mDriveClient;
//    private DriveResourceClient mDriveResourceClient;
//
//    private static final int READ_REQUEST_CODE = 42;
//    private Uri selectedWalletURI;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_open_pass_wallet);
//
//        final Button selectGDrivePasswalletButton = findViewById(R.id.select_gdrive_passwallet_button);
//        selectGDrivePasswalletButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                try {
//                    loadEncryptedWalletFromGoogleDrive();
//                } catch (Exception exception) {
//                    Log.e(TAG, exception.getMessage(), exception);
//                }
//            }
//        });
//        final Button selectLocalPasswalletButton = findViewById(R.id.select_local_passwallet_button);
//        selectLocalPasswalletButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                try {
//                    loadEncryptedWalletFromLocalDrive();
//                } catch (Exception exception) {
//                    Log.e(TAG, exception.getMessage(), exception);
//                }
//            }
//        });
//
//        final Button openSelectedPasswalletButton = findViewById(R.id.open_passwallet_button);
//        openSelectedPasswalletButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//                    InputStream inputStream = getContentResolver().openInputStream(selectedWalletURI);
//                    loadEncryptedWalletFile(inputStream);
//                } catch (FileNotFoundException e) {
//                    Log.e(TAG, "File not found: " + e.getMessage(), e);
//                }
//            }
//        });
//
//        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
//        String lastWalletURI = getPreferences(Context.MODE_PRIVATE).getString("selectedWalletURI", "");
//        selectedWalletName.setText(lastWalletURI);
//        if (lastWalletURI.length() > 0) {
//            selectedWalletURI = Uri.parse(lastWalletURI);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        switch (requestCode) {
//            case READ_REQUEST_CODE:
//                if (resultCode == Activity.RESULT_OK) {
//                    if (data != null) {
//                        selectedWalletURI = data.getData();
//                        Log.i(TAG, "Uri: " + selectedWalletURI.toString());
//                        SharedPreferences preferences = this.getPreferences(Context.MODE_PRIVATE);
//                        preferences.edit().putString("selectedWalletURI", selectedWalletURI.toString());
//                        TextView selectedWalletName = findViewById(R.id.selected_wallet_name_textView);
//                        selectedWalletName.setText(selectedWalletURI.toString());
//                    } else {
//                        Log.e(TAG, "Data is null, resultCode " + resultCode);
//                    }
//                }
//                break;
//            case REQUEST_CODE_SIGN_IN:
//                Task<GoogleSignInAccount> getAccountTask =
//                        GoogleSignIn.getSignedInAccountFromIntent(data);
//                if (getAccountTask.isSuccessful()) {
//                    initializeDriveClient(getAccountTask.getResult());
//                    pickWalletFile();
//                } else {
//                    int code = getAccountTask.getException() != null && getAccountTask.getException() instanceof ApiException ? ((ApiException) getAccountTask.getException()).getStatusCode() : Integer.MIN_VALUE;
//                    String codeString = GoogleSignInStatusCodes.getStatusCodeString(code);
//                    Log.e(TAG, "Sign-in failed: " + codeString, getAccountTask.getException());
//                    finish();
//                }
//                break;
//            case REQUEST_CODE_OPEN_ITEM:
//                if (resultCode == RESULT_OK) {
//                    DriveId driveId = data.getParcelableExtra(
//                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
//                    Log.i(TAG, driveId.toString());
//                } else {
//                    Log.e(TAG, "Unable to open file, resultCode: " + resultCode);
//                }
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void googleSignIn() {
//        Log.i(TAG, "Start google sign in");
//        if (mGoogleSignInClient == null) {
//            mGoogleSignInClient = buildGoogleSignInClient();
//        }
//        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//    }
//
//    /**
//     * Continues the sign-in process, initializing the Drive clients with the current
//     * user's account.
//     */
//    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
//        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
//        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
//    }
//
//    private void pickWalletFile() {
//        OpenFileActivityOptions openOptions =
//                new OpenFileActivityOptions.Builder()
//                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "application/xml"))
//                        .setActivityTitle("Load wallet")
//                        .build();
//        Task<DriveId> fileId = pickItem(openOptions);
//        fileId.addOnSuccessListener(new OnSuccessListener<DriveId>() {
//            @Override
//            public void onSuccess(DriveId driveId) {
//                Task<DriveContents> file = mDriveResourceClient.openFile(driveId.asDriveFile(), DriveFile.MODE_READ_ONLY);
//                file.addOnSuccessListener(new OnSuccessListener<DriveContents>() {
//                    @Override
//                    public void onSuccess(DriveContents driveContents) {
//                        InputStream is = file.getResult().getInputStream();
//                        loadEncryptedWalletFile(is);
//                    }
//                });
//
//                file.addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
//                        Log.e(TAG, exception.getMessage(), exception);
//                    }
//                });
//            }
//        });
//    }
//
//    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
//        TaskCompletionSource mOpenItemTaskSource = new TaskCompletionSource<>();
//        mDriveClient
//                .newOpenFileActivityIntentSender(openOptions)
//                .continueWith(new Continuation<IntentSender, Void>() {
//                    @Override
//                    public Void then(@NonNull Task<IntentSender> task) throws Exception {
//                        startIntentSenderForResult(
//                                task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0);
//                        return null;
//                    }
//                });
//        return mOpenItemTaskSource.getTask();
//    }
//
//    private GoogleSignInClient buildGoogleSignInClient() {
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(Drive.SCOPE_FILE)
//                        .build();
//        return GoogleSignIn.getClient(this, signInOptions);
//    }
//
//    private void loadEncryptedWalletFromGoogleDrive() throws IOException {
//        googleSignIn();
//    }
//
//    private void loadEncryptedWalletFromLocalDrive() {
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("application/xml");
//        startActivityForResult(intent, READ_REQUEST_CODE);
//    }
//
//    private void loadEncryptedWalletFile(InputStream is) {
//        byte[] encryptedWalletFile = null;
//        try {
//            encryptedWalletFile = loadEncryptedWalletContent(is);
//        } catch (IOException exception) {
//            Log.e(TAG, exception.getMessage(), exception);
//            try {
//                encryptedWalletFile = loadLocalEncryptedWallet();
//            } catch (IOException e) {
//            }
//        }
//
//        Intent intent = new Intent(_OldOpenPassWallet.this, ManagePassWallet.class);
//        intent.putExtra("encryptedWalletFile", encryptedWalletFile);
//        EditText password = findViewById(R.id.walletKey);
//        intent.putExtra("key", password.getText().toString().getBytes());
//        startActivity(intent);
//    }
//
//    private byte[] loadLocalEncryptedWallet() throws IOException {
//        AssetManager assetManager = getAssets();
//        try (InputStream in = assetManager.open("mywallet20180414.xml2")) {
//            return loadEncryptedWalletContent(in);
//        }
//    }
//
//    private byte[] loadEncryptedWalletContent(InputStream in) throws IOException {
//        List<Byte> xmlFileContent = new ArrayList<>();
//        byte[] buffer = new byte[256];
//        int count = -1;
//        while ((count = in.read(buffer)) != -1) {
//            for (int i = 0; i < count; i++) {
//                xmlFileContent.add(buffer[i]);
//            }
//        }
//        Byte[] encryptedArray = xmlFileContent.toArray(new Byte[]{});
//        byte[] encrypted = new byte[encryptedArray.length];
//        int i = 0;
//        for (byte e : encryptedArray) {
//            encrypted[i++] = e;
//        }
//        return encrypted;
//    }
}
