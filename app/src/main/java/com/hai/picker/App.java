package com.hai.picker;

import android.app.Application;
import android.graphics.Color;

import com.squareup.leakcanary.LeakCanary;

import cn.yang.galleryfinal.CoreConfig;
import cn.yang.galleryfinal.FunctionConfig;
import cn.yang.galleryfinal.GalleryFinal;
import cn.yang.galleryfinal.ThemeConfig;
import cn.yang.galleryfinal.utils.DefaultImageLoader;

/**
 * Created by Administrator on 2017/3/17.
 */

public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        initGalleryFinal();
    }

    /**
     * 相册加载初始化
     */
    private void initGalleryFinal() {
        //设置主题
//        ThemeConfig theme = new ThemeConfig.Builder()
//                .setTitleBarBgColor(Color.rgb(0x2B, 0xAE, 0xFD))
//                .setCheckSelectedColor(Color.rgb(0x2B, 0xAE, 0xFD))
//                .setCropControlColor(Color.rgb(0x2B, 0xAE, 0xFD))
//                .setFabNornalColor(Color.rgb(0x2B, 0xAE, 0xFD))
//                .setFabPressedColor(Color.rgb(0x00, 0x7F, 0xCC))
//                .setIconBack(R.drawable.back_selector)
//                .build();
        //配置功能
        FunctionConfig functionConfig = new FunctionConfig.Builder()
                .setMutiSelectMaxSize(9)
                .setEnableCamera(true)//开启相机功能
                .setEnableEdit(false)
                .setEnableCrop(false)
                .setForceCrop(true)
                .setForceCropEdit(true)
                .setEnableRotate(true)
                .setCropSquare(true)
                .setEnablePreview(true)
                .build();
        //配置imageloader
        CoreConfig coreConfig = new CoreConfig.Builder(this, new DefaultImageLoader(), ThemeConfig.CYAN)
                .setFunctionConfig(functionConfig)
                .setNoAnimcation(true)
                .build();
        GalleryFinal.init(coreConfig);
    }
}
