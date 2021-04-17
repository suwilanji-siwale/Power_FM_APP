package com.melborp.suwilanji.powerfm;

import android.content.Context;
import android.content.Intent;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import com.melborp.suwilanji.powerfm.R;

/**
 * Created by reale on 4/20/2017.
 */

public class MyAdapter  extends PagerAdapter{

    List<Integer> lstImages;
    Context context;
    LayoutInflater layoutInflater;


    public MyAdapter(List<Integer> lstImages, Context context) {
        this.lstImages = lstImages;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return lstImages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = layoutInflater.inflate(R.layout.card_item,container,false);
        ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
        imageView.setImageResource(lstImages.get(position));
        container.addView(view);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               switch(position){
                   case 0:
                       Intent intent = new Intent(context, PowerfmKabwe.class);
                       context.startActivity(intent);
                       Toast.makeText(context, "Power FM Lusaka", Toast.LENGTH_SHORT).show();
                       break;
                   case 1:
                       Intent intent2 = new Intent(context, PowerfmKabwe.class);
                       context.startActivity(intent2);
                       Toast.makeText(context, "Power FM Kabwe", Toast.LENGTH_SHORT).show();
                       break;
                   case 2:
                       Toast.makeText(context, "Power TV", Toast.LENGTH_SHORT).show();
                       break;
               }
            }
        });
        return view;
    }
}
