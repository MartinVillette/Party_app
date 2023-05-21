package com.martin.partyapp

import android.content.Context
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class EventUserAdapter(private val context: Context, private val event: Event, private val userList: List<User>) :
    RecyclerView.Adapter<EventUserAdapter.EventUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventUserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_user_item_layout, parent, false)
        return EventUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventUserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)

        val eventUserIds: List<String> = event.users.map{ it.userId!! }

        holder.checkBox.isChecked = eventUserIds.contains(user.userId)
        //Toggle the checkbox when pressed on the item
        holder.itemLayout.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            if (holder.checkBox.isChecked){
                event.addUserToEvent(user)
            } else {
                event.removeUserFromEvent(user.userId!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class EventUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.text_username)
        private val profilePictureImage: ImageView = itemView.findViewById(R.id.image_profile_picture)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_user)
        val itemLayout: LinearLayout = itemView.findViewById(R.id.event_item_layout)

        fun bind(user: User) {
            nameTextView.text = user.username
            Glide.with(itemView)
                .load(user.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)
        }
    }
}