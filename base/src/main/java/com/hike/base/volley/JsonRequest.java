/*
 *
 *  Proprietary and confidential. Property of Kellton Tech Solutions Ltd. Do not disclose or distribute.
 *  You must have written permission from Kellton Tech Solutions Ltd. to use this code.
 *
 */

package com.hike.base.volley;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyLog;
import com.hike.base.BuildConfig;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * A request for retrieving a {@link JSONObject} response body at a given URL, allowing for an
 * optional {@link JSONObject} to be passed in as part of the request body.
 */
public abstract class JsonRequest<T> extends Request<T> {

    /**
     * Charset for request.
     */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /**
     * Content type for request.
     */
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);
    private String mRequestBody;
    private byte[] mMultipartBody;
    private boolean isMultipartRequest;
    NetworkResponse mResponse;

    private Priority mPriority;
    /**
     * Request headers.
     */
    private Map<String, String> mRequestHeaders;

    JsonRequest(String url, Map<String, String> mRequestHeaders, String jsonPayload, ErrorListener errorListener) {
        this(jsonPayload == null ? Method.GET : Method.POST, url, mRequestHeaders, jsonPayload, errorListener);

    }

    JsonRequest(int method, String url, Map<String, String> mRequestHeaders, String jsonPayload, ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mRequestBody = jsonPayload;
        this.mRequestHeaders = mRequestHeaders;
        this.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        if (BuildConfig.BUILD_TYPE.contains("debug")) {
            Log.d("REQUEST", "URL: " + url);
            Log.d("REQUEST", "PARAM: " + jsonPayload);
        }
    }

    JsonRequest(int method, boolean isMultipartRequest, String url, Map<String, String> headers, byte[] multipartBody, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.isMultipartRequest = isMultipartRequest;
        this.mMultipartBody = multipartBody;
        this.mRequestHeaders = headers;
    }

    public void setPriority(Priority mPriority) {
        this.mPriority = mPriority;
    }

    @Override
    public Priority getPriority() {
        return this.mPriority;
    }


    @Override
    public String getBodyContentType() {
        if (isMultipartRequest) {
            return FileUploadRequest.mMimeType;
        }
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        if (isMultipartRequest) {
            return mMultipartBody;
        }
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    public Map<String, String> getHeaders() {
        return mRequestHeaders;
    }


}
