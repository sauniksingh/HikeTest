package com.hike.test.ui.adapter

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.hike.test.R
import com.hike.test.model.Photo
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Saunik Singh on 4/18/2020.
 * Bada Business
 */
class PhotoAdapter(var photos: ArrayList<Photo?>) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>(),
    Filterable {
    private var filterList = photos

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOwner: TextView = view.findViewById(R.id.tv_owner)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val image: ImageView = view.findViewById(R.id.image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_row, parent, false))

    override fun getItemCount(): Int = filterList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val photo = filterList[position]
        loadImage(holder.image, getImageUrl(photo))
        holder.tvOwner.text = photo?.owner
        holder.tvTitle.text = photo?.title
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val charString = charSequence.toString()
            if (charString.isEmpty()) {
                filterList = photos
            } else {
                val filteredList: ArrayList<Photo?> = ArrayList()
                for (photo in photos) {
                    if (photo?.title?.toLowerCase(Locale.getDefault())
                            ?.contains(charString)!! || photo.owner?.toLowerCase(Locale.getDefault())
                            ?.contains(charString)!!
                    ) {
                        filteredList.add(photo)
                    }
                }
                filterList = filteredList
            }
            val filterResults = FilterResults()
            filterResults.values = filterList
            return filterResults
        }

        override fun publishResults(
            charSequence: CharSequence,
            filterResults: FilterResults
        ) {
            @Suppress("UNCHECKED_CAST")
            filterList = filterResults.values as ArrayList<Photo?>
            notifyDataSetChanged()
        }
    }


    private fun loadImage(imvImage: ImageView, url: String) {
        Log.d("ImageUrl", url)
        if (!TextUtils.isEmpty(url)) {
            try {
                Glide.with(imvImage.context).setDefaultRequestOptions(
                    RequestOptions().error(R.drawable.loading).
                        diskCacheStrategy(DiskCacheStrategy.ALL)
                ).load(url).dontAnimate().into(imvImage)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun getImageUrl(photo: Photo?): String =
        "https://farm${photo?.farm}.static.flickr.com/${photo?.server}/${photo?.id}_${photo?.secret}.jpg"

    fun updateList(photosList: ArrayList<Photo?>) {
        photos.clear()
        filterList.clear()
        photos.addAll(photosList)
        filterList.addAll(photosList)
        notifyDataSetChanged()
    }
}