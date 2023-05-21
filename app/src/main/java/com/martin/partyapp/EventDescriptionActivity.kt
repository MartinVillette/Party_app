package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventDescriptionActivity : AppCompatActivity() {

    private lateinit var toolbarBack: ImageButton
    private lateinit var eventNameText: TextView
    private lateinit var fragmentManager: FragmentManager
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentList: ArrayList<Fragment>

    lateinit var authUser: User
    lateinit var event: Event
    lateinit var eventId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_description)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        eventId = intent.getStringExtra("eventId")!!

        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            val intent = Intent(this@EventDescriptionActivity, EventActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        eventNameText = findViewById(R.id.text_event_name)

        val eventRef = database.getReference("Event/$eventId")
        eventRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    event = snapshot.getValue(Event::class.java)!!
                    eventNameText.text = event.eventName

                    val authUserRef = database.getReference("User/${auth.currentUser?.uid}")
                    authUserRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                authUser = snapshot.getValue(User::class.java)!!

                                fragmentManager = supportFragmentManager
                                val transaction: FragmentTransaction = fragmentManager.beginTransaction()
                                transaction.add(R.id.fragment_container, EventDescriptionFragment())
                                transaction.commit()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}