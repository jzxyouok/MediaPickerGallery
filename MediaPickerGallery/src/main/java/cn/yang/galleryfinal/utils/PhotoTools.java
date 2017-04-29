
package cn.yang.galleryfinal.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.yang.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.R;
import cn.yang.galleryfinal.model.PhotoFolderInfo;
import cn.yang.galleryfinal.model.PhotoInfo;
import cn.finalteam.toolsfinal.io.FilenameUtils;

/**
 * Desction:
 * Author:pengjianbo
 * Date:15/10/10 下午4:26
 */
public class PhotoTools {
    private static final String TAG = "PhotoTools";

    /**
     * 获取所有图片&视频
     *
     * @param context        上下文
     * @param selectPhotoMap 列表集合
     * @param isLoadVideo    是否加载视频
     * @return List<PhotoFolderInfo>
     */
    public static List<PhotoFolderInfo> loadAllPhotoFolder(Context context, Map<String, PhotoInfo> selectPhotoMap, boolean isLoadVideo) {
        List<PhotoFolderInfo> allFolderList = new ArrayList<>();
        final String[] projectionPhotos = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.SIZE
        };

        ArrayList<PhotoFolderInfo> allPhotoFolderList = new ArrayList<>();
        HashMap<Integer, PhotoFolderInfo> bucketMap = new HashMap<>();//存放文件夹名称
        Cursor cursor = null;
        Cursor dataVideo = null;
        //所有图片
        PhotoFolderInfo allPhotoFolderInfo = new PhotoFolderInfo();
        allPhotoFolderInfo.setFolderId(0);
        allPhotoFolderInfo.setFolderName(context.getResources().getString(R.string.all_photo));
        allPhotoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
        allPhotoFolderList.add(0, allPhotoFolderInfo);
        List<String> selectedList = GalleryFinal.getFunctionConfig().getSelectedList();
        List<String> filterList = GalleryFinal.getFunctionConfig().getFilterList();
        try {
            cursor = MediaStore.Images.Media.query(context.getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    , projectionPhotos, "", null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            if (cursor != null) {
                int bucketNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                final int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
                while (cursor.moveToNext()) {
                    int bucketId = cursor.getInt(bucketIdColumn);
                    String bucketName = cursor.getString(bucketNameColumn);
                    final int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    final int imageIdColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                    final int imageWidthColumn = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
                    final int imageHeightColumn = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
                    long imageSize =cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE));
                    long photoAddTime = cursor.getLong(cursor.getColumnIndexOrThrow(projectionPhotos[4]));
                    final int imageId = cursor.getInt(imageIdColumn);
                    final String path = cursor.getString(dataColumn);
                    Log.d(TAG, "PhotoTools" + imageSize);
                    //TODO GIF开关
                    if (!isLoadVideo && path.endsWith("gif")) {
                        Log.d(TAG, "GIF开关" + path);
                        continue;
                    }
                    int imgWidth = cursor.getInt(imageWidthColumn);
                    int imgHeight = cursor.getInt(imageHeightColumn);
                    //final String thumb = cursor.getString(thumbImageColumn);
                    File file = new File(path);
                    if ((filterList == null || !filterList.contains(path)) && file.exists() && file.length() > 0) {
                        final PhotoInfo photoInfo = new PhotoInfo();
                        photoInfo.setPhotoId(imageId);
                        photoInfo.setPhotoPath(path);
                        //photoInfo.setThumbPath(thumb);
                        if (allPhotoFolderInfo.getCoverPhoto() == null) {
                            allPhotoFolderInfo.setCoverPhoto(photoInfo);
                        }
                        photoInfo.setPic(true);
                        if (path.endsWith("gif")) {
                            photoInfo.setGifPic(true);
                        } else {
                            photoInfo.setGifPic(false);
                        }
                        photoInfo.setVideoAddTime(photoAddTime);
                        photoInfo.setWidth(imgWidth);
                        photoInfo.setHeight(imgHeight);
                        photoInfo.setFileSize(imageSize);
                        //添加到所有图片
                        allPhotoFolderInfo.getPhotoList().add(photoInfo);

                        //通过bucketId获取文件夹
                        PhotoFolderInfo photoFolderInfo = bucketMap.get(bucketId);

                        if (photoFolderInfo == null) {
                            photoFolderInfo = new PhotoFolderInfo();
                            photoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
                            photoFolderInfo.setFolderId(bucketId);
                            photoFolderInfo.setFolderName(bucketName);
                            photoFolderInfo.setCoverPhoto(photoInfo);
                            bucketMap.put(bucketId, photoFolderInfo);
                            allPhotoFolderList.add(photoFolderInfo);
                        }
                        photoFolderInfo.getPhotoList().add(photoInfo);

                        if (selectedList != null && selectedList.size() > 0 && selectedList.contains(path)) {
                            selectPhotoMap.put(path, photoInfo);
                        }
                    }
                }
            }
            if (isLoadVideo) {
                final String[] projectionVideos = new String[]{
                        MediaStore.Video.Media.DATA,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATE_ADDED,
                        MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.BUCKET_ID,
                        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Video.Media.SIZE,
                        MediaStore.Video.Media.DURATION
                };
                //所有视频
                PhotoFolderInfo allVideoFolderInfo = new PhotoFolderInfo();
                allVideoFolderInfo.setFolderId(1);
                allVideoFolderInfo.setFolderName(context.getResources().getString(R.string.all_video));
                allVideoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
                allPhotoFolderList.add(1, allVideoFolderInfo);
                // 得到一个游标
                dataVideo = MediaStore.Video.query(context.getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projectionVideos);
                if (dataVideo != null) {
                    while (dataVideo.moveToNext()) {
                        //查询数据
                        String videoPath = dataVideo.getString(dataVideo.getColumnIndexOrThrow(projectionVideos[0]));
                        String videoName = dataVideo.getString(dataVideo.getColumnIndexOrThrow(projectionVideos[1]));
                        long videoAddTime = dataVideo.getLong(dataVideo.getColumnIndexOrThrow(projectionVideos[2]));
                        int videoId = dataVideo.getInt(dataVideo.getColumnIndexOrThrow(projectionVideos[3]));
                        int videoBucketId = dataVideo.getInt(dataVideo.getColumnIndexOrThrow(projectionVideos[4]));
                        String bucketName = dataVideo.getString(dataVideo.getColumnIndexOrThrow(projectionVideos[5]));
                        long videoSize = dataVideo.getLong(dataVideo.getColumnIndexOrThrow(projectionVideos[6]));
                        long duration = (long) Math.ceil(dataVideo.getLong(dataVideo.getColumnIndexOrThrow(projectionVideos[7])) / 1000.0);
                        Log.d(TAG, "duration4:" + duration);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(videoAddTime * 1000);
                        Log.d(TAG, "转换后时间：" + sdf.format(date) + "   视频日期：" + videoAddTime + "   视频路径：" + videoPath);

                        //判断视频大小 大于200MB不添加
                        if (!formetFileSize(videoSize)) {
                            Log.d(TAG, "videoName:" + videoName);
                            continue;
                        }

                        String ext = FilenameUtils.getExtension(videoPath);
                        if (!ext.equalsIgnoreCase("mp4")) {//只添加后缀名mp4的视频文件
                            Log.d(TAG, "videoName:" + videoName);
                            continue;
                        }
                        File file = new File(videoPath);
                        if ((filterList == null || !filterList.contains(videoPath)) && file.exists() && file.length() > 0) {
                            final PhotoInfo videoInfo = new PhotoInfo();
                            videoInfo.setPhotoId(videoId);
                            videoInfo.setPhotoPath(videoPath);
                            videoInfo.setDuration(duration);
                            videoInfo.setVideoAddTime(videoAddTime * 1000);
                            if (allVideoFolderInfo.getCoverPhoto() == null) {
                                allVideoFolderInfo.setCoverPhoto(videoInfo);
                            }
                            videoInfo.setPic(false);
                            videoInfo.setFileSize(videoSize);
                            //添加到所有视频
                            allVideoFolderInfo.getPhotoList().add(videoInfo);
                            allPhotoFolderInfo.getPhotoList().add(videoInfo);
                            //通过bucketId获取文件夹
                            PhotoFolderInfo videoFolderInfo = bucketMap.get(videoBucketId);

                            if (videoFolderInfo == null) {
                                videoFolderInfo = new PhotoFolderInfo();
                                videoFolderInfo.setPhotoList(new ArrayList<PhotoInfo>());
                                videoFolderInfo.setFolderId(videoBucketId);
                                videoFolderInfo.setFolderName(bucketName);
                                videoFolderInfo.setCoverPhoto(videoInfo);
                                bucketMap.put(videoBucketId, videoFolderInfo);
                                allPhotoFolderList.add(videoFolderInfo);
                            }
                            videoFolderInfo.getPhotoList().add(videoInfo);

                            if (selectedList != null && selectedList.size() > 0 && selectedList.contains(videoPath)) {
                                selectPhotoMap.put(videoPath, videoInfo);
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ILogger.e(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (dataVideo != null) {
                dataVideo.close();
            }
        }
        allFolderList.addAll(allPhotoFolderList);
        if (selectedList != null) {
            selectedList.clear();
        }
        if (isLoadVideo && null != allFolderList.get(1).getPhotoList()) {
            Collections.sort(allFolderList.get(1).getPhotoList());//给视频排序
        }
        Collections.sort(allFolderList.get(0).getPhotoList());//给照片和视频排序
        return allFolderList;
    }

    /**
     * 转换文件大小
     */
    public static boolean formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        boolean flag = true;
        if (((int) fileS / 1048576) >= 200) {
            flag = false;
        }
        return flag;
    }

}
