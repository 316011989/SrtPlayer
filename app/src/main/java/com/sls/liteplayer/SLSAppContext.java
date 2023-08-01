package com.sls.liteplayer;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

public class SLSAppContext extends Application {
    private static SLSAppContext mApp;
    private int activityAount = 0;
    private boolean isForeground = false;


    @Override
    public void onCreate() {
        super.onCreate();
        //注册监听器
        registerActivityLifecycleCallbacks(new SwitchBackgroundCallbacks());
    }

    public static SLSAppContext getAppContext() {
        return mApp;
    }

    private class SwitchBackgroundCallbacks implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activityAount == 0) {
                //app回到前台
                isForeground = true;
                String activityName = activity.getClass().getSimpleName();
                if (activityName.compareTo("MainActivity") == 0) {
                    MainActivity mainActivity = (MainActivity)activity;
                    //mainActivity.resetMedec();
                }
            }
            activityAount++;

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityAount--;
            if (activityAount == 0) {
                isForeground = false;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

}