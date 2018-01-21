package keychain.com.keychain;

import android.app.Activity;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import net.glxn.qrgen.android.QRCode;

import static android.nfc.NdefRecord.createMime;

public class MainActivity extends AppCompatActivity {
    NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/vnd.com.keychain.auth.request", getSharedPreferences("prefs", MODE_PRIVATE).getString("public_key", "").getBytes()), NdefRecord.createApplicationRecord("keychain.com.keychain")});
        mNfcAdapter.setNdefPushMessage(msg, this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Reprint.cancelAuthentication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getBooleanExtra("fromnotification", false))
        {
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
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();

            //TODO: Send message to API asking to authenticate and challenge user, once it has completed send me a push back with list of services that has access to
        }

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        if(prefs.getString("username", null) == null) {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
            finish();
        }
        getSupportActionBar().setTitle(prefs.getString("username", "KeyChain"));

        ListView listView = (ListView) findViewById(R.id.listview_with_fab);

        String[] listItwms = new String[]{"MIT Baker Card Reader", "Google Account Services", "MIT Student Certification", "reddit.com"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, listItwms);
        listView.setAdapter(adapter);

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
                prefs.edit().putString("username", null)
                        .putString("private_key", null)
                        .putString("public_key", null)
                        .apply();
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.action_export:

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
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Export Keys");
                        TextView showText = new TextView(MainActivity.this);
                        showText.setPadding(30, 30, 30, 0);
                        showText.setText("Public Key: " + prefs.getString("public_key", "") + "\nPrivate Key: " + prefs.getString("private_key", "")
                                + "\n\nKeyChain keys are not the same as Ethereum keys, do not send Ethereum to this address. This private key gives access to all of your resources, please be careful.");
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
                    }

                    public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                          CharSequence errorMessage, int moduleTag, int errorCode) {
                        Toast.makeText(MainActivity.this, "Authentication Failed. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });



                dialog.show();



                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
