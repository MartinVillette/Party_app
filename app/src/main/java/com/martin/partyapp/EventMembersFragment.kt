package com.martin.partyapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.disklrucache.DiskLruCache.Value
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
    private lateinit var userList: ArrayList<User>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var auth: FirebaseAuth

    private lateinit var eventMembersNumberText: TextView

    private lateinit var descriptionButton: LinearLayout
    private lateinit var itemsButton: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        userList = ArrayList()

        adapter = UserAdapter(requireContext(), userList)
        userRecyclerView = view.findViewById(R.id.event_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter

        eventMembersNumberText = view.findViewById(R.id.text_event_members_number)

        descriptionButton = view.findViewById(R.id.button_description)
        descriptionButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, EventDescriptionFragment())
            transaction.commit()
        }
        itemsButton = view.findViewById(R.id.button_items)
        itemsButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.fragment_container, EventItemsFragment())
            transaction.commit()
        }

        for (user in event.users){
            if (user.userId != auth.currentUser?.uid!!){
                userList.add(user)
            }
        }
        eventMembersNumberText.text = userList.size.toString()
        adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            event = context.event
            authUser = context.authUser
        }
    }
}