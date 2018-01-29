package keychain.com.keychain;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import net.glxn.qrgen.android.QRCode;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity {
    NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //showSignedMessage("test");

        /*
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.com.keychain.auth.request", getSharedPreferences("prefs", MODE_PRIVATE).getString("public_key", "").getBytes()), NdefRecord.createApplicationRecord("keychain.com.keychain")});
        mNfcAdapter.setNdefPushMessage(msg, this);*/
    }

    private void showSignedMessage(String message) {
        /*try {
            String output = Cryptography.sign(message);
            Toast.makeText(this, output, Toast.LENGTH_LONG).show();
        } catch (UserNotAuthenticatedException e) {
            Intent in = ((KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE)).createConfirmDeviceCredentialIntent(
                    "KeyChain", "Please log in to confirm access to your account.");
            startActivityForResult(in, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // The user authenticated successfully, so let's try to en-/decrypt again.
            if (requestCode == 1) {
                showSignedMessage("test");
            }
        } else {
            // The user canceled or didn’t complete the lock screen
            // operation. Go to error/cancellation flow.
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Reprint.cancelAuthentication();
    }

    @Override
    protected void onResume() {
        super.onResume();

        KeyguardManager keyguardManager =
                (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (!keyguardManager.isKeyguardSecure()) {
            Toast.makeText(this,
                    "Lock screen security not enabled in Settings",
                    Toast.LENGTH_LONG).show();
            return;
        }

        /*
        if (getIntent().getBooleanExtra("fromnotification", false))
        {
            getIntent().removeExtra("fromnotification");
            final String challenge = getIntent().getStringExtra("challenge");
            final String callback_url = getIntent().getStringExtra("callback_url");
            final String resource = getIntent().getStringExtra("resource");

            final AlertDialog.Builder fingerprintDialog = new AlertDialog.Builder(this);
            LayoutInflater factory = LayoutInflater.from(this);
            final View view = factory.inflate(R.layout.dialog_fingerprint, null);
            fingerprintDialog.setView(view);
            fingerprintDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dlg, int sumthin) {
                    Reprint.cancelAuthentication();
                    dlg.dismiss();
                }
            });
            final SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
            final AlertDialog dialog = fingerprintDialog.create();
            Reprint.authenticate(new AuthenticationListener() {
                public void onSuccess(int moduleTag) {
                    dialog.dismiss();
                    try {
                        PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Cryptography.base64DecodeKey(prefs.getString("private_key", ""))));
                        final String response = "";//new String(Cryptography.decrypt(key, Cryptography.base64DecodeKey(challenge)));
                        Toast.makeText(MainActivity.this, "Decrypted:" + response, Toast.LENGTH_LONG).show();

                        // Request a string response from the provided URL.
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, callback_url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Toast.makeText(MainActivity.this, "Authenticated", Toast.LENGTH_LONG).show();
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(MainActivity.this, "Request to Server failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                        ){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String, String>();
                                params.put("result", response);
                                return params;
                            }
                        };
                        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                        queue.add(stringRequest);
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                    //finish challenge
                    Toast.makeText(MainActivity.this, challenge + callback_url + resource, Toast.LENGTH_LONG).show();

                }

                public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                      CharSequence errorMessage, int moduleTag, int errorCode) {
                    Toast.makeText(MainActivity.this, "Authentication Failed. Please try again", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            // only one message sent during the beam
            final NdefMessage msg = (NdefMessage) rawMsgs[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();

            //TODO: Send message to API asking to authenticate and challenge user, once it has completed send me a push back with list of services that has access to
            StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://ec2-54-224-142-62.compute-1.amazonaws.com:3000/query_access_and_return_result/",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(MainActivity.this, "Sent challenge to user", Toast.LENGTH_LONG).show();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Request to Server failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("public_key", new String(msg.getRecords()[0].getPayload()));
                    params.put("resource", "NFC Device Verification");
                    params.put("return_key", getSharedPreferences("prefs", MODE_PRIVATE).getString("firebase_token", ""));

                    return params;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            queue.add(stringRequest);
        }
        */
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if(prefs.getLong("keychain-id", -1) == -1) {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
            finish();
        }
        getSupportActionBar().setTitle(Long.toHexString(prefs.getLong("keychain-id", 0)));

        ListView listView = (ListView) findViewById(R.id.listview_with_fab);

        String[] listItwms = new String[]{"MIT Baker Card Reader", "Google Account Services", "MIT Student Certification", "reddit.com"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItwms);
        listView.setAdapter(adapter);
        /*
        findViewById(R.id.show_public).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertadd = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View view = factory.inflate(R.layout.dialog_public_key, null);
                final SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
                ((TextView) view.findViewById(R.id.public_key)).setText("Public Key: " + prefs.getString("public_key", "") + "\nKeyChain keys are not the same as Ethereum keys, do not send Ethereum to this address.");
                ((TextView) view.findViewById(R.id.public_key)).setTextIsSelectable(true);

                Bitmap myBitmap = QRCode.from(prefs.getString("public_key", "")).bitmap();
                ImageView myImage = (ImageView) view.findViewById(R.id.qr_code);
                myImage.setImageBitmap(myBitmap);

                alertadd.setView(view);
                alertadd.setNegativeButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("KeyChain Public Key", prefs.getString("public_key", ""));
                        clipboard.setPrimaryClip(clip);
                    }
                });
                alertadd.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                        dlg.dismiss();
                    }
                });

                alertadd.show();
            }
        });
        */

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, QRScannerActivity.class));
            }
        });

    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        final SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        switch (item.getItemId()) {
            case R.id.action_logout:
                prefs.edit().putLong("keychain-id", -1)
                        .apply();
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.action_export:
                /*
                final AlertDialog.Builder fingerprintDialog = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater factory = LayoutInflater.from(MainActivity.this);
                final View view = factory.inflate(R.layout.dialog_fingerprint, null);
                fingerprintDialog.setView(view);
                fingerprintDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                        Reprint.cancelAuthentication();
                        dlg.dismiss();
                    }
                });
                final AlertDialog dialog = fingerprintDialog.create();
                Reprint.authenticate(new AuthenticationListener() {
                    public void onSuccess(int moduleTag) {
                        dialog.dismiss();
                        */
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Export Keys");
                        TextView showText = new TextView(MainActivity.this);
                        showText.setPadding(30, 30, 30, 0);

                        try {
                            showText.setText("Public Key: " + Cryptography.getPublickey());
                        } catch (CertificateException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (KeyStoreException e) {
                            e.printStackTrace();
                        }
                        showText.setTextIsSelectable(true);
                        alertDialog.setView(showText);
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Copy Private Key",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("KeyChain Public Key", prefs.getString("private_key", ""));
                                        clipboard.setPrimaryClip(clip);
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Copy Public Key",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("KeyChain Public Key", prefs.getString("public_key", ""));
                                        clipboard.setPrimaryClip(clip);
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Done",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    /*}

                    public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                          CharSequence errorMessage, int moduleTag, int errorCode) {
                        Toast.makeText(MainActivity.this, "Authentication Failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });



                dialog.show();*/



                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
