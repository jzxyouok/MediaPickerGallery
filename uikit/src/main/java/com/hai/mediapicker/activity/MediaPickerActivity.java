package com.hai.mediapicker.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hai.mediapicker.R;
import com.hai.mediapicker.adapter.GalleryAdapter;
import com.hai.mediapicker.adapter.PopupDirectoryListAdapter;
import com.hai.mediapicker.decoration.SpaceItemDecoration;
import com.hai.mediapicker.entity.Photo;
import com.hai.mediapicker.entity.PhotoDirectory;
import com.hai.mediapicker.util.MediaManager;
import com.hai.mediapicker.util.MediaStoreHelper;
import com.hai.mediapicker.view.PopupWindowMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MediaPickerActivity extends AppCompatActivity implements MediaManager.OnCheckchangeListener {
    RecyclerView imageRecyclerView;
    GalleryAdapter galleryAdapter;
    Button btnSend;
    TextView tvPreview, tvDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);
        initUi();
        readIntentParams();
    }


    /**
     * 修复输入法管理器引起的内存泄漏
     *
     * @param destContext
     */
    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }

        String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field f = null;
        Object obj_get = null;
        for (int i = 0; i < arr.length; i++) {
            String param = arr[i];
            try {
                f = imm.getClass().getDeclaredField(param);
                if (f.isAccessible() == false) {
                    f.setAccessible(true);
                } // author: sodino mail:sodino@qq.com
                obj_get = f.get(imm);
                if (obj_get != null && obj_get instanceof View) {
                    View v_get = (View) obj_get;
                    if (v_get.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        f.set(imm, null); // 置空，破坏掉path to gc节点
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaManager.getInstance().removeOnCheckchangeListener(this);
        EventBus.getDefault().unregister(this);
        MediaManager.getInstance().clear();
        fixInputMethodManagerLeak(this);
    }

    private void initUi() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert toolbar != null;
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvDirectory = (TextView) findViewById(R.id.tv_dictory);
        tvDirectory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });
        btnSend = (Button) findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaManager.getInstance().send();
            }
        });
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        imageRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageRecyclerView.addItemDecoration(new SpaceItemDecoration(this, 1));
        imageRecyclerView.setHasFixedSize(true);
        imageRecyclerView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        tvPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), PreviewActivity.class);
                startActivity(intent);
            }
        });

        MediaManager.getInstance().init();
        MediaStoreHelper.getPhotoDirs(this, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> dirs) {
                dirs.get(0).setSelected(true);
                MediaManager.getInstance().setPhotoDirectorys(dirs);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        directoryChanged();
                    }
                });
            }
        });
    }

    private void readIntentParams() {
        Intent intent = getIntent();
        int maxMedia = intent.getIntExtra("maxSum", 9);
        MediaManager.getInstance().setMaxMediaSum(maxMedia);
    }


    public static int getNavigationBarHeight(Activity activity) {
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    PopupWindowMenu menuWindow;

    private void show() {
        if (menuWindow == null) {
            menuWindow = new PopupWindowMenu(MediaPickerActivity.this, new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    directoryChanged();
                    tvDirectory.setText(MediaManager.getInstance().getPhotoDirectorys().get(position).getName());
                }
            });
            menuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    imageRecyclerView.setAlpha(1.0f);
                }
            });
            PopupDirectoryListAdapter popupDirectoryListAdapter = new PopupDirectoryListAdapter(MediaManager.getInstance().getPhotoDirectorys());
            menuWindow.setAdapter(popupDirectoryListAdapter);
            menuWindow.setHeight(calculatePopupWindowHeight(menuWindow.getAdapter().getCount()));
        }
        ObjectAnimator.ofFloat(imageRecyclerView, "alpha", 1.0f, 0.2f).setDuration(getResources().getInteger(R.integer.anim_duration)).start();
        int barHeight = getResources().getDimensionPixelOffset(R.dimen.toolbar_height);
        int windowHeight = getNavigationBarHeight(this) + barHeight;
        menuWindow.showAtLocation(findViewById(R.id.bottom), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, windowHeight);

    }

    private void directoryChanged() {
        if (galleryAdapter == null) {
            galleryAdapter = new GalleryAdapter(MediaPickerActivity.this, MediaManager.getInstance().getSelectDirectory());
            galleryAdapter.setImageRecyclerView(imageRecyclerView);
            imageRecyclerView.setAdapter(galleryAdapter);
            MediaManager.getInstance().addOnCheckchangeListener(MediaPickerActivity.this);
            galleryAdapter.setOnItemClickListener(new GalleryItemClickImpl());
        } else {
            galleryAdapter.setImages(MediaManager.getInstance().getSelectDirectory());
            imageRecyclerView.getLayoutManager().scrollToPosition(0);
        }
    }

    /**
     * 不使用匿名内部类，避免出现内存泄漏
     */
    private static class GalleryItemClickImpl implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(view.getContext(), PreviewActivity.class);
            intent.putExtra("index", position);
            intent.putExtra("dir", MediaManager.getInstance().getSelectIndex());
            view.getContext().startActivity(intent);
        }
    }

    int calculatePopupWindowHeight(int count) {
        int maxHeight = imageRecyclerView.getMeasuredHeight() - getResources().getDimensionPixelSize(R.dimen.directory_window_margin_top);
        int windowHeight = count * getResources().getDimensionPixelOffset(R.dimen.directory_item_height);
        windowHeight = windowHeight < maxHeight ? windowHeight : maxHeight;
        return windowHeight;
    }


    @Override
    public void onCheckedChanged(Map<Integer, Photo> checkStaus, int changedId, boolean uiUpdated) {
        final int checkSize = checkStaus.size();
        btnSend.setEnabled(checkSize != 0);
        btnSend.setText(checkSize == 0 ? getString(R.string.btn_send) : String.format(getString(R.string.send_multi), checkSize, MediaManager.getInstance().getMaxMediaSum()));

        tvPreview.setEnabled(checkSize != 0);
        tvPreview.setText(checkSize == 0 ? getString(R.string.preview) : getString(R.string.preview_multi, checkSize));
        if (!uiUpdated)
            galleryAdapter.updateCheckbox(changedId);
    }

}
