package com.hike.test.model

/**
 * Created by Saunik Singh on 4/18/2020.
 * Bada Business
 */
class FlickrRequest {
    var page = 1
    var text = "tesla"
    var perpage = 10

    fun getRequestJson(): String = "&text=$text&page=$page&perpage=$perpage"
}