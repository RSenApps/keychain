package keychain.com.keychain;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by rsen on 1/20/18.
 */

public class PushNotificationService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...
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

            }

            public void onFailure(AuthenticationFailureReason failureReason, boolean fatal,
                                  CharSequence errorMessage, int moduleTag, int errorCode) {
                Toast.makeText(PushNotificationService.this, "Authentication Failed. Please try again", Toast.LENGTH_SHORT).show();
            }
        });



        dialog.show();
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("PushNotification", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("PushNotification", "Message data payload: " + remoteMessage.getData());


        }
    }
}
