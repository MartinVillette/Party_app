package com.martin.partyapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text
import java.lang.NumberFormatException
import java.util.Calendar
import kotlin.math.exp

class NewExpensePriceFragment : Fragment() {
    private lateinit var event: Event
    private lateinit var authUser: User
    private var expenseName: String? = null

    private lateinit var database: FirebaseDatabase
    private lateinit var userWhoPaidId: String
    private lateinit var userWhoPaidText: TextView

    private var userList: ArrayList<User> = ArrayList()
    private lateinit var adapter: UserSimpleAdapter
    private lateinit var userRecyclerView: RecyclerView

    companion object {
        private const val ARG_EXPENSE_NAME = "expense_name"

        fun newInstance(expenseName: String): NewExpensePriceFragment {
            val fragment = NewExpensePriceFragment()
            val args = Bundle().apply {
                putString(ARG_EXPENSE_NAME, expenseName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            expenseName = it.getString(NewExpensePriceFragment.ARG_EXPENSE_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_expense_price, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()

        userWhoPaidId = authUser.userId!!
        userWhoPaidText = view.findViewById(R.id.text_user_who_paid)
        userWhoPaidText.text = "@${authUser.username}"
        userWhoPaidText.setTextColor(event.usersColor[authUser.userId]!!)
        userWhoPaidText.setOnClickListener {
            showUserWhoPaidPopup()
        }

        val priceEdit: EditText = view.findViewById(R.id.edit_expense_price)

        val fragmentManager = requireActivity().supportFragmentManager

        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {

            var expensePrice: Float = 0f
            try {
                expensePrice = priceEdit.text.toString().toFloat()
            } catch(e: NumberFormatException){
                //
            }
            if (expensePrice == 0f){
                Toast.makeText(requireContext(), "Enter a correct expense's price", Toast.LENGTH_SHORT)
            } else {
                val transaction = fragmentManager.beginTransaction()
                val usersConcernedFragment = NewExpenseUsersConcernedFragment.newInstance(expenseName!!, expensePrice, userWhoPaidId)
                transaction.replace(R.id.fragment_container, usersConcernedFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            fragmentManager.popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NewExpenseActivity){
            event = context.event
            authUser = context.authUser
        }
    }

    private fun showUserWhoPaidPopup(){

        val dialogBuilder = AlertDialog.Builder(context)
        val popupView = LayoutInflater.from(context).inflate(R.layout.expense_user_who_paid_popup, null)

        dialogBuilder.setView(popupView)
        val alertDialog = dialogBuilder.create()

        adapter = UserSimpleAdapter(requireContext(), userList) { user ->
            selectUserWhoPaid(user)
            alertDialog.dismiss()
        }
        userRecyclerView = popupView.findViewById(R.id.user_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        userRecyclerView.adapter = adapter
        updateUserList()

        val searchBar: EditText = popupView.findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList(searchBar.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun selectUserWhoPaid(user: User){
        userWhoPaidId = user.userId!!
        userWhoPaidText.text = "@${user.username}"
        userWhoPaidText.setTextColor(event.usersColor[user.userId]!!)
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
                                    Log.e("UserList", userList.toString())
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
}