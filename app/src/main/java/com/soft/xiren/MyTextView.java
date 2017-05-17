package com.soft.xiren;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * 描述：
 * 作者：邵兵
 * 邮箱：2017/5/17 12:45
 */
public class MyTextView extends TextView {
    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d("touch","               "+2+"    onTouchEvent  ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("touch","               "+2+"    onTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("touch","               "+2+"   onTouchEvent ACTION_UP");
                break;
        }
        return true;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.d("touch","               "+2+"    dispatchTouchEvent  ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("touch","               "+2+"     dispatchTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("touch","               "+2+"    dispatchTouchEvent ACTION_UP");
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
