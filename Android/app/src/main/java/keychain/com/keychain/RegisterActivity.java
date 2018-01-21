package keychain.com.keychain;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;



public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        try {
            final KeyPair pair = Cryptography.buildKeyPair();
            final SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
            sharedPref.edit().putString("public_key", Cryptography.base64EncodeKey(pair.getPublic().getEncoded()))
                    .putString("private_key", Cryptography.base64EncodeKey(pair.getPrivate().getEncoded()))
                    .apply();

            findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((EditText) findViewById(R.id.username)).getText().toString().length() == 0) {
                        Toast.makeText(RegisterActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                        String url = "https://l6g44s18fj.execute-api.us-east-1.amazonaws.com/prod/"; //FILL API register

                        try {
                            url += "username=" + URLEncoder.encode(((EditText) findViewById(R.id.username)).getText().toString(), "UTF-8") + "&key=" + URLEncoder.encode(sharedPref.getString("public_key", null), "UTF-8");
                            if (sharedPref.getString("firebase_token", null) != null) {
                                url += "&UID=" + sharedPref.getString("firebase_token", null);
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        final ProgressDialog progress = new ProgressDialog(RegisterActivity.this);
                        progress.setMessage("Creating Account");
                        progress.setIndeterminate(true);
                        progress.show();

                        // Request a string response from the provided URL.
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        progress.dismiss();
                                        //Go to next activity
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progress.dismiss();
                                Toast.makeText(RegisterActivity.this, "Request to Server failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        });
                        queue.add(stringRequest);
                    }

                }
            });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            Toast.makeText(RegisterActivity.this, "No such algorithm", Toast.LENGTH_SHORT).show();
        }
    }
}
