package cn.yang.galleryfinal.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;

import cn.yang.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.R;
import cn.yang.galleryfinal.adapter.viewholder.GalleryHolder;
import cn.yang.galleryfinal.model.PhotoInfo;

/**
 * Created by yangc on 2017/4/28.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated:   列表是适配器
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder> {
    private List<PhotoInfo> images;
    private LayoutInflater layoutInflater;
    private AdapterView.OnItemClickListener onItemClickListener;
    private LinkedHashMap<String, PhotoInfo> mSelectList;
    private Activity mActivity;

    public GalleryAdapter(Activity activity, List<PhotoInfo> images, LinkedHashMap<String, PhotoInfo> mSelectList) {
        this.images = images;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mActivity = activity;
        this.mSelectList = mSelectList;
    }


    public void setImages(List<PhotoInfo> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public List<PhotoInfo> getImages() {
        return images;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return getImages().get(position).isPic() ? 1 : 2;
    }

    @Override
    public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutRes = viewType == 2 ? R.layout.gf_video_item : R.layout.gf_image_item;
        View view = layoutInflater.inflate(layoutRes, parent, false);
        return new GalleryHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryHolder holder, final int position) {
        PhotoInfo photoInfo = getImages().get(position);
        //判断是视频还是图片
        Log.d("onBindViewHolder", photoInfo.getPhotoPath());
        if (photoInfo.isPic()) {
            GalleryFinal.getCoreConfig().getImageLoader().displayImage(mActivity, photoInfo.getPhotoPath(), holder.thumbIv, R.drawable.img_default, 100, 100);
        } else {
            GalleryFinal.getCoreConfig().getImageLoader().displayImage(mActivity, "file:///" + photoInfo.getPhotoPath(), holder.thumbIv, R.drawable.ic_gf_group_dhk_video, 200, 200);
            holder.tvVideoDuration.setText(converDuration(photoInfo.getDuration()));
        }
        if (mSelectList.get(photoInfo.getPhotoPath()) != null && mSelectList.get(photoInfo.getPhotoPath()).getPhotoPath().equals(photoInfo.getPhotoPath())) {
            holder.appCompatCheckBox.setChecked(true);
        } else {
            holder.appCompatCheckBox.setChecked(false);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(null, v, position, 0);
            }
        });
        holder.itemView.setAnimation(null);
        if (GalleryFinal.getCoreConfig().getAnimation() > 0) {
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(mActivity, GalleryFinal.getCoreConfig().getAnimation()));
        }
        holder.appCompatCheckBox.setButtonDrawable(GalleryFinal.getGalleryTheme().getIconCheck());
        if (GalleryFinal.getFunctionConfig().isMutiSelect()) {
            if (photoInfo.isPic()) {
                holder.appCompatCheckBox.setVisibility(View.VISIBLE);
            }
            if (mSelectList.get(photoInfo.getPhotoPath()) != null) {
                holder.appCompatCheckBox.setBackgroundColor(GalleryFinal.getGalleryTheme().getCheckSelectedColor());
            } else {
                holder.appCompatCheckBox.setBackgroundColor(GalleryFinal.getGalleryTheme().getCheckNornalColor());
            }
        } else {
            holder.appCompatCheckBox.setVisibility(View.GONE);
        }
    }


    @Override
    public void onViewDetachedFromWindow(GalleryHolder holder) {
        holder.appCompatCheckBox.setOnCheckedChangeListener(null);
        super.onViewDetachedFromWindow(holder);
    }

    /***
     * 大卫秒
     * */
    String converDuration(long duration) {
        StringBuilder durationString = new StringBuilder();

        long min = duration / 60;
        long hour = min / 60;
        if (hour > 0)
            durationString.append(hour + ":");
        durationString.append(min + ":");
        durationString.append(new DecimalFormat("00").format(duration));
        return durationString.toString();
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}
