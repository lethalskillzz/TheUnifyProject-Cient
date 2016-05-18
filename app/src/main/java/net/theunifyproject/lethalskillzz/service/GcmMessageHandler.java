package net.theunifyproject.lethalskillzz.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.activity.DigestActivity;
import net.theunifyproject.lethalskillzz.activity.MainActivity;
import net.theunifyproject.lethalskillzz.activity.RepositoryActivity;
import net.theunifyproject.lethalskillzz.activity.ShoppingActivity;
import net.theunifyproject.lethalskillzz.activity.TransitActivity;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.PrefManager;

/**
 * Created by Ibrahim on 02/12/2015.
 */
public class GcmMessageHandler extends GcmListenerService {
    public static final int MESSAGE_NOTIFICATION_ID = 1337;

    public static String isNotify = null;

    @Override
    public void onMessageReceived(String from, Bundle data) {

        PrefManager pref = new PrefManager(this);

        if(pref.isLoggedIn()) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), soundUri);

            int type = data.getInt("type");
            if (type == AppConfig.NOTIFICATION_DIGEST || type == AppConfig.NOTIFICATION_REPO ||
                    type == AppConfig.NOTIFICATION_SHOP || type == AppConfig.NOTIFICATION_TRANSIT) {

                if (!pref.getPrompt()) {
                    return;
                }
            }

            String title = data.getString("title");
            String body = data.getString("body");

            if (pref.getVibrate()) {
                long pattern[] = {0, 200, 200};
                vibrator.vibrate(pattern, -1);
            }

            if (pref.getSound())
                r.play();

            showNotification(type, title, body);
        }
    }


    private void showNotification(int type, String title, String body) {
        Intent intent;
        switch (type) {

            case AppConfig.NOTIFICATION_REPO:
                intent = new Intent(this, RepositoryActivity.class);
                break;

            case AppConfig.NOTIFICATION_DIGEST:
                intent = new Intent(this, DigestActivity.class);
                break;

            case AppConfig.NOTIFICATION_SHOP:
                intent = new Intent(this, ShoppingActivity.class);
                break;

            case AppConfig.NOTIFICATION_TRANSIT:
                intent = new Intent(this, TransitActivity.class);
                break;

            default:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra("isNotify", "isNotify");
                break;
        }


        int requestID = (int) System.currentTimeMillis(); //unique requestID to differentiate between various notification with same NotifId
        int flags = PendingIntent.FLAG_CANCEL_CURRENT; // cancel old intent and create new one
        PendingIntent pIntent = PendingIntent.getActivity(this, requestID, intent, flags);

        Context context = getBaseContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pIntent)
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MESSAGE_NOTIFICATION_ID, mBuilder.build());
    }
}
