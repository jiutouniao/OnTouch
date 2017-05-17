package com.soft.xiren;

import android.content.Context;
import android.util.Log;

/**
 * Created by Nirui on 17/2/21.
 */

public class AppContext {

    public static Context mAppContext;

    public static void init(Context context) {
        Log.d("AppContext","AppContext --->init");
        if (mAppContext == null) {
            mAppContext = context.getApplicationContext();
        } else {
            throw new IllegalStateException("set context duplicate");
        }
    }

    public static Context getmAppContext() {
        if (mAppContext == null) {
            throw new IllegalStateException("forget init?");
        } else {
            return mAppContext;
        }
    }
}
