package cn.yang.galleryfinal.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;

import cn.finalteam.galleryfinal.R;
import cn.yang.galleryfinal.ImageLoader;

/**
 * Created by yangc on 2017/4/5.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:使用Picasso加载图片
 */

public class DefaultImageLoader implements ImageLoader {


    @Override
    public void displayImage(Context activity, String path, ImageView imageView) {
        DrawableTypeRequest glide;

        if (!Utils.isHttp(path)){
            glide = Glide.with(activity).load("file:///"+path);
        }else {
            glide = Glide.with(activity).load(path);
        }
        if (Build.VERSION.SDK_INT > 22) {
            glide.dontAnimate();
        }
        glide.placeholder(cn.finalteam.galleryfinal.R.drawable.img_default)
                .priority(Priority.IMMEDIATE)
                .error(R.drawable.ic_gf_loading_error)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);

    }

    @Override
    public void displayImage(Context activity, String path, ImageView imageView, SimpleTarget<Bitmap> target) {
        DrawableTypeRequest glide;
        glide = Glide.with(activity).load(path);
        if (Utils.isHttp(path)) {

        } else {
            glide.override(500, 500);
        }
        if (Build.VERSION.SDK_INT > 22) {
            glide.dontAnimate();
        }
        glide.asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .placeholder(cn.finalteam.galleryfinal.R.drawable.img_default)
                .fitCenter()
                .error(R.drawable.ic_gf_loading_error)
                .into(target);
    }

    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int defaultDrawable, int width, int height) {
        Glide.with(activity).load(path).placeholder(defaultDrawable).override(width, height).skipMemoryCache(true).priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
    }


    @Override
    public void displayImage(Context activity, String path, SimpleTarget<Bitmap> target) {
        DrawableTypeRequest glide;
        glide = Glide.with(activity).load(path);
        if (Build.VERSION.SDK_INT > 22) {
            glide.dontAnimate();
        }
        glide.asBitmap()
                .placeholder(cn.finalteam.galleryfinal.R.drawable.img_default)
                .error(R.drawable.ic_gf_loading_error)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(target);
    }


    @Override
    public void displayImage(Activity activity, String path, ImageView imageView, int defaultDrawable) {
        Glide.with(activity).load(path)
                .placeholder(defaultDrawable)
                .priority(Priority.IMMEDIATE)
                .error(R.drawable.ic_gf_loading_error)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
    }

    @Override
    public void displayImageNoDefaultImg(Context activity, String path, ImageView imageView, SimpleTarget target) {
        DrawableTypeRequest glide;
        glide = Glide.with(activity).load(path);
        if (Build.VERSION.SDK_INT > 22) {
            glide.dontAnimate();
        }
        glide.asBitmap()
                .skipMemoryCache(true)
                .error(R.drawable.ic_gf_loading_error)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(target);
    }

    @Override
    public void clearMemoryCache() {
        // 必须在UI线程中调用
        // Glide.get(UserModel.getInstance().getContext()).clearMemory();
    }

    @Override
    public void clearDiskCache(final Context context) {
        // 必须在UI线程中调用
        Glide.get(context).clearMemory();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Glide.get(context).clearDiskCache();
            }
        });
    }

    @Override
    public void displayGifImage(Context activity, String path, ImageView imageView) {
        DrawableTypeRequest glide;
        glide = Glide.with(activity).load(path);
        glide.asBitmap()
                .placeholder(cn.finalteam.galleryfinal.R.drawable.img_default)
                .error(R.drawable.ic_gf_loading_error)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);
    }
}