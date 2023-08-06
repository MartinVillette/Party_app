package com.martin.partyapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ExpenseUserAdapter(private val context: Context, private val userList: List<User>, private val usersConcerned: ArrayList<User>) :
    RecyclerView.Adapter<ExpenseUserAdapter.ExpenseUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseUserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_user_item_layout, parent, false)
        return ExpenseUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseUserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)

        holder.checkBox.isChecked = usersConcerned.contains(user)
        //Toggle the checkbox when pressed on the item
        holder.itemLayout.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
            if (holder.checkBox.isChecked){
                usersConcerned.add(user)
            } else {
                usersConcerned.remove(user)
            }
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ExpenseUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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