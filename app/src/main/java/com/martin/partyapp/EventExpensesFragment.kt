package com.martin.partyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventExpensesFragment: Fragment() {
    private lateinit var event: Event
    private lateinit var authUser: User
    private lateinit var eventId: String

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private var expenses: ArrayList<Expense> = ArrayList()
    private lateinit var adapter: EventExpenseAdapter
    private lateinit var eventExpensesRecyclerView: RecyclerView

    private lateinit var membersButton: LinearLayout
    private lateinit var descriptionButton: LinearLayout
    private lateinit var itemsButton: LinearLayout
    private lateinit var balanceButton: Button

    private lateinit var addExpenseButton: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        membersButton = view.findViewById(R.id.button_members)
        membersButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventMembersFragment())
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
        itemsButton.setOnClickListener{
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventItemsFragment())
            transaction.commit()
        }
        balanceButton = view.findViewById(R.id.button_balance)
        balanceButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventBalanceFragment())
            transaction.commit()
        }

        addExpenseButton = view.findViewById(R.id.button_add_expense)
        addExpenseButton.setOnClickListener {
            val intent = Intent(requireContext(), NewExpenseActivity::class.java)
            intent.putExtra("eventId", eventId)
            startActivity(intent)
        }

        eventExpensesRecyclerView = view.findViewById(R.id.expenses_recycler_view)
        eventExpensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val eventExpensesRef = database.getReference("Event/$eventId/expenses")
        eventExpensesRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                expenses.clear()
                for (postSnapshot in snapshot.children){
                    if (postSnapshot.key != null && postSnapshot.exists()) {
                        postSnapshot.getValue(Expense::class.java)?.let{
                            expenses.add(it)
                        }
                    }
                }
                val usersColor = HashMap<String, Int>()
                val eventUsersColorRef = database.getReference("Event/$eventId/usersColor")
                eventUsersColorRef.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (postSnapshot in snapshot.children){
                            val userId = postSnapshot.key
                            if (userId != null && postSnapshot.exists()){
                                val userColor = postSnapshot.getValue(Int::class.java)
                                userColor?.let{
                                    usersColor[userId] = it
                                }
                            }
                        }
                        adapter = EventExpenseAdapter(requireContext(), usersColor, expenses, eventId)
                        eventExpensesRecyclerView.adapter = adapter
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
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
}