package com.martin.partyapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventExpenseActivity : AppCompatActivity() {
    private var expenseId: String ?= null
    private var eventId: String ?= null
    private lateinit var expense: Expense
    private var userColor: Int = 0

    private lateinit var toolbarBack: ImageButton

    private lateinit var database:FirebaseDatabase

    private lateinit var expenseNameText: TextView
    private lateinit var userWhoPaidText: TextView
    private lateinit var priceText: TextView
    private lateinit var allUsersConcernedText: TextView
    private lateinit var expensePicture: ImageView

    private lateinit var usersConcernedRecycler: RecyclerView
    private lateinit var adapter: EventExpenseUserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_expense)

        database = FirebaseDatabase.getInstance()

        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            finish()
        }

        intent.getStringExtra("expenseId")?.let{ expenseId = it }
        intent.getStringExtra("eventId")?.let { eventId = it}

        expenseNameText = findViewById(R.id.text_expense_name)
        userWhoPaidText = findViewById(R.id.text_user_who_paid)
        priceText = findViewById(R.id.text_price)
        allUsersConcernedText = findViewById(R.id.text_all_users_concerned)
        expensePicture = findViewById(R.id.image_expense_picture)

        usersConcernedRecycler = findViewById(R.id.users_concerned_recycler_view)
        usersConcernedRecycler.layoutManager = LinearLayoutManager(this)

        if (eventId != null && expenseId != null){
            val expenseRef = database.getReference("Event/$eventId/expenses/$expenseId")
            expenseRef.addListenerForSingleValueEvent(object:ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        snapshot.getValue(Expense::class.java)?.let {
                            expense = it
                            expenseNameText.text = expense.expenseName
                            priceText.text = expense.price.toString()
                            userWhoPaidText.text = "@" + expense.userWhoPaid!!.username
                        }

                        val userColorRef = database.getReference("Event/$eventId/usersColor/${expense.userWhoPaid!!.userId}")
                        userColorRef.addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    snapshot.getValue(Int::class.java)?.let {
                                        userColor = it
                                        userWhoPaidText.setTextColor(it)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })

                        val eventUsersIdsRef = database.getReference("Event/$eventId/users")
                        eventUsersIdsRef.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val usersIds = ArrayList<String>()
                                for (postSnapshot in snapshot.children){
                                    if (postSnapshot.exists()){
                                        postSnapshot.getValue(String::class.java)?.let {
                                            usersIds.add(it)
                                        }
                                    }
                                }
                                if (usersIds.size == expense.usersConcerned.size) {
                                    //all
                                    allUsersConcernedText.text = "@all"
                                } else {
                                    adapter = EventExpenseUserAdapter(this@EventExpenseActivity, expense.usersConcerned, expense)
                                    usersConcernedRecycler.adapter = adapter
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })

                        Glide.with(this@EventExpenseActivity)
                            .load(expense.pictureUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .transform(CenterCrop())
                            .into(expensePicture)

                        if (expense.pictureUrl != null){
                            expensePicture.setOnClickListener {
                                showPicturePopup(expense)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun showPicturePopup(expense: Expense){
        val dialogBuilder = AlertDialog.Builder(this@EventExpenseActivity)
        val popupView = LayoutInflater.from(this@EventExpenseActivity).inflate(R.layout.picture_popup, null)

        val popupPicture = popupView.findViewById<ImageView>(R.id.image_popup_picture)

        Glide.with(this@EventExpenseActivity)
            .load(expense.pictureUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(popupPicture)

        dialogBuilder.setView(popupView)
        val alertDialog = dialogBuilder.create()

        alertDialog.show()
    }
}