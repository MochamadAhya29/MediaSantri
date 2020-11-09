package com.mochamadahya.mediasantri.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.mochamadahya.mediasantri.R
import com.mochamadahya.mediasantri.model.Post
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.images_item_layout.view.*

class MyImagesAdapter (private val mContext: Context, mPost:List<Post>)
    : RecyclerView.Adapter<MyImagesAdapter.ViewHolder?> (){

    private var mPost: List<Post>? = null

    init {
        this.mPost = mPost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyImagesAdapter.ViewHolder {
        val view  = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyImagesAdapter.ViewHolder, position: Int) {
        val post: Post = mPost!![position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }

    class ViewHolder (itemView: View)
        : RecyclerView.ViewHolder(itemView){

        var postImage: ImageView = itemView.findViewById(R.id.post_image_grid)
    }

}