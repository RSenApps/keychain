package keychain.com.keychain;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        // ...
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        String challenge = remoteMessage.getData().get("challenge");
        String callback_url = remoteMessage.getData().get("callback_url");
        String resource = remoteMessage.getData().get("resource");


        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), MainActivity.class).setAction("actionstring" + System.currentTimeMillis()).putExtra("fromnotification", true)
                        .putExtra("challenge", challenge).putExtra("callback_url", callback_url).putExtra("resource",  resource), 0);



// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("KeyChain Challenge")
                .setContentText(resource + " is requesting authentication")
                .setSmallIcon(R.drawable.ic_action_key)
                .setAutoCancel(true)
                .setContentIntent(pIntent).build();


        notificationManager.notify(0, n);


    }
}
