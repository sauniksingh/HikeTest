package com.hike.base.volley;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Saunik on 04-02-2016.
 */
public class OkHttpStack extends HurlStack {
    private final OkUrlFactory mFactory;

    public OkHttpStack(OkHttpClient client) {
        if (client == null) {
            throw new NullPointerException("Client must not be null.");
        }
        mFactory = new OkUrlFactory(client);

    }

    @Override
    public HttpURLConnection createConnection(URL url) {
        return mFactory.open(url);
    }
}
