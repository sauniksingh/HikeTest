package com.hike.test.engine.error

import android.text.TextUtils
import android.util.Log
import com.android.volley.*
import com.google.gson.GsonBuilder
import com.hike.test.constant.IConstant
import com.hike.test.engine.command.EngineResponse

/**
 * Created by Saunik Singh on 4/18/2020.
 * Bada Business
 */
class ErrorListner(val screenEngine: EngineResponse, val action: Int) : Response.ErrorListener {
    override fun onErrorResponse(error: VolleyError?) {
        getError(error)
    }
    private fun getError(error: VolleyError?) {
        var bytes: ByteArray? = null
        if (error?.networkResponse != null) bytes = error.networkResponse.data
        if (bytes != null && bytes.isNotEmpty()) {
            // Convert Network response in to String
            val response = String(bytes)
            Log.e("Error", response);
            // for dev purpose
            /*   if (!TextUtils.isEmpty(response)) {
                screen.updateUi(false, action, response);
            }*/
//            val errorResponse: ErrorResponse = getErrorResponse(response)
            // Convert Network Response in to model7042829242
//            if (errorResponse != null) {
//                if (!TextUtils.isEmpty(errorResponse.getDetail())) {
//                    screenEngine.engineResponse(false, action, errorResponse.getDetail())
//                } else if (!TextUtils.isEmpty(errorResponse.getMessage())) {
//                    screenEngine.engineResponse(false, action, errorResponse.getMessage())
//                }
//                return
//            }
        }
        staticErrors(error)
    }

    private fun staticErrors(error: VolleyError?) {
        if (error is NoConnectionError) {
            // Update No Connection
            screenEngine.engineResponse(false, action, IConstant.NoConnectionError)
            return
        } else if (error is AuthFailureError) {
            // Update Authorization Failure
            screenEngine.engineResponse(
                false,
                error.networkResponse.statusCode,
                IConstant.AuthFailureError
            )
            return
        } else if (error is NetworkError) {
            // Network Error
            screenEngine.engineResponse(false, action, IConstant.NetworkError)
            return
        } else if (error is ParseError) {
            // Parse Error
            screenEngine.engineResponse(false, action, IConstant.ParseError)
            return
        } else if (error is ServerError) {
            screenEngine.engineResponse(false, action, IConstant.ServerError)
            return
        } else if (error is TimeoutError) {
            // Timeout error
            screenEngine.engineResponse(false, action, IConstant.TimeoutError)
            return
        }
    }
}