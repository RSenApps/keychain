package keychain.com.keychain;

import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class WOTActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wot);
        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addresses = ((EditText) findViewById(R.id.wot)).getText().toString();
                int numNeeded = 0;
                try {
                    numNeeded = Integer.parseInt(((EditText) findViewById(R.id.numNeeded)).getText().toString());
                }
                catch (Exception e) {

                }
                if (numNeeded > 0) {
                    //TODO: create WOT
                }
                getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("wot", addresses).apply();

                Toast.makeText(WOTActivity.this, addresses + numNeeded, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(WOTActivity.this, MainActivity.class));
            }
        });

    }
}
