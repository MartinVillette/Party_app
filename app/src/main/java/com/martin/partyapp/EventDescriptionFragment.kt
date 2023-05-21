package com.martin.partyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventDescriptionFragment : Fragment() {

    private lateinit var authUser: User
    private lateinit var event: Event
    private lateinit var eventId: String

    private lateinit var quitEventButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var membersButton: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        membersButton = view.findViewById(R.id.button_members)
        membersButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, EventMembersFragment())
            transaction.commit()
        }

        quitEventButton = view.findViewById(R.id.button_quit_event)
        quitEventButton.setOnClickListener {
            quitEvent()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            event = context.event
            authUser = context.authUser
        }
    }

    private fun quitEvent(){
        // remove the Event from the User's event List
        val authUserRef = database.getReference("User/${auth.currentUser?.uid!!}")
        authUser.removeUserFromEvent(eventId)
        authUserRef.setValue(authUser)

        // remove User from the Event
        val eventRef = database.getReference("Event/$eventId")
        event.removeUserFromEvent(auth.currentUser?.uid!!)
        eventRef.setValue(event)

        val intent = Intent(requireContext(), EventListActivity::class.java)
        startActivity(intent)
    }
}