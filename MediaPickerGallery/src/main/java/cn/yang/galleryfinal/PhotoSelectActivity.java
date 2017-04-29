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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.finalteam.galleryfinal.R;
import cn.yang.galleryfinal.adapter.FolderListAdapter;
import cn.yang.galleryfinal.adapter.GalleryAdapter;
import cn.yang.galleryfinal.decoration.SpaceItemDecoration;
import cn.yang.galleryfinal.model.PhotoFolderInfo;
import cn.yang.galleryfinal.model.PhotoInfo;
import cn.yang.galleryfinal.permission.AfterPermissionGranted;
import cn.yang.galleryfinal.permission.EasyPermissions;
import cn.yang.galleryfinal.utils.PhotoTools;
import cn.yang.galleryfinal.utils.SdCardUtil;
import cn.yang.galleryfinal.utils.Utils;
import cn.finalteam.toolsfinal.DeviceUtils;
import cn.finalteam.toolsfinal.StringUtils;
import cn.finalteam.toolsfinal.io.FilenameUtils;

/**
 * Desction:图片选择器
 * Author:pengjianbo
 * Date:15/10/10 下午3:54
 */
public class PhotoSelectActivity extends PhotoBaseActivity implements View.OnClickListener {

    private final int HANLDER_TAKE_PHOTO_EVENT = 1000;
    private final int HANDLER_REFRESH_LIST_EVENT = 1002;
    private ListView mLvFolderList;
    private LinearLayout mLlFolderPanel;
    private ImageView mIvTakePhoto;
    private ImageView mIvBack;
    private ImageView mIvClear;
    private Button mIvPreView;//预览
    private TextView mTvSubTitle;
    private Button mFabOk;//发送按钮
    private TextView mTvEmptyView;
    private RelativeLayout mTitlebar;
    private TextView iv_photo_click;
    private List<PhotoFolderInfo> mAllPhotoFolderList;
    private FolderListAdapter mFolderListAdapter;
    private List<PhotoInfo> mCurPhotoList;
    private GalleryAdapter mPhotoListAdapter;
    private RecyclerView mGvPhotoList;
    boolean isClickVideo = false;
    //是否需要刷新相册
    private boolean mHasRefreshGallery = false;
    private LinkedHashMap<String, PhotoInfo> mSelectPhotoMap = new LinkedHashMap<>();
    GridLayoutManager manager;
    private boolean isLoadVideo;
    private RelativeLayout footer_bar;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selectPhotoMap", mSelectPhotoMap);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANLDER_TAKE_PHOTO_EVENT) {
                PhotoInfo photoInfo = (PhotoInfo) msg.obj;
                takeRefreshGallery(photoInfo);
                refreshSelectCount();
            } else if (msg.what == HANDLER_REFRESH_LIST_EVENT) {
                refreshSelectCount();
                mPhotoListAdapter.notifyDataSetChanged();
                mFolderListAdapter.notifyDataSetChanged();
                if (mAllPhotoFolderList.get(0).getPhotoList() == null ||
                        mAllPhotoFolderList.get(0).getPhotoList().size() == 0) {
                    if (isLoadVideo){
                        mTvEmptyView.setText(R.string.no_photo);
                    }else {
                        mTvEmptyView.setText(R.string.no_photo_and_video);
                    }

                }else {
                    mTvEmptyView.setVisibility(View.GONE);
                }
                mGvPhotoList.setEnabled(true);
//                mLlTitle.setEnabled(true);
                iv_photo_click.setEnabled(true);
                mIvTakePhoto.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GalleryFinal.getFunctionConfig() == null || GalleryFinal.getGalleryTheme() == null) {
            resultFailureDelayed(getString(R.string.please_reopen_gf), true);
        } else {
            setContentView(R.layout.gf_activity_photo_select);
            mPhotoTargetFolder = null;
            Intent intent = getIntent();
            isLoadVideo = intent.getBooleanExtra("isLoadVideo", true);
            findViews();
            mAllPhotoFolderList = new ArrayList<>();
            mFolderListAdapter = new FolderListAdapter(this, mAllPhotoFolderList, GalleryFinal.getFunctionConfig());
            mLvFolderList.setAdapter(mFolderListAdapter);
            mCurPhotoList = new ArrayList<>();
            //   mPhotoListAdapter = new PhotoListAdapter(this, mCurPhotoList, mSelectPhotoMap, mScreenWidth);
            mPhotoListAdapter = new GalleryAdapter(this, mCurPhotoList, mSelectPhotoMap);
            mGvPhotoList.setAdapter(mPhotoListAdapter);
            //TODO 判断是单选还是多选
            if (GalleryFinal.getFunctionConfig().isMutiSelect()) {
                mFabOk.setVisibility(View.VISIBLE);
                footer_bar.setVisibility(View.VISIBLE);
                NoDoubleClickListener.MIN_CLICK_DELAY_TIME = 300;
            } else {
                NoDoubleClickListener.MIN_CLICK_DELAY_TIME = 1000;
            }

            setTheme();
            //  mGvPhotoList.setEmptyView(mTvEmptyView);
            if (GalleryFinal.getFunctionConfig().isCamera()) {
                //TODO 隐藏相册上方按钮
                mIvTakePhoto.setVisibility(View.GONE);
            } else {
                mIvTakePhoto.setVisibility(View.GONE);
            }
            refreshSelectCount();
            requestGalleryPermission();
            setListener();
        }
        Global.mPhotoSelectActivity = this;
        if (isLoadVideo) {
            mTvSubTitle.setText(R.string.all_photo);
        } else {
            mTvSubTitle.setText(R.string.gallery);
        }
    }

    private void setTheme() {
        mIvBack.setImageResource(GalleryFinal.getGalleryTheme().getIconBack());
        if (GalleryFinal.getGalleryTheme().getIconBack() == R.drawable.ic_gf_back_selector) {
            mIvBack.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }

//        mIvFolderArrow.setImageResource(GalleryFinal.getGalleryTheme().getIconFolderArrow());
//        if (GalleryFinal.getGalleryTheme().getIconFolderArrow() == R.drawable.ic_gf_triangle_arrow) {
//            mIvFolderArrow.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }

        mIvClear.setImageResource(GalleryFinal.getGalleryTheme().getIconClear());
        if (GalleryFinal.getGalleryTheme().getIconClear() == R.drawable.ic_gf_clear) {
            mIvClear.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }

//        mIvPreView.setImageResource(GalleryFinal.getGalleryTheme().getIconPreview());
//        if (GalleryFinal.getGalleryTheme().getIconPreview() == R.drawable.ic_gf_preview) {
//            mIvPreView.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
//        }

        mIvTakePhoto.setImageResource(GalleryFinal.getGalleryTheme().getIconCamera());
        if (GalleryFinal.getGalleryTheme().getIconCamera() == R.drawable.ic_gf_camera) {
            mIvTakePhoto.setColorFilter(GalleryFinal.getGalleryTheme().getTitleBarIconColor());
        }
//        mFabOk.setIcon(GalleryFinal.getGalleryTheme().getIconFab());

        mTitlebar.setBackgroundColor(GalleryFinal.getGalleryTheme().getTitleBarBgColor());
        mTvSubTitle.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
//        mTvTitle.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
        mFabOk.setTextColor(GalleryFinal.getGalleryTheme().getTitleBarTextColor());
//        mFabOk.setColorPressed(GalleryFinal.getGalleryTheme().getFabPressedColor());
//        mFabOk.setColorNormal(GalleryFinal.getGalleryTheme().getFabNornalColor());
    }

    private void findViews() {
        manager = new GridLayoutManager(this, 3);
        mGvPhotoList = (RecyclerView) findViewById(R.id.gv_photo_list);
        mGvPhotoList.setLayoutManager(manager);
        mLvFolderList = (ListView) findViewById(R.id.lv_folder_list);
        mTvSubTitle = (TextView) findViewById(R.id.iv_photo_title);
        mLlFolderPanel = (LinearLayout) findViewById(R.id.ll_folder_panel);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mFabOk = (Button) findViewById(R.id.fab_ok);
        mTvEmptyView = (TextView) findViewById(R.id.tv_empty_view);
        mGvPhotoList.addItemDecoration(new SpaceItemDecoration(this, 1));
        mGvPhotoList.setHasFixedSize(true);
        mTvEmptyView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
//        mLlTitle = (LinearLayout) findViewById(R.id.ll_title);
        //修改后
        iv_photo_click = (TextView) findViewById(R.id.iv_photo_click);
        mIvClear = (ImageView) findViewById(R.id.iv_clear);
        mTitlebar = (RelativeLayout) findViewById(R.id.titlebar);
//        mIvFolderArrow = (ImageView) findViewById(R.id.iv_folder_arrow);
        mIvPreView = (Button) findViewById(R.id.iv_preview);
        footer_bar = (RelativeLayout) findViewById(R.id.footer_bar);
    }

    private void setListener() {
//        mLlTitle.setOnClickListener(this);
        iv_photo_click.setOnClickListener(this);
        mIvTakePhoto.setOnClickListener(this);
        mIvBack.setOnClickListener(this);
//        mIvFolderArrow.setOnClickListener(this);
        mLvFolderList.setOnItemClickListener(noDoubleClickListener);
        mPhotoListAdapter.setOnItemClickListener(noDoubleClickListener);
        mFabOk.setOnClickListener(this);
        mIvClear.setOnClickListener(this);
        mIvPreView.setOnClickListener(this);
    }

    protected void deleteSelect(int photoId) {
        try {
            Iterator<Map.Entry<String, PhotoInfo>> entries = mSelectPhotoMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, PhotoInfo> entry = entries.next();
                if (entry.getValue() != null && entry.getValue().getPhotoId() == photoId) {
                    entries.remove();
                }
            }
        } finally {
            refreshAdapter();
        }


    }

    private void refreshAdapter() {
        mHandler.sendEmptyMessageDelayed(HANDLER_REFRESH_LIST_EVENT, 100);
    }

    protected void takeRefreshGallery(PhotoInfo photoInfo, boolean selected) {
        if (isFinishing() || photoInfo == null) {
            return;
        }

        Message message = mHandler.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;
        mSelectPhotoMap.put(photoInfo.getPhotoPath(), photoInfo);
        mHandler.sendMessageDelayed(message, 100);
    }

    /**
     * 解决在5.0手机上刷新Gallery问题，从startActivityForResult回到Activity把数据添加到集合中然后理解跳转到下一个页面，
     * adapter的getCount与list.size不一致，所以我这里用了延迟刷新数据
     *
     * @param photoInfo 集合
     */
    private void takeRefreshGallery(PhotoInfo photoInfo) {
        mCurPhotoList.add(0, photoInfo);
        mPhotoListAdapter.notifyDataSetChanged();

        //添加到集合中
        List<PhotoInfo> photoInfoList = mAllPhotoFolderList.get(0).getPhotoList();
        if (photoInfoList == null) {
            photoInfoList = new ArrayList<>();
        }
        photoInfoList.add(0, photoInfo);
        mAllPhotoFolderList.get(0).setPhotoList(photoInfoList);

        if (mFolderListAdapter.getSelectFolder() != null) {
            PhotoFolderInfo photoFolderInfo = mFolderListAdapter.getSelectFolder();
            List<PhotoInfo> list = photoFolderInfo.getPhotoList();
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(0, photoInfo);
            if (list.size() == 1) {
                photoFolderInfo.setCoverPhoto(photoInfo);
            }
            mFolderListAdapter.getSelectFolder().setPhotoList(list);
        } else {
            String folderA = new File(photoInfo.getPhotoPath()).getParent();
            for (int i = 1; i < mAllPhotoFolderList.size(); i++) {
                PhotoFolderInfo folderInfo = mAllPhotoFolderList.get(i);
                String folderB = null;
                if (!StringUtils.isEmpty(photoInfo.getPhotoPath())) {
                    folderB = new File(photoInfo.getPhotoPath()).getParent();
                }
                if (TextUtils.equals(folderA, folderB)) {
                    List<PhotoInfo> list = folderInfo.getPhotoList();
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(0, photoInfo);
                    folderInfo.setPhotoList(list);
                    if (list.size() == 1) {
                        folderInfo.setCoverPhoto(photoInfo);
                    }
                }
            }
        }

        mFolderListAdapter.notifyDataSetChanged();
    }

    @Override
    protected void takeResult(PhotoInfo photoInfo) {

        Message message = mHandler.obtainMessage();
        message.obj = photoInfo;
        message.what = HANLDER_TAKE_PHOTO_EVENT;

        if (!GalleryFinal.getFunctionConfig().isMutiSelect()) { //单选
            mSelectPhotoMap.clear();
            mSelectPhotoMap.put(photoInfo.getPhotoPath(), photoInfo);

            if (GalleryFinal.getFunctionConfig().isEditPhoto()) {//裁剪
                mHasRefreshGallery = true;
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(photoInfo);
                resultData(list);
            }

            mHandler.sendMessageDelayed(message, 100);
        } else {//多选
            mSelectPhotoMap.put(photoInfo.getPhotoPath(), photoInfo);
            mHandler.sendMessageDelayed(message, 100);
        }
    }

    /**
     * 执行裁剪
     */
    protected void toPhotoEdit() {
        convertPhoto();
        Intent intent = new Intent(this, PhotoEditActivity.class);
        intent.putExtra(PhotoEditActivity.SELECT_MAP, mSelectPhotoMap);
        startActivity(intent);
    }

    /**
     * 转换要裁剪的图片格式为JPG
     * 兼容 Android 6.0 ExifInterface constructor throws IOExxception
     */
    public void convertPhoto() {
        Collection<PhotoInfo> c = mSelectPhotoMap.values();
        Iterator it = c.iterator();
        PhotoInfo photoInfo = (PhotoInfo) it.next();
        String ext = FilenameUtils.getExtension(photoInfo.getPhotoPath());
        if (!ext.equalsIgnoreCase("png")) return;
        String newFile = Utils.saveJPG(BitmapFactory.decodeFile(photoInfo.getPhotoPath()), SdCardUtil.SABEIMAGE + SdCardUtil.getFileKey() + ".jpg");
        photoInfo.setPhotoPath(newFile);
        mSelectPhotoMap.clear();
        mSelectPhotoMap.put(newFile, photoInfo);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        //点击相册目录:ll_title,iv_folder_arrow

        if (id == R.id.iv_photo_click) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
                mLlFolderPanel.setVisibility(View.GONE);
                mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.gf_flip_horizontal_out));
            } else {
                mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(this, R.anim.gf_flip_horizontal_in));
                mLlFolderPanel.setVisibility(View.VISIBLE);
            }
        } else if (id == R.id.iv_take_photo) {
            //判断是否达到多选最大数量
            if (GalleryFinal.getFunctionConfig().isMutiSelect() && mSelectPhotoMap.size() == GalleryFinal.getFunctionConfig().getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            }

            if (!DeviceUtils.existSDCard()) {
                toast(getString(R.string.empty_sdcard));
                return;
            }

            takePhotoAction();
        } else if (id == R.id.iv_back) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
