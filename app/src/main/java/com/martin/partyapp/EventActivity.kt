package com.martin.partyapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.sql.Timestamp

class EventActivity : AppCompatActivity() {

    private lateinit var toolbarBack: ImageButton
    private lateinit var user: User
    private lateinit var event: Event
    private lateinit var eventId: String
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var buttonSendMessage: ImageButton
    private lateinit var editMessage: EditText
    private lateinit var textEventName: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var messageList: ArrayList<Message> = ArrayList()
    private var firstTime: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            val intent = Intent(this@EventActivity, EventListActivity::class.java)
            startActivity(intent)
        }

        textEventName = findViewById(R.id.text_event_name)
        textEventName.setOnClickListener {
            val intent = Intent(this@EventActivity, EventDescriptionActivity::class.java)
            intent.putExtra(    "eventId", event.eventId)
            startActivity(intent)
        }

        editMessage = findViewById(R.id.edit_message)
        buttonSendMessage = findViewById(R.id.button_send_message)


        //get the User model of the connected user
        val userRef = database.getReference("User/${auth.currentUser?.uid!!}")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    user = snapshot.getValue(User::class.java)!!
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        eventId = intent.getStringExtra("eventId") ?: ""
        val eventRef = database.getReference("Event/$eventId")
        eventRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    event = snapshot.getValue(Event::class.java)?: Event()
                    run()
                 }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        buttonSendMessage.setOnClickListener {
            val messageContent = editMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                val message = Message()
                message.content = messageContent
                message.date = System.currentTimeMillis()
                message.senderUser = user

                val messageRef = database.getReference("Event/$eventId/messages").push()
                message.messageId = messageRef.key ?: ""
                messageRef.setValue(message)
                    .addOnSuccessListener {
                        notifyUsers(message)
                        editMessage.setText("")

                        editMessage.clearFocus()
                        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.hideSoftInputFromWindow(editMessage.windowToken, 0)
                    }
                /*
                event.messages.add(message)
                val eventRef = database.getReference("Event/$eventId")
                val updateData = HashMap<String, Any>()
                updateData["messages"] = event.messages
                //eventRef.setValue(event)
                eventRef.updateChildren(updateData)
                    .addOnSuccessListener {
                        notifyUsers(message)
                    }
                 */

            } else {
                Toast.makeText(this@EventActivity, "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun notifyUsers(message: Message){
        val url = "https://fcm.googleapis.com/fcm/send"
        val key = "AAAAkdpS1IA:APA91bF1JGK5v_wES0GvsX9xZlXdKo3xIfp99F1bM_VPGEKdjY6WI0ql9woioAU50ej3ufpkrpx1Gnm4Hxn7G8wD2RIXlASETUW85HqXZaKyzP5cvYaXfMVoSK2R-kgyNJL3CusVEi0C"
        for (userId in event.usersIds) {
            if (userId != auth.currentUser?.uid){
                val userFCMTokenRef = database.getReference("User/$userId/userFCMToken")
                userFCMTokenRef.addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            val token = snapshot.getValue(String::class.java) ?: ""
                            val bodyJson = JSONObject()
                            bodyJson.put("to", token)
                            bodyJson.put("data", JSONObject().apply {
                                put("title", event.eventName)
                                put("message", message.content)
                                put("eventId", eventId)
                                put("username", message.senderUser!!.username)
                            })

                            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                            val requestBody = bodyJson.toString().toRequestBody(mediaType)

                            val request = Request.Builder()
                                .url(url)
                                .addHeader("Content-Type", "application/json")
                                .addHeader("Authorization", "key=$key")
                                .post(requestBody)
                                .build()

                            val client = OkHttpClient()

                            client.newCall(request).enqueue(
                                object : Callback {
                                    override fun onResponse(call: Call, response: Response) {

                                    }

                                    override fun onFailure(call: Call, e: IOException) {
                                        println(e.message.toString())
                                    }
                                }
                            )

                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
        }
    }
    private fun run() {
        if (!event.usersIds.contains(auth.currentUser?.uid)){
            finish()
            val intent = Intent(this@EventActivity, EventListActivity::class.java)
            startActivity(intent)
        } else {
            textEventName.text = event.eventName

            messageAdapter = MessageAdapter(this, messageList, event)
            chatRecyclerView = findViewById(R.id.chat_recycler_view)
            chatRecyclerView.layoutManager = LinearLayoutManager(this)
            chatRecyclerView.adapter = messageAdapter

            val messagesRef = database.getReference("Event/$eventId/messages")
            messagesRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children){
                        val messageId = postSnapshot.key
                        if (messageId != null && postSnapshot.exists()){
                            postSnapshot.getValue(Message::class.java)?.let{
                                val message = it
                                val updateData = HashMap<String, Any>()
                                val authUserId = auth.currentUser?.uid!!
                                if (!message.viewers.contains(authUserId)){
                                    val messageRef = database.getReference("Event/$eventId/messages/$messageId")
                                    message.viewers.add(authUserId)
                                    updateData["viewers"] = message.viewers
                                    messageRef.updateChildren(updateData)
                                }
                                messageList.add(message)
                            }
                        }
                    }
                    messageAdapter.notifyDataSetChanged()
                    if (firstTime){
                        chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                        firstTime = false
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}