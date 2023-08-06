package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import io.opencensus.metrics.export.Value

class NewExpenseActivity : AppCompatActivity() {
    lateinit var event: Event
    lateinit var authUser: User

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var toolbarBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)

        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            finish()
        }
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val eventId = intent.getStringExtra("eventId")

        val eventRef = database.getReference("Event/$eventId")
        eventRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    event = snapshot.getValue(Event::class.java)!!
                    val authUserRef = database.getReference("User/${auth.currentUser?.uid!!}")
                    authUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                authUser = snapshot.getValue(User::class.java)!!
                                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                                transaction.add(R.id.fragment_container, NewExpenseNameFragment())
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