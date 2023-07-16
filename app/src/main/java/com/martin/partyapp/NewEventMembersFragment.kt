package com.martin.partyapp

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Mms.Addr
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle.Delegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import org.w3c.dom.Text
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class NewEventMembersFragment : Fragment() {
    private var eventName: String? = null
    private var eventTimestamp: Long? = null
    private var eventAddress: Address? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userList: ArrayList<User>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: EventUserAdapter
    private lateinit var newEvent: Event
    private lateinit var searchBar: EditText
    private lateinit var createEventButton: Button
    private lateinit var membersNumberText: TextView

    companion object {
        private const val ARG_EVENT_NAME = "event_name"
        private const val ARG_EVENT_TIMESTAMP = "event_timestamp"
        private const val ARG_EVENT_ADDRESS = "event_address"


        fun newInstance(eventName: String, eventTimestamp: Long, eventAddressJson: String): NewEventMembersFragment {
            val fragment = NewEventMembersFragment()
            val args = Bundle().apply {
                putString(ARG_EVENT_NAME, eventName)
                putLong(ARG_EVENT_TIMESTAMP, eventTimestamp)
                putString(ARG_EVENT_ADDRESS, eventAddressJson)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventName = it.getString(ARG_EVENT_NAME)
            eventTimestamp = it.getLong(ARG_EVENT_TIMESTAMP)
            eventAddress = Gson().fromJson(it.getString(ARG_EVENT_ADDRESS), Address::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_event_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        searchBar = view.findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList(searchBar.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })


        newEvent = Event()
        userList = ArrayList()

        val userId = auth.currentUser?.uid
        val userRef = database.getReference("User")
        userRef.child(userId!!).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val authUser = snapshot.getValue(User::class.java)
                    newEvent.addUserToEvent(authUser!!)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        val eventNameText: TextView = view.findViewById(R.id.text_event_name)
        eventNameText.text = eventName

        val date = Date(eventTimestamp!!)
        val dateFormat = SimpleDateFormat("dd/MM/yy - HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val eventDateText: TextView = view.findViewById(R.id.text_event_date)
        eventDateText.text = formattedDate

        membersNumberText = view.findViewById(R.id.text_event_members_number)

        createEventButton = view.findViewById(R.id.button_create)
        createEventButton.setOnClickListener {
            //create the event
            saveNewEvent(newEvent)
        }

        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }

        adapter = EventUserAdapter(requireContext(), newEvent, userList)
        userRecyclerView = view.findViewById(R.id.event_user_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter

        updateUserList()
    }

    private fun updateUserList(research: String = ""){
        val userId = auth.currentUser?.uid
        database.getReference("User/$userId/following").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)
                    val username = currentUser!!.username!!.lowercase()
                    if (username.startsWith(research.lowercase()) && currentUser.userId != auth.currentUser?.uid){
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun saveNewEvent(event: Event) {
        event.eventName = eventName
        event.eventTimestamp = eventTimestamp
        event.eventAddress = eventAddress

        //Add the event to the database
        val newEventRef = database.getReference("Event").push()
        event.eventId = newEventRef.key ?: ""
        newEventRef.setValue(event)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // If the event if added, we add the event to all users' events
                    for (userId in event.usersIds){
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
                    val intent =  Intent(requireContext(), EventActivity::class.java)
                    intent.putExtra("eventId", event.eventId)
                    startActivity(intent)
                } else {
                    //Error
                    Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }
}