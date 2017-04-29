package com.hai.picker;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.hai.mediapicker.entity.Photo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import cn.yang.galleryfinal.FunctionConfig;
import cn.yang.galleryfinal.GalleryFinal;
import cn.yang.galleryfinal.model.PhotoInfo;
import cn.yang.galleryfinal.permission.AfterPermissionGranted;
import cn.yang.galleryfinal.permission.EasyPermissions;

public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks {
    private GalleryFinal.OnMediaResultCallback mediaResultCallback;
    FunctionConfig functionConfig = new FunctionConfig.Builder()
            .setEnableEdit(true)
            .setEnableCrop(true)
            .setForceCrop(true)
            .setForceCropEdit(false)
            .setCropSquare(true)
            .setEnableCamera(true)
            .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    /**
     * 获取所有图片
     */
    @AfterPermissionGranted(GalleryFinal.PERMISSIONS_CODE_GALLERY)
    private void requestGalleryPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA,Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE)) {
            GalleryFinal.openCamera(10,functionConfig, mediaResultCallback);
        } else {
            EasyPermissions.requestPermissions(this, getString(cn.finalteam.galleryfinal.R.string.permissions_tips_ca),
                    GalleryFinal.TAKE_REQUEST_CODE, Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        System.gc();
    }

    public void onCLick2() {
        mediaResultCallback = new GalleryFinal.OnMediaResultCallback() {
            @Override
            public void onHandlerSuccess(int requestCode, List<PhotoInfo> resultList) {
                Log.d("MainActivity", "resultList"+resultList.get(0).getPhotoPath());
            }

            @Override
            public void onHandlerFailure(int requestCode, String errorMsg) {

            }
        };
        if (Build.VERSION.SDK_INT > 22) {
            requestGalleryPermission();
        } else {

            GalleryFinal.openCamera(10,functionConfig, mediaResultCallback);
        }

        // GalleryFinal.openMultiSelect(10,10,mediaResultCallback,true);
    }

    public void click() {

        com.hai.mediapicker.util.GalleryFinal.selectMedias(this, 10, new com.hai.mediapicker.util.GalleryFinal.OnSelectMediaListener() {
            @Override
            public void onSelected(ArrayList<Photo> photoArrayList) {

            }
        });
    }

    @Override
    public void onPermissionsGranted(List<String> perms) {
        Log.d("MainActivity", "onPermissionsGranted");

    }

    @Override
    public void onPermissionsDenied(List<String> perms) {
            Log.d("MainActivity", "onPermissionsDenied");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        Log.d("MainActivity", grantResults[0] + "");
        if (grantResults[0]==0&&grantResults[1]==0){
            requestGalleryPermission();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

}
