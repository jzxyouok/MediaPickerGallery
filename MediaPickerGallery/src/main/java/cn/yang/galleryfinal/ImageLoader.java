/*
 * Copyright (C) 2014 pengjianbo(pengjianbosoft@gmail.com), Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.yang.galleryfinal;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;

import java.io.Serializable;

/**
 * Desction:imageloader抽象类，外部需要实现这个类去加载图片， GalleryFinal尽力减少对第三方库的依赖，所以这么干了
 * Author:pengjianbo
 * Date:15/10/10 下午5:27
 */
public interface ImageLoader extends Serializable {

    void displayImage(Context activity, String path, ImageView imageView);

    void displayImage(Context activity, String path, ImageView imageView, SimpleTarget<Bitmap> callback);

    void displayImage(Activity activity, String path, ImageView imageView, int defaultDrawable, int width, int height);

    void displayImage(Activity activity, String path, ImageView imageView, int defaultDrawable);

    void displayImageNoDefaultImg(Context activity, String path, ImageView imageView, SimpleTarget target);

    void displayImage(Context activity, String path, SimpleTarget<Bitmap> callback);

    void displayGifImage(Context activity, String path, ImageView imageView);

    void clearMemoryCache();

    void clearDiskCache(Context context);


}
