package loclock.net.loclock;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_USERNAME = "username";
    public static final String KEY_SHARE_ENABLED = "shareEnabled";
    Button saveButton;

    EditText usernameEditText;

    CheckBox shareLocationCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.saveButton = (Button)findViewById(R.id.save_button);
        this.usernameEditText = (EditText)findViewById(R.id.username_edittext);
        this.shareLocationCheckBox = (CheckBox)findViewById(R.id.share_location_checkbox);

        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();

                e.putString(KEY_USERNAME, usernameEditText.getText().toString());
                e.putBoolean(KEY_SHARE_ENABLED, shareLocationCheckBox.isChecked());

                e.commit();

                //TODO: trigger service

                finish();
            }
        });

        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            this.shareLocationCheckBox.setChecked(sp.getBoolean(KEY_SHARE_ENABLED, true));
            this.usernameEditText.setText(sp.getString(KEY_USERNAME, ""));
        }
    }
}
