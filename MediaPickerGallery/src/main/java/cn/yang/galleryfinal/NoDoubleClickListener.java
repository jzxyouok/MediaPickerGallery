package cn.yang.galleryfinal;

import android.view.View;
import android.widget.AdapterView;

import java.util.Calendar;

/**
 * Created by yangc on 2017/4/28.
 * E-Mail:yangchaojiang@outlook.com
 * Description: 防止相册多次点击
 */

public abstract class NoDoubleClickListener implements AdapterView.OnItemClickListener, View.OnClickListener {
    static int MIN_CLICK_DELAY_TIME = 300;
    private long lastClickTime = 0;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            onNoDoubleItemClick(parent, view, position, id);
        }
    }

    @Override
    public void onClick(View v) {
        onSingleClick(v);
    }

    public abstract void onNoDoubleItemClick(AdapterView<?> parent, View view, int position, long id);

    public abstract void onSingleClick(View view);
}