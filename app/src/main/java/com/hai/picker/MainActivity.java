package com.hai.picker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hai.mediapicker.entity.Photo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.yang.galleryfinal.GalleryFinal;
import cn.yang.galleryfinal.model.PhotoInfo;

public class MainActivity extends Activity {
  private GalleryFinal.OnMediaResultCallback  mediaResultCallback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.layout);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCLick2();
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click();
            }
        });


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void sendMedia(ArrayList<Photo> photoList) {
        Log.e("多媒体", photoList.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        System.gc();
    }
    public  void  onCLick2(){
        mediaResultCallback=new GalleryFinal.OnMediaResultCallback() {
            @Override
            public void onHandlerSuccess(int requestCode, List<PhotoInfo> resultList) {

            }

            @Override
            public void onHandlerFailure(int requestCode, String errorMsg) {

            }
        };
        GalleryFinal.openMultiSelect(10,10,mediaResultCallback,true);
    }
    public  void  click(){

        com.hai.mediapicker.util.GalleryFinal.selectMedias(this, 10, new com.hai.mediapicker.util.GalleryFinal.OnSelectMediaListener() {
            @Override
            public void onSelected(ArrayList<Photo> photoArrayList) {

            }
        });
    }
}
