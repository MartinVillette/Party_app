package com.martin.partyapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
            intent.putExtra("eventId", event.eventId)
            startActivity(intent)
        }

        editMessage = findViewById(R.id.edit_message)
        buttonSendMessage = findViewById(R.id.button_send_message)


        //get the User model of the connected user
        val userId = auth.currentUser?.uid!!
        val userRef = database.getReference("User")
        userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    user = snapshot.getValue(User::class.java)!!
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        eventId = intent.getStringExtra("eventId") ?: ""
        val eventRef = database.getReference("Event")
        eventRef.child(eventId).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    event = snapshot.getValue(Event::class.java)?: Event()
                    run()
                 }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        buttonSendMessage.setOnClickListener {
            val messageContent = editMessage.text.toString()
            if (messageContent.isNotEmpty()) {
                val message = Message()
                message.content = messageContent
                message.date = System.currentTimeMillis()
                message.senderUser = user
                event.messages.add(message)
                val eventRef = database.getReference("Event/$eventId")
                eventRef.setValue(event)
                editMessage.setText("")

                editMessage.clearFocus()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(editMessage.windowToken, 0)
            } else {
                Toast.makeText(this@EventActivity, "Veuillez entrer un message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun run() {
        textEventName.text = event.eventName

        messageAdapter = MessageAdapter(this, messageList, event)
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        chatRecyclerView.adapter = messageAdapter

        val messageRef = database.getReference("Event/$eventId/messages")
        messageRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children.reversed()){
                    val currentMessage = messageSnapshot.getValue(Message::class.java)
                    messageList.add(currentMessage!!)
                }
                messageAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}