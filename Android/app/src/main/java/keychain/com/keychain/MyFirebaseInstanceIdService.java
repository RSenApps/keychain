package keychain.com.keychain;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rsen on 1/20/18.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Token", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        if (sharedPref.getString("username", null) != null) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://ec2-54-224-142-62.compute-1.amazonaws.com:3000/registerUID/";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(), "Refreshed UID with server", Toast.LENGTH_SHORT).show();
                            //Go to next activity
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Failed to refresh UID with server", Toast.LENGTH_LONG).show();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    SharedPreferences prefs = getSharedPreferences("prefs" , MODE_PRIVATE);
                    Map<String,String> params = new HashMap<String, String>();
                    params.put("public_key", prefs.getString("public_key", null));
                    params.put("uid",  refreshedToken);

                    return params;
                }
            };;
            queue.add(stringRequest);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("firebase_token", refreshedToken);
        editor.apply();
    }
}
