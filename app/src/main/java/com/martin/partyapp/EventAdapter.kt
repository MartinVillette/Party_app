package com.martin.partyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventAdapter(private val context: Context, private val eventList: List<Event>) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_item_layout, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent =  Intent(context, EventActivity::class.java)
            intent.putExtra("eventId", event.eventId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textEventName: TextView = itemView.findViewById(R.id.text_event_name)
        private val textEventDate: TextView = itemView.findViewById(R.id.text_event_date)
        private val textEventLastNotification: TextView = itemView.findViewById(R.id.text_event_last_notification)
        private val notificationDot: RelativeLayout = itemView.findViewById(R.id.notification_dot)
        private val eventImage: ImageView = itemView.findViewById(R.id.image_event)

        private val NOTIFICATION_LENGTH = 30

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        fun bind(event: Event) {
            //Event Name
            textEventName.text = event.eventName

            //Event Date
            val date = Date(event.eventTimestamp!!)
            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val formattedDate = dateFormat.format(date)
            textEventDate.text = formattedDate

            //Event Last Notification

            if (event.messages.size > 0){
                val lastMessage = event.messages.values.sortedBy{ it.date}.last()
                //val lastMessage = event.messages.last()
                val lastMessageContent = lastMessage.content!!
                val textLastMessage = if (lastMessageContent.length + lastMessage.senderUser!!.username!!.length > NOTIFICATION_LENGTH){
                    lastMessageContent.substring(0,NOTIFICATION_LENGTH - lastMessage.senderUser!!.username!!.length) + " ..."
                } else {
                    lastMessageContent
                }

                Glide.with(itemView)
                    .load(lastMessage.senderUser!!.profilePictureUrl)
                    .circleCrop()
                    .into(eventImage)

                textEventLastNotification.text = lastMessage.senderUser!!.username + " : " + textLastMessage
                textEventLastNotification.visibility = View.VISIBLE

                val authUserId = FirebaseAuth.getInstance().currentUser?.uid!!
                if (lastMessage.viewers.contains(authUserId)){
                    //the user has already seen the message
                    textEventLastNotification.setTextColor(context.resources.getColor(R.color.grey))
                    notificationDot.visibility = View.GONE
                } else {
                    textEventLastNotification.setTextColor(context.resources.getColor(R.color.white))
                    notificationDot.visibility = View.VISIBLE
                }
            } else {
                textEventLastNotification.visibility = View.GONE
            }
        }
    }
}