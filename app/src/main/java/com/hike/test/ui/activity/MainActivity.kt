package com.hike.test.ui.activity

import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hike.base.ui.activity.BaseActivity
import com.hike.base.utils.ConnectivityUtils
import com.hike.base.utils.ToastUtils
import com.hike.test.R
import com.hike.test.constant.IEvent
import com.hike.test.databinding.ActivityMainBinding
import com.hike.test.engine.command.EngineResponse
import com.hike.test.engine.controller.Engine
import com.hike.test.model.Flickr
import com.hike.test.model.FlickrRequest
import com.hike.test.model.Photo
import com.hike.test.ui.adapter.PhotoAdapter

class MainActivity : BaseActivity(), EngineResponse {
    private var mPhotos: ArrayList<Photo?>? = ArrayList()
    private lateinit var mPhotoAdapter: PhotoAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var mFlickr: Flickr
    override fun engineResponse(status: Boolean?, command: Int, c: Any?) {
        updateUi(status ?: false, command, c)
    }

    override fun showProgress(show: Boolean?, message: String?) {

    }

    override fun updateUi(status: Boolean, action: Int, serviceResponse: Any?) {
        removeProgressDialog()
        if (status && serviceResponse is Flickr) {
            mFlickr = serviceResponse
            mPhotos = mFlickr.photos?.photo
            mPhotoAdapter.updateList(mPhotos!!)
        } else {
            ToastUtils.showToast(this, "Something went Wrong!")
            mPhotos = ArrayList()
            mPhotoAdapter.updateList(mPhotos!!)
        }
    }

    override fun onEvent(eventId: Int, eventData: Any?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        setSupportActionBar(binding.toolbar)
        initViews()
        getData(IEvent.FETCH_DATA)
    }

    override fun getData(actionID: Int) {
        if (ConnectivityUtils.isNetworkEnabled(this)) {
            val request = FlickrRequest()
            request.page = 1
            request.text =
                if (TextUtils.isEmpty(binding.searchView.query)) "tesla" else binding.searchView.query.toString()
            showProgressDialog("Fetching")
            Engine.getInstance(this).execute(actionID, request.getRequestJson(), this)
        } else {
            ToastUtils.showToast(this, "Please connect Internet!")
        }
    }

    private fun initViews() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(this)
        binding.cardRecyclerView.layoutManager = layoutManager
        mPhotoAdapter = PhotoAdapter(mPhotos!!)
        binding.cardRecyclerView.adapter = mPhotoAdapter
        search()
    }

    private fun search() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
//                mPhotoAdapter.filter.filter(newText)
                getData(IEvent.FETCH_DATA)
                return false
            }

        })
    }

}
