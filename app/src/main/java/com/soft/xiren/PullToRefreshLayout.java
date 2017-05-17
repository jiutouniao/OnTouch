package com.soft.xiren;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 整个下拉刷新就这一个布局，用来管理两个子控件，其中一个是下拉头，另一个是包含内容的contentView（可以是AbsListView的任何子类）
 *
 * @author 陈靖
 */
public class PullToRefreshLayout extends RelativeLayout
{
    public static final String TAG = "PullToRefreshLayout";

    /**
     * 默认的状态
     */
   public static final int DEFAULTE= 0;
    // 下拉刷新
    public static final int PULL_TO_REFRESH = 1;
    // 释放刷新
    public static final int RELEASE_TO_REFRESH = 2;
    // 正在刷新
    public static final int REFRESHING = 3;
    // 刷新完毕
    public static final int DONE = 4;
    // 当前状态
    private int state = DEFAULTE;
    // 刷新回调接口
    private OnRefreshListener mListener;
    // 刷新成功
    public static final int REFRESH_SUCCEED = 0;
    // 刷新失败
    public static final int REFRESH_FAIL = 1;
    // 下拉头
    private View headView;
    // 内容
    private View contentView;
    // 按下Y坐标，上一个事件点Y坐标
    private float downY, lastY;
    // 下拉的距离
    public float moveDeltaY = 0;
    // 释放刷新的距离
    private float refreshDist = 0;
    private Timer timer;
    private MyTimerTask mTask;
    // 回滚速度
    public float MOVE_SPEED = 8;
    // 第一次执行布局
    private boolean isLayout = false;

