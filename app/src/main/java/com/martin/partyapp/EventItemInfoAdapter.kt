package com.martin.partyapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EventItemInfoAdapter(private val context: Context, private val itemUserQuantity: List<ItemUserQuantity>) :
    RecyclerView.Adapter<EventItemInfoAdapter.EventItemInfoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): EventItemInfoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_description_item_info_layout, parent, false)
        return EventItemInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventItemInfoAdapter.EventItemInfoViewHolder, position: Int) {
        val itemInfo = itemUserQuantity[position]
        holder.bind(itemInfo)
    }

    override fun getItemCount(): Int {
        return itemUserQuantity.size
    }

    inner class EventItemInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var userNameText: TextView
        private lateinit var quantityText: TextView
        private lateinit var profilePictureImage: ImageView

        fun bind(itemUserQuantity: ItemUserQuantity){
            userNameText = itemView.findViewById(R.id.text_username)
            quantityText = itemView.findViewById(R.id.text_quantity)
            profilePictureImage = itemView.findViewById(R.id.image_profile_picture)

            userNameText.text = itemUserQuantity.user!!.username
            quantityText.text = itemUserQuantity.quantity.toString()

            Glide.with(itemView)
                .load(itemUserQuantity.user!!.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)
        }
    }
}