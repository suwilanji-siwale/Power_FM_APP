package com.melborp.suwilanji.powerfm.services;

/**
 * Created by suwilanji siwale on 2/18/2017.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


import com.melborp.suwilanji.powerfm.MainActivity;

import java.io.IOException;

import com.melborp.suwilanji.powerfm.R;

//import android.support.v7.app.NotificationCompat;


public class MainActivityService extends Service implements OnCompletionListener,
        OnPreparedListener, OnErrorListener, OnSeekCompleteListener,
        OnInfoListener, OnBufferingUpdateListener {

    private static final String TAG = "TELSERVICE";

    private MediaPlayer mediaPlayer = new MediaPlayer();

    // Set up the notification ID
    private static final int NOTIFICATION_ID = 1;
    public final String CHANNEL_ID = "my_notification_channel";
    public static final String TXT_REPLY = "text_reply";
    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    // Set up broadcast identifier and intent
    public static final String BROADCAST_BUFFER = "com.melporp.www.drawereg.broadcastbuffer";

    Intent bufferIntent;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private int notification_id;
    private RemoteViews remoteViews;
    private Context context;


    // Declare headsetSwitch variable
    private int headsetSwitch = 1;

    // OnCreate
    @Override
    public void onCreate() {
        Log.v(TAG, "Creating Service");
        // android.os.Debug.waitForDebugger();
        // Instantiate bufferIntent to communicate with Activity for progress
        // dialogue
        bufferIntent = new Intent(BROADCAST_BUFFER);

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.reset();

        // Register headset receiver
        registerReceiver(headsetReceiver, new IntentFilter(
                Intent.ACTION_HEADSET_PLUG));

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // Manage incoming phone calls during playback. Pause mp on incoming,
        // resume on hangup.
        // -----------------------------------------------------------------------------------
        // Get the telephony manager
        Log.v(TAG, "Starting telephony");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.v(TAG, "Starting listener");
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                // String stateString = "N/A";
                Log.v(TAG, "Starting CallStateChange");
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            //MainActivity.stopEqualizer();
                            isPausedInCall = true;
                        }

                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (isPausedInCall) {
                                isPausedInCall = false;
                                playMedia();
                                // MainActivity.startEqualizer();
                            }

                        }
                        break;
                }

            }
        };

        // Register the listener with the telephony manager
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);

        // Insert notification start
        initNotification();

        String inComing = intent.getExtras().getString("power");
        mediaPlayer.reset();


        // Set up the MediaPlayer data source using the strAudioLink value
        if (!mediaPlayer.isPlaying()) {
            try {

                mediaPlayer.setDataSource(inComing);

                // Send message to Activity to display progress dialogue
                sendBufferingBroadcast();
                // Prepare mediaplayer
                mediaPlayer.prepareAsync();

                //MainActivity.startEqualizer();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //return START_STICKY;
        return START_REDELIVER_INTENT;
    }

    private void sendBufferingBroadcast() {
        bufferIntent.putExtra("buffering", "1");
        sendBroadcast(bufferIntent);
    }


    // If headset gets unplugged, stop music and service.
    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        private boolean headsetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // Log.v(TAG, "ACTION_HEADSET_PLUG Intent received");
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false;
                    headsetSwitch = 0;
                    // Log.v(TAG, "State =  Headset disconnected");
                    // headsetDisconnected();
                } else if (!headsetConnected
                        && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true;
                    headsetSwitch = 1;
                    // Log.v(TAG, "State =  Headset connected");
                }

            }

            switch (headsetSwitch) {
                case (0):
                    headsetDisconnected();
                    break;
                case (1):
                    break;
            }
        }

    };

    private void headsetDisconnected() {
        stopMedia();
        stopSelf();
        //MainActivity.stopEqualizer();

    }

    // --- onDestroy, stop media player and release.  Also stop phoneStateListener, notification, receivers...---
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }

        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener,
                    PhoneStateListener.LISTEN_NONE);
        }

        // Cancel the notification
        cancelNotification();

        // Unregister headsetReceiver
        unregisterReceiver(headsetReceiver);


    }

    // Send a message to Activity that audio is being prepared and buffering
    // started.

    private void sendBufferCompleteBroadcast() {
        // Log.v(TAG, "BufferCompleteSent");
        bufferIntent.putExtra("buffering", "0");
        sendBroadcast(bufferIntent);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this,
                        "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra,
                        Toast.LENGTH_SHORT).show();

                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(this, "MEDIA ERROR SERVER DIED " + extra,
                        Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "MEDIA ERROR UNKNOWN " + extra,
                        Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        sendBufferCompleteBroadcast();
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    public void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    // Add for Telephony Manager
    public void pauseMedia() {
        // Log.v(TAG, "Pause Media");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }

    }

    public void stopMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            stopSelf();
            cancelNotification();
            //MainActivity.stopEqualizer();
        }
    }

    private void initNotification() {

        //Intent i = new Intent(this, ButtonReceiver.class);
        Intent i = new Intent(this, MainActivity.class);
        //i.putExtra("notificationId",NOTIFICATION_ID);
        //i.putExtra("stopService","YAR_FM_STREAM");
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("YAR FM");
        //builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText("The Nations Rhythm!!");
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.addAction(R.mipmap.icon_cancel, "cancel", pendingIntent);
        //builder.setColor(Color.BLUE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
        //workMrNotification();

        /*Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("YAR FM");
        //builder.setContentText(remoteMessage.getNotification().getBody());
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText("The Nations Rhythm!!");
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        //builder.setColor(Color.BLUE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());

        Intent buttonIntent = new Intent(context, ButtonReceiver.class);
buttonIntent.putExtra("notificationId",NOTIFICATION_ID);

//Create the PendingIntent
PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, buttonIntent,0);

//Pass this PendingIntent to addAction method of Intent Builder
NotificationCompat.Builder mb = new NotificationCompat.Builder(getBaseContext());
.....
.....
.....
mb.addAction(R.drawable.ic_Action, "My Action", btPendingIntent);
manager.notify(NOTIFICATION_ID, mb.build()); */


    }

    public void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

 /*   public void displayNotification(View view){
        //createNotificationChannel();

        RemoteViews normal_layout = new RemoteViews(getPackageName(),R.layout.notification_normal);
        RemoteViews expanded_layout = new RemoteViews(getPackageName(), R.layout.custom_expanded);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_firebase);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        builder.setCustomContentView(normal_layout);
        builder.setCustomBigContentView(expanded_layout);
    }*/

   /* public void workMrNotification() {
        int icon = R.drawable.ic_launcher_firebase;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, "Custom Notification", when);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.customs);
        contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher_firebase);
        contentView.setTextViewText(R.id.title, "YAR 89.7 FM");
        contentView.setTextViewText(R.id.text, "#kopalasno1hitstation");
        //contentView.setOnClickPendingIntent(R.id.notif_stop, pendingStop);
        //contentView.setOnClickPendingIntent(R.id.notif_stop, pen);

        notification.contentView = contentView;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //MainActivity.startEqualizer();
        //MainActivity.stopStream.setVisibility(View.VISIBLE);
        notification.contentIntent = contentIntent;

        //notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
        //notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        //notification.defaults |= Notification.DEFAULT_SOUND; // Sound

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }*/
}
