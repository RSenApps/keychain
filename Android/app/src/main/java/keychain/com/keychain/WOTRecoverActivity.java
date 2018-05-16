package keychain.com.keychain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.List;

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
                new RecoverWOTTask().execute(keychainID, address);
            }
        });

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    class RecoverWOTTask extends AsyncTask<Object, Void, Boolean> {

        protected Boolean doInBackground(Object... params) {
            try {
                String idToRecover = (String) params[0];
                idToRecover = idToRecover + String.format("%1$-" + (32 -  idToRecover.length()) + "s", " ");
                String newAddress = (String) params[1];
                SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                String password = prefs.getString("password", "");
                String keychainID = prefs.getString("keychainid", "");

                final Credentials credentials;
                credentials = WalletUtils.loadCredentials(password, prefs.getString("walletPath", ""));
                Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
                KeychainIdentity contract = new KeychainIdentity(web3j, credentials, BigInteger.valueOf(100), BigInteger.valueOf(400000));
                Log.d("test", contract.Do_recover_address(idToRecover.getBytes(), keychainID.getBytes(), newAddress).send().toString());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;


        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(WOTRecoverActivity.this, "You have submitted your request to recover the account.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