    // 手指滑动距离与下拉头的滑动距离比，中间会随正切函数变化
    private float radio = 2;
    // 下拉箭头的转180°动画
    private RotateAnimation rotateAnimation;
    // 下拉的箭头
    private View pullView;
    // 刷新结果图标
    private ImageView stateImageView;
    // 刷新结果：成功或失败
    private TextView stateTextView;
    /**
     * 执行自动回滚的handler
     */
    Handler updateHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            // 回弹速度随下拉距离moveDeltaY增大而增大
            MOVE_SPEED = (float) (8 + 5 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));
            if (state == REFRESHING && moveDeltaY <= refreshDist )
            {
                // 正在刷新，且没有往上推的话则悬停，显示"正在刷新..."
                moveDeltaY = refreshDist;
                mTask.cancel();
            }
            moveDeltaY -= MOVE_SPEED;

            if (moveDeltaY <= 0)
            {
                // 已完成回弹
                moveDeltaY = 0;
                pullView.clearAnimation();
                // 隐藏下拉头时有可能还在刷新，只有当前状态不是正在刷新时才改变状态
                if (state != REFRESHING)
                    changeState(PULL_TO_REFRESH);
                mTask.cancel();
            }
            // 刷新布局,会自动调用onLayout
            requestLayout();
        }

    };

    public void setOnRefreshListener(OnRefreshListener listener)
    {
        mListener = listener;
    }

    public PullToRefreshLayout(Context context)
    {
        super(context);
        initView(context);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView(context);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context)
    {
        timer = new Timer();
        mTask = new MyTimerTask(updateHandler);
        // 添加匀速转动动画
        rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(context, R.anim.reverse_anim);
        LinearInterpolator lir = new LinearInterpolator();
        rotateAnimation.setInterpolator(lir);
        refreshDist = DisplayMetricsUtil.dip2px(AppContext.getmAppContext(),30);
    }

    private void hideHead()
    {
        if (mTask != null)
        {
            mTask.cancel();
            mTask = null;
        }
        mTask = new MyTimerTask(updateHandler);
        timer.schedule(mTask, 0, 5);
    }

    /**
     * 完成刷新操作，显示刷新结果
     */
    public void refreshFinish(int refreshResult) {
        switch (refreshResult) {
            case REFRESH_SUCCEED:
                // 刷新成功
                stateImageView.setVisibility(View.VISIBLE);
                stateTextView.setText(R.string.refresh_succeed);
                stateImageView.setImageResource(R.drawable.ebook_bt_remark_red);
                break;
            case REFRESH_FAIL:
                // 刷新失败
                stateImageView.setVisibility(View.VISIBLE);
                stateTextView.setText(R.string.refresh_fail);
                stateImageView.setImageResource(R.drawable.ebook_bt_remark_red);
                break;
            default:
                break;
        }
    }

    private void changeState(int to)
    {
        state = to;
        switch (state)
        {
            case PULL_TO_REFRESH:
                // 下拉刷新
                stateTextView.setText(R.string.pull_to_refresh);
                pullView.clearAnimation();
                pullView.setVisibility(View.VISIBLE);
                break;
            case RELEASE_TO_REFRESH:
                // 释放刷新
                stateTextView.setText(R.string.release_to_refresh);
                pullView.startAnimation(rotateAnimation);
                break;
            case REFRESHING:
                // 正在刷新
                pullView.clearAnimation();
                pullView.setVisibility(View.INVISIBLE);
                stateTextView.setText(R.string.refreshing);
                break;
            default:
                break;
        }
    }

    /**
     * 判断是否为上下滑动
     * 当前目录只是消费   上下滑动   左右滑动   点击事件      长按事件
     *
     * 0 代表默认  1 代表自己消费(上下滑动)  2.代表下一级消费  3代表分配完毕
     */
    private int  flag;

    private float startx;

    private float starty;

    private  void isPull( float startx,float starty,MotionEvent ev){
        if(flag ==0){
            if(Math.abs(startx-ev.getX())+Math.abs(starty-ev.getY())>2){
                //动的幅度比较大的时候才执行判断左右滑动
                if(Math.abs(startx-ev.getX())<Math.abs(starty-ev.getY())  ){
                    flag =1;
                }
                else{
                    flag =2;
                }
            }else{
                flag =2;
            }
            this.onInterceptTouchEvent(ev);
        }
        this.startx = ev.getX();
        this.starty = ev.getY();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                Log.d("touch",getClass().getSimpleName()+"  dispatchTouchEvent  ACTION_DOWN");

                downY = ev.getY();
                lastY = downY;
                if (mTask != null) {
                    mTask.cancel();
                }
                startx = ev.getX();
                starty = ev.getY();

                flag=0;
                return super.dispatchTouchEvent(ev);
            case MotionEvent.ACTION_MOVE:
                //消费类型判断
                isPull(  startx, starty, ev);
                Log.d("touch",getClass().getSimpleName()+"  dispatchTouchEvent  ACTION_MOVE  "+flag);
               if(flag==2){ //子控件消费
                   moveDeltaY = 0;
               }else{//下拉消费
                   handleMove(ev);
                   return true;
               }
            case MotionEvent.ACTION_UP:
                Log.d("touch",getClass().getSimpleName()+"  dispatchTouchEvent  ACTION_UP");
                moveDeltaY = 0;
                if (moveDeltaY > refreshDist)
                if (state == RELEASE_TO_REFRESH)
                {
                    changeState(REFRESHING);
                    // 刷新操作
                    if (mListener != null)
                        mListener.onRefresh();
                } else
                {

                }
                hideHead();
            default:
                break;
        }
        // 事件分发交给父类
        Log.d("touch",super.dispatchTouchEvent(ev)+"");
        return super.dispatchTouchEvent(ev);
    }

    private   void handleMove(MotionEvent ev){
        //控制下拉距离
        moveDeltaY = moveDeltaY + (ev.getY() - lastY) / radio;
        if (moveDeltaY < 0) moveDeltaY = 0;
        //保证下拉距离总是为55
        if (moveDeltaY >DisplayMetricsUtil.dip2px(AppContext.getmAppContext(),55)) moveDeltaY = DisplayMetricsUtil.dip2px(AppContext.getmAppContext(),55);

        lastY = ev.getY();
        // 根据下拉距离改变比例
        radio = (float) (2 + 2 * Math.tan(Math.PI / 2 / getMeasuredHeight() * moveDeltaY));
        requestLayout();
        if (moveDeltaY <= refreshDist && state == DEFAULTE) {
            // 保持箭头向下
            changeState(PULL_TO_REFRESH);
        }
        if (moveDeltaY >= refreshDist && state == PULL_TO_REFRESH) {
            changeState(RELEASE_TO_REFRESH);
        }
        if (moveDeltaY >0) {
            // 防止下拉过程中误触发长按事件和点击事件
            clearContentViewEvents();
        }
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
       switch (ev.getAction()){
           case MotionEvent.ACTION_DOWN:
               Log.d("touch",getClass().getSimpleName()+"   onInterceptTouchEvent  ACTION_DOWN");
               break;
           case MotionEvent.ACTION_MOVE:
               Log.d("touch",getClass().getSimpleName()+" onInterceptTouchEvent ACTION_MOVE");
               break;
           case MotionEvent.ACTION_UP:
               Log.d("touch",getClass().getSimpleName()+"   onInterceptTouchEvent ACTION_UP");
               break;
       }
       Log.d("touch","flag  "+flag);
        if(flag==1){
            //拦截，自己消费
            return true;
        }
       else if(flag==2){
            //不拦截，子控件消费
            return false;
        }else{
            //默认情况不拦截
            return super.onInterceptTouchEvent(ev);
        }
    }





    /**
     * 通过反射修改字段去掉长按事件和点击事件
     */
    private void clearContentViewEvents()
    {
        try
        {
            Field[] fields = AbsListView.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++)
                if (fields[i].getName().equals("mPendingCheckForLongPress"))
                {
                    // mPendingCheckForLongPress是AbsListView中的字段，通过反射获取并从消息列表删除，去掉长按事件
                    fields[i].setAccessible(true);
                    contentView.getHandler().removeCallbacks((Runnable) fields[i].get(contentView));
                } else if (fields[i].getName().equals("mTouchMode"))
                {
                    // TOUCH_MODE_REST = -1， 这个可以去除点击事件
                    fields[i].setAccessible(true);
                    fields[i].set(contentView, -1);
                }
            // 去掉焦点
            ((AbsListView) contentView).getSelector().setState(new int[]
                    { 0 });
        } catch (Exception e)
        {
            Log.d(TAG, "error : " + e.toString());
        }
    }

    /*
     * （非 Javadoc）绘制阴影效果，颜色值可以修改
     *
     * @see android.view.ViewGroup#dispatchDraw(android.graphics.Canvas)
     */
    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        super.dispatchDraw(canvas);
        if (moveDeltaY == 0)
            return;
        RectF rectF = new RectF(0, 0, getMeasuredWidth(), moveDeltaY);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        /**
         * shaobing去掉阴影高度
         */
        // 阴影的高度为26
        LinearGradient linearGradient = new LinearGradient(0, moveDeltaY, 0, moveDeltaY - 26, 0x66000000, 0x66000000, TileMode.CLAMP);
        paint.setShader(linearGradient);
        paint.setStyle(Style.FILL);
        // 在moveDeltaY处往上变淡
        canvas.drawRect(rectF, paint);
    }

    private void initView()
    {
        pullView = headView.findViewById(R.id.pull_icon);
        stateTextView = (TextView) headView.findViewById(R.id.state_tv);
        stateImageView = (ImageView) headView.findViewById(R.id.state_iv);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        if (!isLayout)
        {
            // 这里是第一次进来的时候做一些初始化
            headView = getChildAt(0);
            contentView = getChildAt(1);
            isLayout = true;
            initView();
            refreshDist = ((ViewGroup) headView).getChildAt(0).getMeasuredHeight();
        }
        // 改变子控件的布局
        headView.layout(0, (int) moveDeltaY - headView.getMeasuredHeight(), headView.getMeasuredWidth(), (int) moveDeltaY);
        contentView.layout(0, (int) moveDeltaY, contentView.getMeasuredWidth(), (int) moveDeltaY + contentView.getMeasuredHeight());
    }

    class MyTimerTask extends TimerTask {
        Handler handler;

        public MyTimerTask(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage());
        }

    }
}