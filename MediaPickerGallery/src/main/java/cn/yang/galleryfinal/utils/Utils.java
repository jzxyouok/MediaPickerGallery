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

package cn.yang.galleryfinal.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/12/7 下午7:32
 */
public class Utils {

    public static String getFileName(String pathandname) {
        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }

    /**
     * 保存Bitmap到文件
     *
     * @param bitmap
     * @param format
     * @param target
     */
    public static void saveBitmap(Bitmap bitmap, Bitmap.CompressFormat format, File target) {
        if (target.exists()) {
            target.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(target);
            bitmap.compress(format, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap rotateBitmap(String path, int orientation, int screenWidth, int screenHeight) {
        Bitmap bitmap = null;
        final int maxWidth = screenWidth / 2;
        final int maxHeight = screenHeight / 2;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int sourceWidth, sourceHeight;
            if (orientation == 90 || orientation == 270) {
                sourceWidth = options.outHeight;
                sourceHeight = options.outWidth;
            } else {
                sourceWidth = options.outWidth;
                sourceHeight = options.outHeight;
            }
            boolean compress = false;
            if (sourceWidth > maxWidth || sourceHeight > maxHeight) {
                float widthRatio = (float) sourceWidth / (float) maxWidth;
                float heightRatio = (float) sourceHeight / (float) maxHeight;

                options.inJustDecodeBounds = false;
                if (new File(path).length() > 512000) {
                    float maxRatio = Math.max(widthRatio, heightRatio);
                    options.inSampleSize = (int) maxRatio;
                    compress = true;
                }
                bitmap = BitmapFactory.decodeFile(path, options);
            } else {
                bitmap = BitmapFactory.decodeFile(path);
            }
            if (orientation > 0) {
                Matrix matrix = new Matrix();
                //matrix.postScale(sourceWidth, sourceHeight);
                matrix.postRotate(orientation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            sourceWidth = bitmap.getWidth();
            sourceHeight = bitmap.getHeight();
            if ((sourceWidth > maxWidth || sourceHeight > maxHeight) && compress) {
                float widthRatio = (float) sourceWidth / (float) maxWidth;
                float heightRatio = (float) sourceHeight / (float) maxHeight;
                float maxRatio = Math.max(widthRatio, heightRatio);
                sourceWidth = (int) ((float) sourceWidth / maxRatio);
                sourceHeight = (int) ((float) sourceHeight / maxRatio);
                Bitmap bm = Bitmap.createScaledBitmap(bitmap, sourceWidth, sourceHeight, true);
                bitmap.recycle();
                return bm;
            }
        } catch (Exception e) {
        }
        return bitmap;
    }

    /***
     * 获取图片旋转信息。转正图片
     ****/
    public static String readPictureDegree(String path) {
        int degree = 0;
        SoftReference<Bitmap> bitmap = null;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            degree = orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90 : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180 : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270 : 0;
            if (degree == 0) {
                return path;
            } else {// 需要将图片翻转
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inJustDecodeBounds = true;
                FileInputStream stream1 = new FileInputStream(new File(path));
                BitmapFactory.decodeStream(stream1, null, option);
                int width_tmp = option.outWidth, height_tmp = option.outHeight;
                int scale = 1;
                stream1.close();
                int scalex = width_tmp / 720;
                int scaley = height_tmp / 720;
                scale = scalex > scaley ? scaley : scalex;
                if (scale < 1) {
                    scale = 1;
                }
                BitmapFactory.Options option2 = new BitmapFactory.Options();
                option2.inSampleSize = scale;
                FileInputStream stream2 = new FileInputStream(new File(path));
                bitmap = new SoftReference<>(BitmapFactory.decodeStream(stream2, null, option2));
                stream2.close();
                Matrix matrix = new Matrix();
                matrix.postRotate(degree);
                bitmap = new SoftReference<>(Bitmap.createBitmap(bitmap.get(), 0, 0, bitmap.get().getWidth(), bitmap.get().getHeight(), matrix, true));
                File fileDir = new File(SdCardUtil.SABEIMAGE);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                path = SdCardUtil.SABEIMAGE + UUID.randomUUID().toString().replace("-", "") + ".jpg";
                File file = new File(path);
                bitmap.get().compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null && bitmap.get() != null) {
                bitmap.get().recycle();
            }
        }
        return path;
    }

    /**
     * 取某个范围的任意数
     *
     * @param min
     * @param max
     * @return
     */
    public static int getRandom(int min, int max) {
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return s;
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }
    /**
     * 把bitmap,png格式的图片 转换成jpg图片
     * 因jpg不支持透明，如png透明图片，则转成白底！
     * @param bitmap     源图
     * @param newImgpath 新图片的路径
     */
    public static String saveJPG(Bitmap bitmap, String newImgpath) {
        Bitmap outB = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outB);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        try {
            File file = new File(newImgpath);
            FileOutputStream out = new FileOutputStream(file);
            if (outB.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
            return newImgpath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 判断url是否为网址
     *
     * @param url  链接
     * @return boolean
     */
    public static boolean isHttp(String url) {
        if (null == url) return false;
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }
}
