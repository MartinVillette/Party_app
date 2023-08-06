package com.martin.partyapp

import android.annotation.SuppressLint
import android.content.Context
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
import io.opencensus.metrics.export.Value
import org.w3c.dom.Text
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class NewExpenseUsersConcernedFragment : Fragment() {

    private var expenseName: String? = null
    private var expensePrice: Float? = null
    private var userWhoPaidId: String? = null

    private lateinit var newExpense: Expense
    private lateinit var event: Event

    private lateinit var database: FirebaseDatabase

    private var usersConcerned: ArrayList<User> = ArrayList()
    private var userList: ArrayList<User> = ArrayList()
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: ExpenseUserAdapter
    private lateinit var searchBar: EditText

    companion object {
        private const val ARG_EXPENSE_NAME = "expense_name"
        private const val ARG_EXPENSE_PRICE = "expense_price"
        private const val ARG_USER_WHO_PAID = "user_who_paid"

        fun newInstance(expenseName: String, expensePrice: Float, userWhoPaidId: String): NewExpenseUsersConcernedFragment {
            val fragment = NewExpenseUsersConcernedFragment()
            val args = Bundle().apply {
                putString(ARG_EXPENSE_NAME, expenseName)
                putFloat(ARG_EXPENSE_PRICE, expensePrice)
                putString(ARG_USER_WHO_PAID, userWhoPaidId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            expenseName = it.getString(ARG_EXPENSE_NAME)
            expensePrice = it.getFloat(ARG_EXPENSE_PRICE)
            userWhoPaidId = it.getString(ARG_USER_WHO_PAID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_expense_users_concerned, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()

        val expenseNameText: TextView = view.findViewById(R.id.text_expense_name)
        expenseNameText.text = expenseName

        val expensePriceText: TextView = view.findViewById(R.id.text_expense_price)
        expensePriceText.text = expensePrice.toString() + "€"

        val userWhoPaidText: TextView = view.findViewById(R.id.text_user_who_paid)
        val userRef = database.getReference("User/$userWhoPaidId")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        userWhoPaidText.text = "@" + it.username
                        userWhoPaidText.setTextColor(event.usersColor[userWhoPaidId]!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        searchBar = view.findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList(searchBar.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        adapter = ExpenseUserAdapter(requireContext(), userList, usersConcerned)
        userRecyclerView = view.findViewById(R.id.expense_user_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter

        val selectAllButton: Button = view.findViewById(R.id.button_select_all)
        selectAllButton.setOnClickListener {
            for (user in userList){
                if (!usersConcerned.contains(user)){
                    usersConcerned.add(user)
                }
            }
            adapter.notifyDataSetChanged()
        }

        val nextButton:Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            saveNewExpense()
        }

        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }

        updateUserList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NewExpenseActivity){
            event = context.event
        }
    }

    private fun updateUserList(research: String = ""){
        userList.clear()
        adapter.notifyDataSetChanged()

        var userCounter = 0

        for (userId in event.usersIds) {
            val userRef = database.getReference("User/$userId")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        user?.let{
                            if (it.username!!.lowercase().startsWith(research.lowercase())){
                                userList.add(it)
                            }
                        }
                    }
                    userCounter++
                    if (userCounter == event.usersIds.size){
                        adapter.notifyDataSetChanged()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun saveNewExpense(){
        if (event.eventId != null ){
            newExpense = Expense()
            newExpense.expenseName = expenseName
            newExpense.usersConcerned = usersConcerned
            newExpense.price = expensePrice!!
            val userRef = database.getReference("User/$userWhoPaidId")
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        user?.let{
                            newExpense.userWhoPaid = user
                            val newExpenseRef = database.getReference("Event/${event.eventId}/expenses").push()
                            newExpense.expenseId = newExpenseRef.key ?: ""
                            newExpenseRef.setValue(newExpense)
                                .addOnSuccessListener {

                                    val fragmentManager = requireActivity().supportFragmentManager
                                    val transaction = fragmentManager.beginTransaction()
                                    val expensePictureFragment = NewExpensePictureFragment.newInstance(newExpense.expenseId!!)
                                    transaction.replace(R.id.fragment_container, expensePictureFragment)
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }


        /*
        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val expensePictureFragment = NewExpensePictureFragment.newInstance(event.eventId!!, newExpense.expenseId!!)
        transaction.replace(R.id.fragment_container, expensePictureFragment)
        transaction.addToBackStack(null)
        transaction.commit()

         */
    }
}