package com.martin.partyapp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class EventAddMembersFragment: Fragment() {
    private lateinit var authUser: User
    private lateinit var event: Event
    private lateinit var eventId: String

    private lateinit var database: FirebaseDatabase

    private var userList: ArrayList<User> = ArrayList()
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: EventUserAdapter
    private lateinit var searchBar: EditText
    private lateinit var addMembersButton: Button
    private lateinit var membersNumberText: TextView

    private lateinit var descriptionButton: LinearLayout
    private lateinit var membersButton: LinearLayout
    private lateinit var itemsButton: LinearLayout
    private lateinit var expensesButton: LinearLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_add_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()

        descriptionButton = view.findViewById(R.id.button_description)
        descriptionButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventMembersFragment())
            transaction.commit()
        }
        membersButton = view.findViewById(R.id.button_members)
        membersButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventMembersFragment())
            transaction.commit()
        }
        itemsButton = view.findViewById(R.id.button_items)
        itemsButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventItemsFragment())
            transaction.commit()
        }
        expensesButton = view.findViewById(R.id.button_expenses)
        expensesButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventExpensesFragment())
            transaction.commit()
        }

        searchBar = view.findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList(searchBar.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        addMembersButton = view.findViewById(R.id.button_add_members_to_event)
        addMembersButton.setOnClickListener {
            //create the event
            addMembers()
        }

        val cancelButton: Button = view.findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }

        userRecyclerView = view.findViewById(R.id.event_user_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        val eventRef = database.getReference("Event/$eventId")
        eventRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    event = snapshot.getValue(Event::class.java)!!
                    adapter = EventUserAdapter(requireContext(), event, userList)
                    userRecyclerView.adapter = adapter

                    updateUserList()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            authUser = context.authUser
        }
    }

    private fun updateUserList(research: String = ""){
        userList.clear()
        for (user in authUser.following){
            if (user.userId !in event.usersIds && user.username!!.startsWith(research.lowercase())){
                userList.add(user)
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun addMembers(){
        val database = FirebaseDatabase.getInstance()

        for (user in userList){
            val updateData = HashMap<String,Any>()
            val userEvents = user.eventIds
            userEvents.add(eventId)
            updateData["eventIds"] = userEvents
            val userRef = database.getReference("User/${user.userId}")
            userRef.updateChildren(updateData)
        }

        val updateData = HashMap<String, Any>()
        updateData["usersIds"] = event.usersIds
        updateData["usersColor"] = event.usersColor

        val eventRef = database.getReference("Event/$eventId")
        eventRef.updateChildren(updateData)
            .addOnSuccessListener {
                val fragmentManager = requireActivity().supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, EventMembersFragment())
                transaction.commit()
            }
    }
}