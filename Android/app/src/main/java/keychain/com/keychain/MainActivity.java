package keychain.com.keychain;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import net.glxn.qrgen.android.QRCode;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        /*if(prefs.getString("username", null) == null) {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
            finish();
        }*/
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
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Export Keys");
                TextView showText = new TextView(this);
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

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
