package com.melborp.suwilanji.powerfm;

import android.media.MediaPlayer;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;


import com.bumptech.glide.Glide;
import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;

import java.util.ArrayList;
import java.util.List;

import com.melborp.suwilanji.powerfm.R;

public class MainActivity extends AppCompatActivity {

    List<Integer> lstImages = new ArrayList<>();

    private VideoView mVideoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        HorizontalInfiniteCycleViewPager pager = (HorizontalInfiniteCycleViewPager)findViewById(R.id.horizontal_cycle);
       // MyAdapter adapter = new MyAdapter(lstImages, getBaseContext());
        MyAdapter adapter = new MyAdapter(lstImages, MainActivity.this);
        pager.setAdapter(adapter);


        /*mVideoView = (VideoView) findViewById(R.id.bgVideoView);

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.powerfm_fbcover);

        mVideoView.setVideoURI(uri);
        mVideoView.start();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setLooping(true);
            }
        });*/
    }

    private void initData() {
        lstImages.add(R.drawable.powerfm_lusaka);
        lstImages.add(R.drawable.powerfm_kabwe);
        lstImages.add(R.drawable.power_tv);
    }

     //Glide.with(this).load("http://goo.gl/gEgYUd").into(imageView);
}
