package com.example.ishanfx.departmentapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by IshanFx on 2/14/2016.
 */
public class RemoteControlReceiver extends BroadcastReceiver {
    Intent intent;

    public static final String BROADCAST_ACTION = "com.example.ishanfx.departmentapp";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (KeyEvent.KEYCODE_VOLUME_DOWN == event.getKeyCode()) {
                 Toast.makeText(context,"ssss",Toast.LENGTH_SHORT).show();

            }
        }
    }

}