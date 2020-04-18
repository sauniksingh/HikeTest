package com.hike.test

import com.hike.base.application.BaseApplication
import com.hike.base.volley.RequestManager
import java.util.*

/**
 * Created by Saunik Singh on 4/18/2020.
 * Bada Business
 */
class HikeApplication : BaseApplication() {
    override fun initialize() {
        RequestManager.initializeWith(
            this.applicationContext,
            RequestManager.Config("data/data/hike/pics", 336418202, 334)
        )
    }

    fun getHeader(): HashMap<String, String>? {
        val headerMap =
            HashMap<String, String>()
        headerMap["Accept"] = "application/json"
        return headerMap
    }
}