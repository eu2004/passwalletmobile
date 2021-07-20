package ro.group305.passwalletandroidclient.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ro.eu.passwallet.service.PasswordGenerator;
import ro.group305.passwalletandroidclient.R;
import ro.group305.passwalletandroidclient.utils.ActivityUtils;

public class GeneratePasswordActivity extends AppCompatActivity {
    private static final String TAG = "PassWallet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_password);

        initControls();
        createGeneratePasswordButton();
        createCopyPasswordButton();
        createResetSettingsButton();
    }

    public static PasswordGenerator getGeneratorFromPrefs(Activity activity) {
        PasswordGenerator passwordGenerator = new PasswordGenerator();
        passwordGenerator.setLength(Integer.parseInt(ActivityUtils.getPreference(activity, "gen_pass_length_EditText", String.valueOf(passwordGenerator.getLength()))));
        passwordGenerator.setIncludeNumbers(Boolean.parseBoolean(ActivityUtils.getPreference(activity, "gen_pass_include_numbers", String.valueOf(passwordGenerator.isIncludeNumbers()))));
        passwordGenerator.setIncludeUpperCase(Boolean.parseBoolean(ActivityUtils.getPreference(activity, "gen_pass_include_uppercase", String.valueOf(passwordGenerator.isIncludeUpperCase()))));
        passwordGenerator.setIncludeLowerCase(Boolean.parseBoolean(ActivityUtils.getPreference(activity, "gen_pass_include_lowercase", String.valueOf(passwordGenerator.isIncludeLowerCase()))));
        passwordGenerator.setIncludeSymbols(Boolean.parseBoolean(ActivityUtils.getPreference(activity, "gen_pass_include_symbols", String.valueOf(passwordGenerator.isIncludeSymbols()))));
        return passwordGenerator;
    }

    private void initControls() {
        PasswordGenerator passwordGenerator = new PasswordGenerator();

        EditText length_EditText = findViewById(R.id.length_EditText);
        length_EditText.setText(ActivityUtils.getPreference(this, "gen_pass_length_EditText", String.valueOf(passwordGenerator.getLength())));

        CheckBox include_numbers = findViewById(R.id.include_numbers);
        include_numbers.setChecked(Boolean.parseBoolean(ActivityUtils.getPreference(this, "gen_pass_include_numbers", String.valueOf(passwordGenerator.isIncludeNumbers()))));

        CheckBox include_uppercase = findViewById(R.id.include_uppercase);
        include_uppercase.setChecked(Boolean.parseBoolean(ActivityUtils.getPreference(this, "gen_pass_include_uppercase", String.valueOf(passwordGenerator.isIncludeUpperCase()))));

        CheckBox include_lowercase = findViewById(R.id.include_lowercase);
        include_lowercase.setChecked(Boolean.parseBoolean(ActivityUtils.getPreference(this, "gen_pass_include_lowercase", String.valueOf(passwordGenerator.isIncludeLowerCase()))));

        CheckBox include_symbols = findViewById(R.id.include_symbols);
        include_symbols.setChecked(Boolean.parseBoolean(ActivityUtils.getPreference(this, "gen_pass_include_symbols", String.valueOf(passwordGenerator.isIncludeSymbols()))));
    }

    private void createGeneratePasswordButton() {
        Button createPasswalletItemButton = findViewById(R.id.generate_password_button);
        createPasswalletItemButton.setOnClickListener(v -> {
            PasswordGenerator passwordGenerator = new PasswordGenerator();
            EditText length_EditText = findViewById(R.id.length_EditText);
            CheckBox include_numbers = findViewById(R.id.include_numbers);
            CheckBox include_uppercase = findViewById(R.id.include_uppercase);
            CheckBox include_lowercase = findViewById(R.id.include_lowercase);
            CheckBox include_symbols = findViewById(R.id.include_symbols);

            //set length
            if (length_EditText.getText().toString().isEmpty()) {
                return;
            }
            int length = Integer.parseInt(length_EditText.getText().toString());
            if (length > 256) {
                length = 256;
            }
            passwordGenerator.setLength(length);
            //

            passwordGenerator.setIncludeNumbers(include_numbers.isChecked());
            passwordGenerator.setIncludeUpperCase(include_uppercase.isChecked());
            passwordGenerator.setIncludeLowerCase(include_lowercase.isChecked());
            passwordGenerator.setIncludeSymbols(include_symbols.isChecked());

            EditText passwordEditText = findViewById(R.id.password_EditText);
            passwordEditText.setText(passwordGenerator.generate());

            saveSettingsToPrefs();
        });
    }

    private void saveSettingsToPrefs() {
        EditText length_EditText = findViewById(R.id.length_EditText);
        CheckBox include_numbers = findViewById(R.id.include_numbers);
        CheckBox include_uppercase = findViewById(R.id.include_uppercase);
        CheckBox include_lowercase = findViewById(R.id.include_lowercase);
        CheckBox include_symbols = findViewById(R.id.include_symbols);

        ActivityUtils.savePreference(this, "gen_pass_length_EditText", String.valueOf(length_EditText.getText()));
        ActivityUtils.savePreference(this, "gen_pass_include_numbers", String.valueOf(include_numbers.isChecked()));
        ActivityUtils.savePreference(this, "gen_pass_include_uppercase", String.valueOf(include_uppercase.isChecked()));
        ActivityUtils.savePreference(this, "gen_pass_include_lowercase", String.valueOf(include_lowercase.isChecked()));
        ActivityUtils.savePreference(this, "gen_pass_include_symbols", String.valueOf(include_symbols.isChecked()));
    }

    private void createCopyPasswordButton() {
        Button copyButton = findViewById(R.id.copy_button);
        copyButton.setOnClickListener(v -> {
            EditText passwordText = findViewById(R.id.password_EditText);

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("key", passwordText.getText());
            clipboard.setPrimaryClip(clip);
        });
    }

    private void createResetSettingsButton() {
        TextView createResetSettingsButton = findViewById(R.id.reset_button);
        createResetSettingsButton.setOnClickListener(v -> {
            PasswordGenerator passwordGenerator = new PasswordGenerator();
            EditText length_EditText = findViewById(R.id.length_EditText);
            length_EditText.setText(String.valueOf(passwordGenerator.getLength()));
            CheckBox include_numbers = findViewById(R.id.include_numbers);
            include_numbers.setChecked(passwordGenerator.isIncludeNumbers());
            CheckBox include_uppercase = findViewById(R.id.include_uppercase);
            include_uppercase.setChecked(passwordGenerator.isIncludeUpperCase());
            CheckBox include_lowercase = findViewById(R.id.include_lowercase);
            include_lowercase.setChecked(passwordGenerator.isIncludeLowerCase());
            CheckBox include_symbols = findViewById(R.id.include_symbols);
            include_symbols.setChecked(passwordGenerator.isIncludeSymbols());

            saveSettingsToPrefs();
        });
    }
}
