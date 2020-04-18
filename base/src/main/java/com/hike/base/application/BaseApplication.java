package com.hike.base.application;

/**
 * Created by Saunik Singh on 21-06-2016.
 */

import android.app.Application;
import android.util.Log;

import com.hike.base.BuildConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This class holds some application-global instances.
 */
public abstract class BaseApplication extends Application{
    private static final String LOG_TAG = "BaseApplication";
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean mAppInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    @Override
    public void onCreate() {
        super.onCreate();
        this.initialize();
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "onCreate()");
        }
    }

    /**
     * @return the mIsAppInBackground
     */
    public boolean isAppInBackground() {
        return mAppInBackground;
    }

    /**
     * @param isAppInBackground value to set for mIsAppInBackground
     */
    public void setAppInBackground(boolean isAppInBackground) {
        mAppInBackground = isAppInBackground;
        onAppStateSwitched(isAppInBackground);
        //  Log.d("setAppInBackground", "isAppInBackground " + isAppInBackground);
    }

    /**
     * @param isAppInBackground
     * @note subclass can override this method for this callback
     */
    protected void onAppStateSwitched(boolean isAppInBackground) {
        // nothing to do here in base application class
    }

    /**
     * should be called from onResume of each activity of application
     */
    public void onActivityResumed() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }
        setAppInBackground(false);
    }

    /**
     * should be called from onPause of each activity of app
     */
    public void onActivityPaused() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                setAppInBackground(true);
                if (BuildConfig.DEBUG) {
                    Log.i(LOG_TAG, "None of our activity is in foreground.");
                }
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }


    protected abstract void initialize();
}
