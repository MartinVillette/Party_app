package com.martin.partyapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class EventMembersFragment : Fragment() {

    private lateinit var eventId: String
    private lateinit var event: Event
    private lateinit var authUser: User

    private lateinit var database: FirebaseDatabase
    private var userList: ArrayList<String> = ArrayList()
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var auth: FirebaseAuth

    private lateinit var eventMembersNumberText: TextView
    private lateinit var addMembersButton: Button
    private lateinit var descriptionButton: LinearLayout
    private lateinit var itemsButton: LinearLayout
    private lateinit var expensesButton: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        adapter = UserAdapter(requireContext(), userList, authUser)
        userRecyclerView = view.findViewById(R.id.event_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter

        eventMembersNumberText = view.findViewById(R.id.text_event_members_number)

        val eventRef = database.getReference("Event/$eventId")
        eventRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    event = snapshot.getValue(Event::class.java)!!
                    userList.clear()
                    for (userId in event.usersIds){
                        if (userId != auth.currentUser?.uid!!){
                            userList.add(userId)
                        }
                    }
                    eventMembersNumberText.text = event.usersIds.size.toString()
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })


        addMembersButton = view.findViewById(R.id.button_add_members)
        addMembersButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventAddMembersFragment())
            transaction.commit()
        }

        descriptionButton = view.findViewById(R.id.button_description)
        descriptionButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventDescriptionFragment())
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            authUser = context.authUser
        }
    }
}