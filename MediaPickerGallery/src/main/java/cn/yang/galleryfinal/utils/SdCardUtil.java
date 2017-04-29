package cn.yang.galleryfinal.utils;


import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.UUID;

public class SdCardUtil {
    // 项目文件根目录
    public static final String FILEDIR = "cloudMedicalSociety";
    // 应用程序图片存放
    public static final String FILEIMAGE = File.separator + "images" + File.separator;
    // 应用程序缓存
    public static final String SABEIMAGE = getSdPath() + FILEDIR + FILEIMAGE;//文件保存路径

    public static String getSdPath() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        } else {
            return Environment.getDataDirectory().getAbsolutePath() + "/";
        }
    }

    /****
     * 生成filekey
     ***/
    public static String getFileKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /****
     * 文件存放地址
     *
     * @param context 上下文
     * @param type    存放文件的类型  使用 Environment
     **/
    public static String getDiskFileDir(Context context, String type) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalFilesDir(type).getPath();
        } else {
            cachePath = context.getFilesDir().getPath();
        }
        return cachePath;
    }


}
