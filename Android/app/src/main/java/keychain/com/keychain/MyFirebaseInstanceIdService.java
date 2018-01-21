package keychain.com.keychain;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by rsen on 1/20/18.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Token", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        SharedPreferences sharedPref = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        if (sharedPref.getString("username", null) != null) {
            //TODO: registerTokenWithServer
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("firebase_token", refreshedToken);
        editor.apply();
    }
}
