package com.soft.xiren;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private PullToRefreshLayout pullToRefreshLayout;

    private MyTextView tvTest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initView(){
        pullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.pull_to_refresh);
        tvTest = (MyTextView) findViewById(R.id.tv_test);
    }

    private void initEvent(){
        pullToRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 下拉刷新操作
                pullToRefreshLayout.refreshFinish(PullToRefreshLayout.REFRESH_SUCCEED);
            }
        });
    }




}
