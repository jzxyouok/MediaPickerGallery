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

package cn.yang.galleryfinal.model;

import android.widget.ImageView;

import java.io.Serializable;

/**
 * Desction:图片信息
 * Author:pengjianbo
 * Date:15/7/30 上午11:23
 */
public class PhotoInfo implements Serializable, Comparable<PhotoInfo> {


    private int photoId;
    private String photoPath;
    private int width;
    private int height;
    private boolean isPic;
    private boolean isGifPic;

    private ImageView imageView;
    private String thumbnailVideoPath;  //视频缩略图
    private long videoAddTime;  //视频添加时间
    private long FileSize;
    private  long duration; //视频时长

    public PhotoInfo() {
    }

    public long getDuration() {
        return duration;
    }

    public PhotoInfo setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public boolean isGifPic() {
        return isGifPic;
    }

    public void setGifPic(boolean gifPic) {
        isGifPic = gifPic;
    }

    public long getFileSize() {
        return FileSize;
    }

    public void setFileSize(long fileSize) {
        this.FileSize = fileSize;
    }

    public long getVideoAddTime() {
        return videoAddTime;
    }

    public void setVideoAddTime(long videoAddTime) {
        this.videoAddTime = videoAddTime;
    }

    public boolean isPic() {
        return isPic;
    }

    public void setPic(boolean pic) {
        isPic = pic;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public String getThumbnailVideoPath() {
        return thumbnailVideoPath;
    }

    public void setThumbnailVideoPath(String thumbnailVideoPath) {
        this.thumbnailVideoPath = thumbnailVideoPath;
    }

    @Override
    public int compareTo(PhotoInfo another) {
        if (this.videoAddTime > another.getVideoAddTime()) {
            return -1;
        } else if (this.videoAddTime < another.getVideoAddTime()) {
            return 1;
        } else {
            return 0;
        }
    }
}
