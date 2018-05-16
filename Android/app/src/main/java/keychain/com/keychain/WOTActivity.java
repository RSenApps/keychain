package keychain.com.keychain;

import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
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

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                getSharedPreferences("prefs", MODE_PRIVATE).edit().putString("wot", addresses).apply();

                if (numNeeded > 0) {
                    String[] addressArray = addresses.split(",");
                    List<byte[]> byteList = new ArrayList<>(addressArray.length);
                    for (int i = 0; i < addressArray.length; i++) {
                        addressArray[i] = addressArray[i].trim();
                        addressArray[i] = addressArray[i] + String.format("%1$-" + (32 -  addressArray[i].length()) + "s", " ");
                        byteList.add(addressArray[i].getBytes());
                    }
                    new SetupWOTTask().execute(byteList, numNeeded);
                }
                else {
                    Toast.makeText(WOTActivity.this, addresses + numNeeded, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(WOTActivity.this, MainActivity.class));
                }
            }
        });

    }

    class SetupWOTTask extends AsyncTask<Object, Void, Boolean> {

        protected Boolean doInBackground(Object... params) {
            try {
                List<byte[]> addressList = ( List<byte[]>) params[0];
                int numNeeded = (Integer) params[1];

                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                String password = prefs.getString("password", "");
                String keychainID = prefs.getString("keychainid", "");

                final Credentials credentials;
                credentials = WalletUtils.loadCredentials(password, prefs.getString("walletPath", ""));
                Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
                KeychainIdentity contract = new KeychainIdentity(web3j, credentials, BigInteger.valueOf(100), BigInteger.valueOf(400000));
                contract.Create_web_of_trust(keychainID.getBytes(), addressList, BigInteger.valueOf(addressList.size()), BigInteger.valueOf(numNeeded)).send();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;


        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                startActivity(new Intent(WOTActivity.this, MainActivity.class));
                finish();
            }
        }
    }
}
