package com.hike.base.ui.activity;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.hike.base.BuildConfig;
import com.hike.base.application.BaseApplication;
import com.hike.base.ui.IScreen;
import com.hike.base.utils.KeypadUtils;

/**
 * Created by Saunik on 23-09-2018.
 */
public abstract class BaseActivity extends AppCompatActivity implements IScreen {
    private String LOG_TAG = getClass().getSimpleName();
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "onResume()");
        }

        Application application = this.getApplication();
        if (application instanceof BaseApplication) {
            BaseApplication baseApplication = (BaseApplication) application;
            if (baseApplication.isAppInBackground()) {
                // clear filter when app is in background
                onAppResumeFromBackground();
            }
            baseApplication.onActivityResumed();
        }
    }

    /**
     * This callback will be called after onResume if application is being
     * resumed from background. <br/>
     * <p/>
     * Subclasses can override this method to get this callback.
     */
    protected void onAppResumeFromBackground() {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "onAppResumeFromBackground()");
        }
    }

    /**
     * This method should be called to force app assume itself not in
     * background.
     */
//    public final void setAppNotInBackground() {
//        Application application = this.getApplication();
//        if (application instanceof BaseApplication) {
//            BaseApplication baseApplication = (BaseApplication) application;
//            baseApplication.setAppInBackground(false);
//        }
//    }

    @Override
    protected void onPause() {
        super.onPause();
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "onPause()");
        }

        Application application = this.getApplication();
        if (application instanceof BaseApplication) {
            BaseApplication baseApplication = (BaseApplication) application;
            baseApplication.onActivityPaused();
        }
//        removeProgressDialog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, "onNewIntent()");
        }
    }

    // ////////////////////////////// show and hide ProgressDialog

    /**
     * Subclass should over-ride this method to update the UI with response,
     * this base class promises to call this method from UI thread.
     *
     * @param serviceResponse {@link Object}
     */
    public abstract void updateUi(final boolean status, final int action, final Object serviceResponse);

    /**
     * Shows a simple native progress dialog<br/>
     * Subclass can override below two methods for custom dialogs- <br/>
     * 1. showProgressDialog <br/>
     * 2. removeProgressDialog
     *
     * @param bodyText {@link String}
     */
    public void showProgressDialog(final String bodyText) {
        if (isFinishing()) {
            return;
        }
        if (mProgressDialog == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = new ProgressDialog(BaseActivity.this);
                    mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            return keyCode == KeyEvent.KEYCODE_CAMERA || keyCode == KeyEvent.KEYCODE_SEARCH;
                        }
                    });
                    mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                        }
                    });
                    mProgressDialog.setMessage(bodyText);
                    try {
                        if (!mProgressDialog.isShowing()) {
                            //leaked window here issue
                            mProgressDialog.show();

                        }
                    } catch (Exception e) {
                        if (!TextUtils.isEmpty(e.getMessage()))
                            e.printStackTrace();
                    }
                }
            });
        }
    }
    /**
     * Removes the simple native progress dialog shown via showProgressDialog <br/>
     * Subclass can override below two methods for custom dialogs- <br/>
     * 1. showProgressDialog <br/>
     * 2. removeProgressDialog
     */
    public void removeProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            // Was getting an IllegalArgumentException. So needed to handle it through this or catching exception
            try {
                //get the Context object that was used to great the dialog
                Context context = ((ContextWrapper) mProgressDialog.getContext()).getBaseContext();
                //if the Context used here was an activity AND it hasn't been finished or destroyed
                //then dismiss it
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing() && !((Activity) context).isDestroyed())
                        mProgressDialog.dismiss();
                } else //if the Context used wasnt an Activity, then dismiss it too
                    mProgressDialog.dismiss();

                mProgressDialog = null;
            }catch (final IllegalArgumentException ex){
                Log.e(LOG_TAG, "Error while removing the progress dialog");
            }
//            return true;
        }
//        return false;
    }

    // ////////////////////////////// show and hide key-board
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            View v = getCurrentFocus();
            boolean ret = super.dispatchTouchEvent(event);

            if (v instanceof EditText) {
                View w = getCurrentFocus();
                int scrcoords[] = new int[2];
                if (w != null) {
                    w.getLocationOnScreen(scrcoords);
                    float x = event.getRawX() + w.getLeft() - scrcoords[0];
                    float y = event.getRawY() + w.getTop() - scrcoords[1];

                    if (event.getAction() == MotionEvent.ACTION_UP && (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
                        KeypadUtils.hideSoftKeypad(this);
                    }
                }
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
