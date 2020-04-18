/*
 *
 *  Proprietary and confidential. Property of Kellton Tech Solutions Ltd. Do not disclose or distribute.
 *  You must have written permission from Kellton Tech Solutions Ltd. to use this code.
 *
 */

package com.hike.base.volley;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.Volley;
import com.hike.base.R;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Saunik
 */
public class RequestManager {
    /**
     * 1: Cache Images should be cleaned once application is uninstalled.
     * 2: Do, I have to start the queue on queue() and loader() functions
     * 3: DiskCache in RequestQueue Vs LRUCache in ImageLoader
     * 4: Request Priority order
     * 5: can we use only one requestQueue.
     */
    private static RequestManager instance;
    private static ImageLoader mImageLoader;
    private RequestQueue mDataRequestQueue;
    private RequestQueue mImageQueue;

    private static Context mContext;
    private Config mConfig;

    //	private static String mDefaultRequestTag;
    public static class Config {
        private String mImageCachePath;
        private int mDefaultDiskUsageBytes;
        private int mThreadPoolSize;

        public Config(final String imageCachePath, final int defaultDiskUsageBytes, final int threadPoolSize) {
            this.mDefaultDiskUsageBytes = defaultDiskUsageBytes;
            this.mImageCachePath = imageCachePath;
            this.mThreadPoolSize = threadPoolSize;
        }
    }


    private RequestManager(Context context, Config config) {
        mContext = context;
        this.mConfig = config;
    }

    public static synchronized void initializeWith(Context context, Config config) {
        if (instance == null) {
            instance = new RequestManager(context, config);
            VolleyLog.DEBUG = false;
        }
    }

    // set timeout in okhttpclient.
    public OkHttpClient getOkHttpClient() {
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(10, TimeUnit.MINUTES);
        okHttpClient.setConnectTimeout(10, TimeUnit.MINUTES);
        okHttpClient.setWriteTimeout(10, TimeUnit.MINUTES);
        return okHttpClient;
    }

    private synchronized RequestQueue getDataRequestQueue() {
        if (mDataRequestQueue == null) {
            mDataRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext(), new OkHttpStack(getOkHttpClient()));
            mDataRequestQueue.start();
        }
        return mDataRequestQueue;
    }

    private synchronized RequestQueue loader() {
        if (this.mConfig == null) {
            throw new IllegalStateException(RequestManager.Config.class.getSimpleName() +
                    " is not initialized, call initializeWith(..) method first.");
        }
        if (mImageQueue == null) {
            File rootCache = mContext.getExternalCacheDir();
            if (rootCache == null) {
                rootCache = mContext.getCacheDir();
            }

            File cacheDir = new File(rootCache, mConfig.mImageCachePath);
            cacheDir.mkdirs();

            HttpStack stack = new HurlStack();
            Network network = new BasicNetwork(stack);
            DiskBasedCache diskBasedCache = new DiskBasedCache(cacheDir, mConfig.mDefaultDiskUsageBytes);
            mImageQueue = new RequestQueue(diskBasedCache, network, mConfig.mThreadPoolSize);
            mImageQueue.start();
        }
        return mImageQueue;
    }

    public static <T> void addRequest(Request<T> pRequest) {
        if (instance == null) {
            throw new IllegalStateException(RequestManager.class.getSimpleName() +
                    " is not initialized, call initializeWith(..) method first.");
        }
        if (pRequest.getTag() == null) {
            pRequest.setTag(mContext.getString(R.string.app_name));
            new IllegalArgumentException("Request Object Tag is not specified.");
        }
        pRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 30, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = instance.getDataRequestQueue();
        Log.e("REQUEST_URL", "" + pRequest.getUrl());
        try {
            queue.add(pRequest);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param url
     */
    public static <T> void getImage(String url, ImageListener listener) {
        if (instance == null) {
            throw new IllegalStateException(RequestManager.class.getSimpleName() +
                    " is not initialized, call initializeWith(..) method first.");
        }
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(instance.loader(), new DiskCache(mContext));
        }
        mImageLoader.get(url, listener);
    }


    /**
     * Cancels all pending requests by the specified TAG, it is important to
     * specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param pRequestTag
     */
    public static void cancelPendingRequests(Object pRequestTag) {
        if (instance == null) {
            throw new IllegalStateException(RequestManager.class.getSimpleName() +
                    " is not initialized, call initializeWith(..) method first.");
        }
        if (instance.getDataRequestQueue() != null) {
            instance.getDataRequestQueue().cancelAll(pRequestTag);
        }
    }


    /**
     * Implementation of volley's {@link ImageCache} interface.
     *
     * @author sachin.gupta
     */
    private static class DiskCache implements ImageCache {

        private static DiskLruImageCache mDiskLruImageCache;

        public DiskCache(Context context) {
            String cacheName = context.getPackageCodePath();
            int cacheSize = 1024 * 1024 * 10;
            mDiskLruImageCache = new DiskLruImageCache(context, cacheName, cacheSize, CompressFormat.PNG, 100);
        }

        @Override
        public Bitmap getBitmap(String pImageUrl) {
            try {
                return mDiskLruImageCache.getBitmap(createKey(pImageUrl));
            } catch (NullPointerException e) {
                throw new IllegalStateException("Disk Cache Not initialized");
            }
        }

        @Override
        public void putBitmap(String pImageUrl, Bitmap pBitmap) {
            try {
                mDiskLruImageCache.put(createKey(pImageUrl), pBitmap);
            } catch (NullPointerException e) {
                throw new IllegalStateException("Disk Cache Not initialized");
            }
        }

        /**
         * Creates a unique cache key based on a url value
         *
         * @param pImageUrl url to be used in key creation
         * @return cache key value
         */
        private String createKey(String pImageUrl) {
            return String.valueOf(pImageUrl.hashCode());
        }
    }

}
