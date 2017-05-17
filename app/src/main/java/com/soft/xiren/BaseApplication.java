package com.soft.xiren;

import android.app.Application;

/**
 * description:基本 application类文件
 * Date: 2016/9/8 18:04
 * User: shaobing
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppContext.init(getApplicationContext());
    }

}
