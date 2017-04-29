package cn.yang.galleryfinal.adapter.viewholder;

import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import cn.finalteam.galleryfinal.R;
import cn.yang.galleryfinal.widget.GFImageView;

/**
 * Created by yangc on 2017/4/28.
 * E-Mail:yangchaojiang@outlook.com
 * Deprecated: item
 */
public class GalleryHolder extends RecyclerView.ViewHolder {
    public GFImageView thumbIv;
    public AppCompatCheckBox appCompatCheckBox;
    public TextView tvVideoDuration;
    public ImageView ivVideoFlag;

    public GalleryHolder(View itemView) {
        super(itemView);
        itemView.setClickable(true);
        thumbIv = (GFImageView) itemView.findViewById(R.id.iv_thumb);
        thumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        appCompatCheckBox = (AppCompatCheckBox) itemView.findViewById(R.id.cb_media);
        tvVideoDuration = (TextView) itemView.findViewById(R.id.tv_video_duration);
        ivVideoFlag = (ImageView) itemView.findViewById(R.id.iv_video_flag);

    }
}