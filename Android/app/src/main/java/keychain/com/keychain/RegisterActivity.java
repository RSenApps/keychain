package keychain.com.keychain;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


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
        //Web3j web3 = Web3jFactory.build(new HttpService("https://ropsten.infura.io/ovliA0eGnH5yI2KdpbxX"));


        findViewById(R.id.unlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String password = ((EditText) findViewById(R.id.password)).getText().toString();
                    String keychainID = ((EditText) findViewById(R.id.keychainid)).getText().toString();
                    final Credentials credentials = WalletUtils.loadCredentials(password, walletPath);
                    getSharedPreferences("prefs", MODE_PRIVATE).edit()
                            .putString("password", password)
                            .putString("walletPath", walletPath)
                            .putString("keychainid", keychainID)
                            .apply();
                    // TODO: if new keychain ID then create public key to keychain mapping and go to web of trust creation page
                    // else {
                    startActivity(new Intent(RegisterActivity.this, WOTActivity.class));
                    finish();
                    /*
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
                    */

                }
                catch (Exception e) {
                    Toast.makeText(RegisterActivity.this, "Wallet file or password is incorrect", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
