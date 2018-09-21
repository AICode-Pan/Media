package com.explain.media;

import android.app.Application;
import android.content.Context;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/9/21
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class CoreApplication extends Application {
    private static Context context;

    public static Context getContent() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
