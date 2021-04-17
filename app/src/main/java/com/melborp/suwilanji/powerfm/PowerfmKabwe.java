package com.melborp.suwilanji.powerfm;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.melborp.suwilanji.powerfm.services.MainActivityService;

import com.melborp.suwilanji.powerfm.R;

public class PowerfmKabwe extends AppCompatActivity {
    TextView move;
    private Toolbar toolbar;
    private ImageView playStream, stopStream;

    private boolean isOnline;
    public Intent serviceIntent;
    private static final int REQUEST_PHONE_CALL = 1;
    final String POWER_KABWE_STREAM = "http://stream-africa.com:8000/PowerFMKabwe";

    public static ImageView equalizerImage;

    //CircleMenu circleMenu;
    public static AnimationDrawable aniEqualizer;
    public static ProgressBar playProgressCircle;

    boolean mBufferBroadcastIsRegistered;
    private ProgressDialog pdBuff = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.powerfm_kabwe);

        toolbar = findViewById(R.id.kabweToolBar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        move = findViewById(R.id.textViewMoving);
        move.setSelected(true);

        playStream = findViewById(R.id.actionPlay);
        stopStream = findViewById(R.id.actionStop);


        //playProgressCircle
        playProgressCircle = findViewById(R.id.playProgressBar);
        playProgressCircle.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);

        //equalizer image view
        equalizerImage = findViewById(R.id.equalizer_image);
        equalizerImage.setBackgroundResource(R.drawable.equalizer_animation);


        //on click listeners
        playStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio();
                //playStream.setVisibility(View.GONE);
                //stopStream.setVisibility(View.VISIBLE);

            }
        });

        stopStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MainActivityService ser = new MainActivityService();
                stopMyPlayService();
                stopStream.setVisibility(View.GONE);
                playStream.setVisibility(View.VISIBLE);
                stopEqualizer();
                // ser.cancelNotification();

            }
        });



        try {
            serviceIntent = new Intent(this, MainActivityService.class);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    e.getClass().getName() + " " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        //playAudio();

    }


    private void checkConnectivity() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {

            isOnline = true;
        } else {
            isOnline = false;
        }
    }

    // -- onPause, unregister broadcast receiver. To improve, also save screen data ---
    @Override
    protected void onPause() {
        // Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            unregisterReceiver(broadcastBufferReceiver);
            mBufferBroadcastIsRegistered = false;
        }
        super.onPause();
    }


    // -- onResume register broadcast receiver. To improve, retrieve saved screen data ---
    @Override
    protected void onResume() {
        // Register broadcast receiver
        if (!mBufferBroadcastIsRegistered) {
            registerReceiver(broadcastBufferReceiver, new IntentFilter(
                    MainActivityService.BROADCAST_BUFFER));
            mBufferBroadcastIsRegistered = true;
        }
        super.onResume();
    }


    // Set up broadcast receiver
    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            showPD(bufferIntent);
        }
    };

    // Progress dialogue...
    private void BufferDialogue() {

       /* pdBuff = new ProgressDialog(MainActivity.this);
        // pdBuff.setTitle("Buffering");
        //pdBuff.setMessage("Connecting to YAR FM server..");
        pdBuff.setTitle("Connecting to YAR FM server..");
        pdBuff.setIndeterminate(true);
        pdBuff.setCancelable(false);
        pdBuff.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                stopMyPlayService();
            }
        });
        pdBuff.show();

        playStream.setVisibility(View.GONE);
        stopStream.setVisibility(View.VISIBLE);*/
        playProgressCircle.setVisibility(View.VISIBLE);

    }


    // Handle progress dialogue for buffering...
    private void showPD(Intent bufferIntent) {
        String bufferValue = bufferIntent.getStringExtra("buffering");
        int bufferIntValue = Integer.parseInt(bufferValue);

        // When the broadcasted "buffering" value is 1, show "Buffering"
        // progress dialogue.
        // When the broadcasted "buffering" value is 0, dismiss the progress
        // dialogue.

        switch (bufferIntValue) {
            case 0:
               /* if (pdBuff != null) {
                    pdBuff.dismiss();
                }*/
                playProgressCircle.setVisibility(View.GONE);
                startEqualizer();


                playStream.setVisibility(View.GONE);
                stopStream.setVisibility(View.VISIBLE);
                break;

            case 1:
                BufferDialogue();
                //
                break;


            case 2:

                break;

        }
    }


    private void playAudio() {

        checkConnectivity();
        if (isOnline) {
            stopMyPlayService();

            serviceIntent.putExtra("power", POWER_KABWE_STREAM);

            try {
                startService(serviceIntent);

            } catch (Exception e) {

                e.printStackTrace();
                Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
            }


        } else {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("No Internet Connection...");
            //alertDialog.setMessage("Please connect to a network.....");
            alertDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.setCancelable(true);
                    finish();
                }


            });
            alertDialog.setIcon(R.mipmap.ic_launcher);
            alertDialog.create().getWindow().getAttributes().windowAnimations = R.style.dialogAnimationFade;
            alertDialog.show();
            //FragOnAir.speaker.clearAnimation();
        }
    }


    public void stopMyPlayService() {

        try {
            stopService(serviceIntent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    e.getClass().getName() + " " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }


    ///equalizer animation
    public void startEqualizer() {
        aniEqualizer = (AnimationDrawable) equalizerImage.getBackground();
        aniEqualizer.start();


    }

    public void stopEqualizer() {
        aniEqualizer = (AnimationDrawable) equalizerImage.getBackground();
        aniEqualizer.stop();

    }


}
