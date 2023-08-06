package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventListActivity : AppCompatActivity() {

    private lateinit var contactsButton: LinearLayout
    private lateinit var eventsButton: LinearLayout
    private lateinit var buttonNewEvent: Button
    private lateinit var eventList: ArrayList<Event>
    private lateinit var eventRecyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var searchBar: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private var allUserEvents: ArrayList<Event> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        contactsButton = findViewById(R.id.button_contacts)
        eventsButton = findViewById(R.id.button_events)

        contactsButton.setOnClickListener {
            val intent = Intent(this@EventListActivity, ContactListActivity::class.java)
            startActivity(intent)
        }
        eventsButton.setOnClickListener {
            val intent = Intent(this@EventListActivity, EventListActivity::class.java)
            startActivity(intent)
        }

        eventList = ArrayList()
        adapter = EventAdapter(this, eventList)

        eventRecyclerView = findViewById(R.id.event_recycler_view)
        eventRecyclerView.layoutManager = LinearLayoutManager(this)
        eventRecyclerView.adapter = adapter

        searchBar = findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateEventList(searchBar.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        buttonNewEvent = findViewById(R.id.button_new_event)
        buttonNewEvent.setOnClickListener{
            val intent = Intent(this@EventListActivity, NewEventActivity::class.java)
            startActivity(intent)
        }
        initiateEventList()
    }

    private fun initiateEventList(){
        val userId = auth.currentUser?.uid
        val eventIdsRef = database.getReference("User/$userId/eventIds")
        eventIdsRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventIds: ArrayList<String> = ArrayList()

                for (postSnapshot in snapshot.children){
                    if (postSnapshot.exists()){
                        postSnapshot.getValue(String::class.java)?.let{
                            eventIds.add(it)
                        }
                    }
                }

                for (eventId in eventIds){
                    val eventRef = database.getReference("Event/$eventId")
                    eventRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val event = snapshot.getValue(Event::class.java)
                                event?.let{
                                    allUserEvents.add(it)
                                }
                            }

                            if (allUserEvents.size == eventIds.size){
                                updateEventList()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
    private fun updateEventList(research: String = ""){
        eventList.clear()
        for (event in allUserEvents){
            val eventName = event.eventName!!.lowercase()
            if (eventName.startsWith(research.lowercase())){
                eventList.add(event)
            }
        }

        /*
        eventList.sortWith(compareByDescending {
            if (it.messages.size > 0){
                it.messages.last().date
            } else {
                0
            }
        })
         */
        adapter.notifyDataSetChanged()
    }
}