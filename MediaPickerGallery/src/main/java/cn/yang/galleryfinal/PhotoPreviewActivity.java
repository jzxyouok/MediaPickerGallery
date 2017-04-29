package cn.yang.galleryfinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.R;
import cn.yang.galleryfinal.adapter.PhotoPreviewAdapter;
import cn.yang.galleryfinal.model.PhotoInfo;
import cn.yang.galleryfinal.widget.GFViewPager;

/**
 * Desction:
 * Author:pengjianbo
 * Date:2015/12/29 0029 14:43
 */
public class PhotoPreviewActivity extends PhotoBaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    static final String PHOTO_LIST = "photo_list";

    private RelativeLayout mTitleBar, footer_bar;
    private ImageView mIvBack, iv_video_img, iv_video_mask;
    private TextView mTvIndicator;

    private GFViewPager mVpPager;
    private ArrayList<PhotoInfo> mPhotoList;
    private PhotoPreviewAdapter mPhotoPreviewAdapter;

    private ThemeConfig mThemeConfig;
    private Button btn_file_size, btn_send;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhotoList!=null){
            mPhotoList.clear();
        }
            mThemeConfig=null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThemeConfig = GalleryFinal.getGalleryTheme();

        if (mThemeConfig == null) {
            resultFailureDelayed(getString(R.string.please_reopen_gf), true);
        } else {
            setContentView(R.layout.gf_activity_photo_preview);
            findViews();
            setListener();
            setTheme();

            mPhotoList = (ArrayList<PhotoInfo>) getIntent().getSerializableExtra(PHOTO_LIST);
            if(mPhotoList==null){
                resultFailureDelayed(getString(R.string.please_reopen_gf), true);
            }else{
                if (mPhotoList.get(0).isPic()) {
                    mPhotoPreviewAdapter = new PhotoPreviewAdapter(this, mPhotoList);
                    mVpPager.setAdapter(mPhotoPreviewAdapter);
                    iv_video_mask.setVisibility(View.GONE);
                    iv_video_img.setVisibility(View.GONE);
                    btn_file_size.setText(getString(R.string.image_preview_size,Formatter.formatFileSize(getBaseContext(),mPhotoList.get(0).getFileSize())));//设置显示的文件大小
                } else {
                    mVpPager.setVisibility(View.GONE);
                    mTvIndicator.setText(getString(R.string.video_preview));
                    iv_video_mask.setVisibility(View.VISIBLE);
                    iv_video_img.setVisibility(View.VISIBLE);
                    GalleryFinal.getCoreConfig().getImageLoader().displayImage(this,mPhotoList.get(0).getPhotoPath(),iv_video_img,R.drawable.img_default);
                    btn_file_size.setText(getString(R.string.video_preview_size ,Formatter.formatFileSize(this,mPhotoList.get(0).getFileSize())));//设置显示的文件大小
                }
            }
        }
    }

    private void findViews() {
        mTitleBar = (RelativeLayout) findViewById(R.id.titlebar);
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mTvIndicator = (TextView) findViewById(R.id.tv_indicator);
        mVpPager = (GFViewPager) findViewById(R.id.vp_pager);
        mVpPager.setPageTransformer(true, new DepthPageTransformer());
        iv_video_img = (ImageView) findViewById(R.id.iv_video_img);
        iv_video_mask = (ImageView) findViewById(R.id.iv_video_mask);
        footer_bar = (RelativeLayout) findViewById(R.id.footer_bar);
        btn_file_size = (Button) findViewById(R.id.btn_file_size);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        iv_video_mask.setOnClickListener(this);
    }

    private void setListener() {
        mVpPager.addOnPageChangeListener(this);
        mIvBack.setOnClickListener(mBackListener);
    }

    private void setTheme() {
        mIvBack.setImageResource(mThemeConfig.getIconBack());
        if (mThemeConfig.getIconBack() == R.drawable.ic_gf_back_selector) {
            mIvBack.setColorFilter(mThemeConfig.getTitleBarIconColor());
        }

        mTitleBar.setBackgroundColor(mThemeConfig.getTitleBarBgColor());
        if (mThemeConfig.getPreviewBg() != null) {
            mVpPager.setBackgroundDrawable(mThemeConfig.getPreviewBg());
        }
    }

    @Override
    protected void takeResult(PhotoInfo info) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTvIndicator.setText((position + 1) + "/" + mPhotoList.size());
    }

    @Override
    public void onPageSelected(int position) {
        btn_file_size.setText(getString(R.string.image_preview_size,Formatter.formatFileSize(getBaseContext(),mPhotoList.get(position).getFileSize())));//设置显示的文件大小
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private View.OnClickListener mBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    public void onClick(View v) {
        //点击视频预览
        if (v.getId() == R.id.iv_video_mask) {
            try {
                Intent  intent = getVideoFileIntent(mPhotoList.get(0).getPhotoPath());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.video_preview_no_toast), Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.btn_send) {//发送
            btn_send.setEnabled(false);
            resultData(mPhotoList);
        }
    }

    // android获取一个用于打开视频文件的intent

    public static Intent getVideoFileIntent(String param)

    {

        Intent intent = new Intent("android.intent.action.VIEW");

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra("oneshot", 0);

        intent.putExtra("configchange", 0);

        Uri uri = Uri.fromFile(new File(param));

        intent.setDataAndType(uri, "video/*");

        return intent;

    }

    @Override
    public void onPermissionsGranted(List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }
    public class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
