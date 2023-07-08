package com.martin.partyapp

import android.content.Context
import android.content.Intent
import android.provider.Telephony.Mms.Sent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageAdapter (val context: Context, private val messageList: ArrayList<Message>, private val event: Event):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVED = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVED){
            val view = LayoutInflater.from(context).inflate(R.layout.message_received_layout, parent, false)
            ReceivedViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.message_sent_layout, parent, false)
            SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (holder.itemViewType == ITEM_SENT) {
            //sent message
            val viewHolder = holder as SentViewHolder
            viewHolder.bind(message, event)
        } else {
            //received message
            val viewHolder = holder as ReceivedViewHolder
            viewHolder.bind(message, event)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == message.senderUser?.userId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val textMessageSent: TextView = itemView.findViewById(R.id.text_message_sent)
        //private val profilePictureImage: ImageView = itemView.findViewById(R.id.image_profile_picture)

        fun bind(message: Message, event: Event){
            textMessageSent.text = message.content
            textMessageSent.setTextColor(event.usersColor[message.senderUser!!.userId]!!)
            /*
            Glide.with(itemView)
                .load(message.senderUser!!.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)*/
        }
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val textMessageReceived: TextView = itemView.findViewById(R.id.text_message_received)
        private val profilePictureImage: ImageView = itemView.findViewById(R.id.image_profile_picture)

        fun bind(message: Message, event: Event){
            textMessageReceived.text = message.content
            textMessageReceived.setTextColor(event.usersColor[message.senderUser!!.userId]!!)

            Glide.with(itemView)
                .load(message.senderUser!!.profilePictureUrl)
                .circleCrop()
                .into(profilePictureImage)

            profilePictureImage.setOnClickListener {
                val intent = Intent(itemView.context, ProfileActivity::class.java)
                intent.putExtra("userId", message.senderUser!!.userId)
                itemView.context.startActivity(intent)
            }
        }
    }
}