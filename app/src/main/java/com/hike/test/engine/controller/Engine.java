package com.hike.test.engine.controller;

import android.annotation.SuppressLint;
import android.content.Context;

import com.hike.base.volley.GsonObjectRequest;
import com.hike.base.volley.RequestManager;
import com.hike.test.HikeApplication;
import com.hike.test.constant.IEvent;
import com.hike.test.engine.command.EngineResponse;
import com.hike.test.engine.error.ErrorListner;
import com.hike.test.model.Flickr;

import java.util.HashMap;

/**
 * Created by Saunik Singh on 4/18/2020.
 * Bada Business
 */
public class Engine {
    @SuppressLint("StaticFieldLeak")
    private static Engine mInstance = null;
    private HikeApplication hikeApplication;

    //constructor
    private Engine(Context ctx) {
        hikeApplication = (HikeApplication) ctx.getApplicationContext();
    }

    //get instance in engine
    public static Engine getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new Engine(ctx);
        }
        return mInstance;
    }

    public void execute(int commandId, Object obj, EngineResponse responseScreen) {
        if (commandId == IEvent.FETCH_DATA) {
            fetchFlickrData(commandId, obj, responseScreen);
        }
    }

    private void fetchFlickrData(final int commandId, Object obj, final EngineResponse eResponse) {
        if (obj instanceof String) {
            RequestManager.addRequest(new GsonObjectRequest<Flickr>("https://api.flickr.com/services/rest?method=flickr.photos.search&api_key=3e7cc266ae2b0e0d78e279ce8e361736&format=json&nojsoncallback=1" + obj.toString(),
                    getHeader(), null, Flickr.class, new ErrorListner(eResponse, commandId)) {
                @Override
                protected void deliverResponse(Flickr response) {
                    eResponse.engineResponse(true, commandId, response);
                }
            });
        }
    }

    private HashMap<String, String> getHeader() {
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "application/json");
        return headerMap;
    }
}
