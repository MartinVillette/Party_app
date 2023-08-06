package com.martin.partyapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.opencensus.metrics.export.Value
import java.math.BigDecimal
import java.math.RoundingMode

class EventBalanceFragment: Fragment() {
    private lateinit var eventId: String
    private lateinit var authUser: User
    private lateinit var event: Event

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var adapter: EventTransactionAdapter
    private lateinit var balanceRecyclerView: RecyclerView
    private var transactions = ArrayList<Transaction>()

    private lateinit var membersButton: LinearLayout
    private lateinit var descriptionButton: LinearLayout
    private lateinit var itemsButton: LinearLayout
    private lateinit var expensesButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_balance, container, false)
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
        expensesButton = view.findViewById(R.id.button_expenses)
        expensesButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventExpensesFragment())
            transaction.commit()
        }

        balanceRecyclerView = view.findViewById(R.id.balances_recycler_view)
        balanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val eventRef = database.getReference("Event/$eventId")
        eventRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    snapshot.getValue(Event::class.java)?.let{
                        event = it
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        val balanceButton: Button = view.findViewById(R.id.button_balance)
        balanceButton.setOnClickListener {
            event.makeBalance(database)
            /*
            val updateData = HashMap<String, Any>()
            updateData["transactions"] = event.transactions
            eventRef.updateChildren(updateData)*/
        }

        val transactionsRef = database.getReference("Event/$eventId/transactions")
        transactionsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactions.clear()
                for (postSnapshot in snapshot.children) {
                    if (postSnapshot.key != null && postSnapshot.exists()) {
                        postSnapshot.getValue(Transaction::class.java)?.let {
                            if (it.userIdTo == authUser.userId!! || it.userIdFrom == authUser.userId!!){
                                transactions.add(it)
                            }
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
                        adapter = EventTransactionAdapter(requireContext(), transactions, usersColor, eventId)
                        balanceRecyclerView.adapter = adapter
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        val usernameText = view.findViewById<TextView>(R.id.text_username)
        val userBalanceText = view.findViewById<TextView>(R.id.text_user_balance)

        val balanceMapRef = database.getReference("Event/$eventId/balanceMap")
        balanceMapRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val balanceMap = HashMap<String, Float>()
                for (postSnapshot in snapshot.children) {
                    if (postSnapshot.key != null && postSnapshot.exists()) {
                        postSnapshot.getValue(Float::class.java)?.let {
                            balanceMap[postSnapshot.key!!] = it
                            if (postSnapshot.key!! == authUser.userId){
                                usernameText.text = "@${authUser.username}"
                                usernameText.setTextColor(event.usersColor[authUser.userId]!!)

                                val bigDecimal = BigDecimal(it.toString())
                                val roundedBigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN)
                                val userBalance = roundedBigDecimal.toFloat()

                                if (it > 0){
                                    userBalanceText.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                                    userBalanceText.text = "+$userBalance€"
                                } else {
                                    userBalanceText.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                                    userBalanceText.text = "-$userBalance€"
                                }
                            }
                        }

                    }
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
}