//                mLlTitle.performClick();
                iv_photo_click.performClick();
            } else {
                finish();
            }
        } else if (id == R.id.fab_ok) {
            //TODO 选择完成
            if (mSelectPhotoMap.size() > 0) {
                ArrayList<PhotoInfo> photoList = new ArrayList<>(mSelectPhotoMap.values());
                if (!GalleryFinal.getFunctionConfig().isEditPhoto()) {
                    mFabOk.setClickable(false);
                    resultData(photoList);
                } else {
                    toPhotoEdit();
                }
            }
        } else if (id == R.id.iv_clear) {
            mSelectPhotoMap.clear();
            mPhotoListAdapter.notifyDataSetChanged();
            refreshSelectCount();
        } else if (id == R.id.iv_preview) {
            Intent intent = new Intent(this, PhotoPreviewActivity.class);
            intent.putExtra(PhotoPreviewActivity.PHOTO_LIST, new ArrayList<>(mSelectPhotoMap.values()));
            startActivity(intent);
        }
    }

    NoDoubleClickListener noDoubleClickListener = new NoDoubleClickListener() {
        @Override
        public void onNoDoubleItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent != null && parent.getId() == R.id.lv_folder_list) {
                if (position == 1 && isClickVideo) {
                    mLlFolderPanel.setVisibility(View.GONE);
                    mLlFolderPanel.setAnimation(AnimationUtils.loadAnimation(PhotoSelectActivity.this, R.anim.gf_flip_horizontal_out));
                } else if (position == 1 && !isClickVideo) {
                    folderItemClick(position);
                    isClickVideo = true;
                } else {
                    folderItemClick(position);
                    isClickVideo = false;
                }
            } else {
                photoItemClick(view, position);
            }
        }

        @Override
        public void onSingleClick(View view) {

        }
    };

    private void folderItemClick(int position) {
        mLlFolderPanel.setVisibility(View.GONE);
        mCurPhotoList.clear();
        PhotoFolderInfo photoFolderInfo = mAllPhotoFolderList.get(position);
        if (photoFolderInfo.getPhotoList() != null) {
            mCurPhotoList.addAll(photoFolderInfo.getPhotoList());
        }
        mPhotoListAdapter.notifyDataSetChanged();

        if (position == 0) {
            mPhotoTargetFolder = null;
        } else {
            PhotoInfo photoInfo = photoFolderInfo.getCoverPhoto();
            if (photoInfo != null && !StringUtils.isEmpty(photoInfo.getPhotoPath())) {
                mPhotoTargetFolder = new File(photoInfo.getPhotoPath()).getParent();
            } else {
                mPhotoTargetFolder = null;
            }
        }
        iv_photo_click.setText(photoFolderInfo.getFolderName());
        mFolderListAdapter.setSelectFolder(photoFolderInfo);
        mFolderListAdapter.notifyDataSetChanged();

        if (mCurPhotoList.size() == 0) {
            mTvEmptyView.setText(R.string.no_photo);
        }
    }

    /**
     * 检查选中集合列表中是否有图片
     */
    private boolean containImage(HashMap<String, PhotoInfo> map) {
        boolean flag = false;
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            PhotoInfo val = (PhotoInfo) entry.getValue();
            if (val.isPic()) {
                flag = true;
                break;
            } else {
                flag = false;
            }
        }
        return flag;
    }

    private void photoItemClick(View view, int position) {
        //TODO 图片点击
        PhotoInfo info = mCurPhotoList.get(position);
        String ext = FilenameUtils.getExtension(info.getPhotoPath());
        //TODO 判断是否点击的是视频
        if (ext.equalsIgnoreCase("mp4")) {
            if (containImage(mSelectPhotoMap)) {
                Toast.makeText(this, "照片和视频不可以同时选择", Toast.LENGTH_LONG).show();
                return;
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(info);
                Intent intent = new Intent(this, PhotoPreviewActivity.class);
                intent.putExtra(PhotoPreviewActivity.PHOTO_LIST, list);
                startActivity(intent);
                return;
            }
        }

        if (!GalleryFinal.getFunctionConfig().isMutiSelect()) {//单选
            mSelectPhotoMap.clear();
            mSelectPhotoMap.put(info.getPhotoPath(), info);
            if (GalleryFinal.getFunctionConfig().isEditPhoto() && (ext.equalsIgnoreCase("png")
                    || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg"))) {
                toPhotoEdit();
            } else {
                ArrayList<PhotoInfo> list = new ArrayList<>();
                list.add(info);
                resultData(list);
            }
            return;
        }
        boolean checked;
        if (mSelectPhotoMap.get(info.getPhotoPath()) == null) {
            if (GalleryFinal.getFunctionConfig().isMutiSelect() && mSelectPhotoMap.size() == GalleryFinal.getFunctionConfig().getMaxSize()) {
                toast(getString(R.string.select_max_tips));
                return;
            } else {
                mSelectPhotoMap.put(info.getPhotoPath(), info);
                checked = true;
            }
        } else {
            mSelectPhotoMap.remove(info.getPhotoPath());
            checked = false;
        }
        refreshSelectCount();
        AppCompatCheckBox compatCheckBox = (AppCompatCheckBox) view.findViewById(R.id.cb_media);
        if (compatCheckBox != null) {
            if (checked) {
                compatCheckBox.setBackgroundColor(GalleryFinal.getGalleryTheme().getCheckSelectedColor());
            } else {
                compatCheckBox.setBackgroundColor(GalleryFinal.getGalleryTheme().getCheckNornalColor());
            }
        } else {
            mPhotoListAdapter.notifyItemChanged(position);
        }

    }

    @SuppressLint("StringFormatMatches")
    public void refreshSelectCount() {
        int checkSize = mSelectPhotoMap.size();
        mFabOk.setEnabled(checkSize != 0);
        mFabOk.setText(checkSize == 0 ? getString(R.string.btn_send) : String.format(getString(R.string.send_multi), checkSize, GalleryFinal.getFunctionConfig().getMaxSize()));
        mIvPreView.setEnabled(checkSize != 0);
        mIvPreView.setText(checkSize == 0 ? getString(R.string.preview) : getString(R.string.preview_select, checkSize));
        if (mSelectPhotoMap.size() > 0 && GalleryFinal.getFunctionConfig().isMutiSelect()) {
            mIvClear.setVisibility(View.VISIBLE);
            if (GalleryFinal.getFunctionConfig().isEnablePreview()) {
                mIvPreView.setVisibility(View.VISIBLE);
            }
        } else {
            mIvClear.setVisibility(View.GONE);
            mIvPreView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionsGranted(List<String> list) {
        getPhotos(isLoadVideo);
    }

    @Override
    public void onPermissionsDenied(List<String> list) {
        mTvEmptyView.setText(R.string.permissions_denied_tips);
        mIvTakePhoto.setVisibility(View.GONE);
    }

    /**
     * 获取所有图片
     */
    @AfterPermissionGranted(GalleryFinal.PERMISSIONS_CODE_GALLERY)
    private void requestGalleryPermission() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getPhotos(isLoadVideo);
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_tips_gallery),
                    GalleryFinal.PERMISSIONS_CODE_GALLERY, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void getPhotos(final boolean isLoadVideo) {
        mTvEmptyView.setText(R.string.waiting);
        mGvPhotoList.setEnabled(false);
        iv_photo_click.setEnabled(false);
        mIvTakePhoto.setEnabled(false);

        new Thread() {
            @Override
            public void run() {
                super.run();
                mAllPhotoFolderList.clear();
                List<PhotoFolderInfo> allFolderList = PhotoTools.loadAllPhotoFolder(PhotoSelectActivity.this, mSelectPhotoMap, isLoadVideo);
                mAllPhotoFolderList.addAll(allFolderList);

                mCurPhotoList.clear();
                if (allFolderList.size() > 0) {
                    if (allFolderList.get(0).getPhotoList() != null) {
                        mCurPhotoList.addAll(allFolderList.get(0).getPhotoList());
                    }
                }
                refreshAdapter();
            }
        }.start();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mLlFolderPanel.getVisibility() == View.VISIBLE) {
//                mLlTitle.performClick();
                iv_photo_click.performClick();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mHasRefreshGallery) {
            mHasRefreshGallery = false;
            requestGalleryPermission();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (GalleryFinal.getCoreConfig() != null &&
                GalleryFinal.getCoreConfig().getImageLoader() != null) {
            GalleryFinal.getCoreConfig().getImageLoader().clearMemoryCache();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoTargetFolder = null;
        if (mAllPhotoFolderList != null) {
            mAllPhotoFolderList.clear();
            mAllPhotoFolderList = null;
        }
        if (mCurPhotoList != null) {
            mCurPhotoList.clear();
            mCurPhotoList = null;
        }
        if (mSelectPhotoMap != null) {
            mSelectPhotoMap.clear();
            mSelectPhotoMap = null;
        }
        GalleryFinal.getCoreConfig().getImageLoader().clearMemoryCache();
        System.gc();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
}
