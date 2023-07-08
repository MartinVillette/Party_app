package com.martin.partyapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EventItemUserAdapter(private val context: Context, private val userMap: HashMap<User,Int>) :
    RecyclerView.Adapter<EventItemUserAdapter.EventItemUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemUserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_description_item_layout, parent, false)
        return EventItemUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventItemUserViewHolder, position: Int) {
        val user = userMap.keys.elementAt(position)
        val quantity = userMap[user]
        holder.bind(user, quantity!!)
    }

    override fun getItemCount(): Int {
        return userMap.size
    }

    inner class EventItemUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profilePictureImage: ImageView = itemView.findViewById(R.id.image_profile_picture)
        private val quantityText: TextView = itemView.findViewById(R.id.text_item_number)

        fun bind(user: User, quantity: Int) {
            quantityText.text = quantity.toString()

            Glide.with(itemView)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)
        }
    }
}