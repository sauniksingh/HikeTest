package com.hike.test.engine.command

import com.hike.base.ui.IScreen

/**
 * Created by Saunik Singh on 4/18/2020.
 * Bada Business
 */
interface EngineResponse:IScreen {
    fun engineResponse(status: Boolean?, command: Int, c: Any?)

    fun showProgress(show: Boolean?, message: String?)
}