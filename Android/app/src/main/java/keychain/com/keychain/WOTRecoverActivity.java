package keychain.com.keychain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WOTRecoverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wot_recover);
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            String keychainID = ((EditText) findViewById(R.id.keychainid)).getText().toString();
            String address = ((EditText) findViewById(R.id.address)).getText().toString();

            //TODO:

            Toast.makeText(WOTRecoverActivity.this, "You have submitted your request to recover the account. After x more requests, this account will be recovered.", Toast.LENGTH_LONG).show();
            finish();
            }
        });

    }
}
