package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.sql.Timestamp
import java.util.Calendar

class NewEventActivity : AppCompatActivity() {


    private lateinit var fragmentManager: FragmentManager
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private var fragmentList: ArrayList<Fragment> = ArrayList()
    private var fragmentIndex: Int = 0

    private lateinit var toolbarBack: ImageButton
    private lateinit var newEvent: Event
    private lateinit var userList: ArrayList<User>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: EventUserAdapter
    private lateinit var editEventName: EditText
    private lateinit var datePickerEvent: DatePicker
    private lateinit var timePickerEvent: TimePicker
    private lateinit var buttonNewEvent: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            val intent = Intent(this@NewEventActivity, EventListActivity::class.java)
            startActivity(intent)
        }

        fragmentList.add(NewEventNameFragment())
        fragmentManager = supportFragmentManager


        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, NewEventNameFragment())
        transaction.commit()

        /*
        newEvent = Event()

        nextButton = findViewById(R.id.button_next)
        previousButton = findViewById(R.id.button_previous)

        nextButton.setOnClickListener {
            if (fragmentIndex == 0){
                //name
                val eventName = findViewById<EditText>(R.id.edit_event_name).text.toString()
                fragmentList.add(NewEventDateFragment.newInstance(eventName))
            }
            fragmentIndex++
            showFragment()
        }
        previousButton.setOnClickListener {
            fragmentIndex--
            showFragment()
        }
        showFragment()

        */



        /*
        editEventName = findViewById(R.id.edit_event_name)
        datePickerEvent = findViewById(R.id.date_event)
        timePickerEvent = findViewById(R.id.time_event)
        buttonNewEvent = findViewById(R.id.button_new_event)
        initiatePickers(datePickerEvent, timePickerEvent)


        buttonNewEvent.setOnClickListener {
            val eventName = editEventName.text.toString()

            val year = datePickerEvent.year
            val month = datePickerEvent.month
            val day = datePickerEvent.dayOfMonth
            val hour = timePickerEvent.hour
            val minute = timePickerEvent.minute
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day, hour, minute)
            val eventTimestamp = Timestamp(calendar.timeInMillis)

            saveNewEvent(event, eventName, eventTimestamp)
        }

        userList = ArrayList()

        event = Event()
        //Add the user to the event users
        val userId = auth.currentUser?.uid
        val userRef = database.getReference("User")
        userRef.child(userId!!).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val authUser = snapshot.getValue(User::class.java)
                    event.addUserToEvent(authUser!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        adapter = EventUserAdapter(this, event, userList)


        userRecyclerView = findViewById(R.id.event_user_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        updateUserList()
        */
    }

    private fun showFragment(){
        val fragment: Fragment = fragmentList[fragmentIndex]
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun initiatePickers(datePicker: DatePicker, timePicker: TimePicker){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        datePicker.init(year, month, dayOfMonth, null)

        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        timePicker.setIs24HourView(true)
        timePicker.hour = hour
        timePicker.minute = minute
    }

    private fun updateUserList(){
        val userId = auth.currentUser?.uid
        database.getReference("User/$userId/following").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)
                    userList.add(currentUser!!)
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun saveNewEvent(event: Event, eventName: String, eventTimestamp: Timestamp){
        event.eventName = eventName
        //event.eventTimestamp = eventTimestamp

        //Add the event to the database
        val newEventRef = database.getReference("Event").push()
        event.eventId = newEventRef.key ?: ""
        newEventRef.setValue(event)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If the event if added, we add the event to all users' events
                    for (user in event.users){
                        val userId = user.userId!!
                        val userRef = database.getReference("User")
                        userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    val currentUser = snapshot.getValue(User::class.java)
                                    currentUser!!.addEvent(event)
                                    userRef.child(userId!!).setValue(currentUser)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                    //Redirect to event list
                    val intent = Intent(this@NewEventActivity, EventListActivity::class.java)
                    startActivity(intent)
                } else {
                    //Error
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }
}