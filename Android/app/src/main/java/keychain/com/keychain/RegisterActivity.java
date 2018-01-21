package keychain.com.keychain;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
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
import java.util.HashMap;
import java.util.Map;


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
                        String url = "http://ec2-54-224-142-62.compute-1.amazonaws.com:3000/register_username/"; //FILL API register

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
                                        sharedPref.edit().putString("username", ((EditText) findViewById(R.id.username)).getText().toString()).apply();
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progress.dismiss();
                                Toast.makeText(RegisterActivity.this, "Request to Server failed. Please try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                        ){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<String, String>();
                                try {
                                    params.put("username", ((EditText) findViewById(R.id.username)).getText().toString());
                                    params.put("public_key", sharedPref.getString("public_key", null));
                                    Log.d("IMFEFE", sharedPref.getString("public_key", null));
                                    if (sharedPref.getString("firebase_token", null) != null) {
                                        params.put("uid", sharedPref.getString("firebase_token", null));
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return params;
                            }
                        };
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
