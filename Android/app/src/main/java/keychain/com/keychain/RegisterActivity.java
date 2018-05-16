package keychain.com.keychain;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.obsez.android.lib.filechooser.ChooserDialog;

import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import rx.Subscription;

import static keychain.com.keychain.QRScannerActivity.bytesToHex;


public class RegisterActivity extends AppCompatActivity {

    boolean walletSelected;
    String walletPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        walletSelected = false;
        findViewById(R.id.wallet_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ChooserDialog().with(RegisterActivity.this)
                        .withStartFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath())
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                Toast.makeText(RegisterActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                                walletPath = path;
                                ((TextView) findViewById(R.id.wallet_view)).setText(walletPath);
                                walletSelected = true;
                            }
                        })
                        .build()
                        .show();
            }
        });


        findViewById(R.id.unlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    String keychainID = ((EditText) findViewById(R.id.keychainid)).getText().toString();
                    keychainID = keychainID + String.format("%1$-" + (32 - keychainID.length()) + "s", " "); //pad keychainID
                    Log.d("fefefe", keychainID);
                    Log.d("fefefe", bytesToHex(keychainID.getBytes()));
                    Log.d("fefefe", String.valueOf(keychainID.getBytes().length));

                    final Credentials credentials = WalletUtils.loadCredentials(password, walletPath);

                    getSharedPreferences("prefs", MODE_PRIVATE).edit()
                            .putString("password", password)
                            .putString("walletPath", walletPath)
                            .putString("keychainid", keychainID)
                            .apply();

                    new SetupUserTask().execute(password, keychainID);
                }
                catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, "Wallet file or password is incorrect", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    class SetupUserTask extends AsyncTask<String, Void, Boolean> {

        protected Boolean doInBackground(String... strings) {
            try {
                final Credentials credentials = WalletUtils.loadCredentials(strings[0], walletPath);

                Web3j web3j = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));
                KeychainIdentity contract = new KeychainIdentity(web3j, credentials, BigInteger.valueOf(11), BigInteger.valueOf(100000));
                Boolean userExists = contract.Query_user_exists(strings[1].getBytes()).send();
                if (!userExists) {
                    // if new keychain ID then create public key to keychain mapping and go to web of trust creation page
                    contract.Create_username(strings[1].getBytes(), credentials.getAddress()).sendAsync();
                    return true;
                } else{
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;


        }

        protected void onPostExecute(Boolean wasNewUser) {
            if (wasNewUser) {
                startActivity(new Intent(RegisterActivity.this, WOTActivity.class));
                finish();
            } else {

                String password = getSharedPreferences("prefs", MODE_PRIVATE).getString("password", "");
                final Credentials credentials;
                try {
                    credentials = WalletUtils.loadCredentials(password, walletPath);
                    AlertDialog alertDialog = new AlertDialog.Builder(RegisterActivity.this).create();
                    alertDialog.setTitle("Authorize Public Key");
                    alertDialog.setMessage("Please authorize this address: " + credentials.getAddress() + " on another device or via your web of trust.");

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Copy Address",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("KeyChain Address", credentials.getAddress());
                                    clipboard.setPrimaryClip(clip);
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finish();
                                }
                            });
                    alertDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
