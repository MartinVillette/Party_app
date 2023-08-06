package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContactListActivity : AppCompatActivity() {

    private lateinit var searchBar: EditText
    private lateinit var newContactButton: Button
    var userType: String = "following" //[following,followers,requests]
    private lateinit var followerButton: LinearLayout
    private lateinit var followingButton: LinearLayout
    private lateinit var requestButton: LinearLayout
    private lateinit var userNameText: TextView
    private var userList: ArrayList<String> = ArrayList()
    private lateinit var adapter: UserAdapter
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var buttonBackToolbar: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var authUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        userNameText = findViewById(R.id.text_username)
        val authUserRef = database.getReference("User/${auth.currentUser?.uid!!}")
        authUserRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    authUser = snapshot.getValue(User::class.java)!!
                    userNameText.text = authUser.username

                    adapter = UserAdapter(this@ContactListActivity, userList, authUser)

                    userRecyclerView = findViewById(R.id.user_recycler_view)
                    userRecyclerView.layoutManager = LinearLayoutManager(this@ContactListActivity)
                    userRecyclerView.adapter = adapter

                    updateUserList("", userType)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        buttonBackToolbar = findViewById(R.id.button_back_toolbar)
        buttonBackToolbar.setOnClickListener {
            val intent = Intent(this@ContactListActivity, MainActivity::class.java)
            startActivity(intent)
        }

        newContactButton = findViewById(R.id.button_new_contact)

        newContactButton.setOnClickListener {
            val intent = Intent(this@ContactListActivity, UserListActivity::class.java)
            startActivity(intent)
        }

        followerButton = findViewById(R.id.button_follower)
        followingButton = findViewById(R.id.button_following)
        requestButton = findViewById(R.id.button_request)

        searchBar = findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList(searchBar.text.toString(), userType)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        followerButton.setOnClickListener {
            userType = "followers"
            searchBar.setText("")
            searchBar.visibility = View.VISIBLE
            followerButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background_colored)
            followingButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background)
            requestButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background)
        }
        followingButton.setOnClickListener {
            userType = "following"
            searchBar.setText("")
            searchBar.visibility = View.VISIBLE
            followerButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background)
            followingButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background_colored)
            requestButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background)
        }
        requestButton.setOnClickListener {
            userType = "requests"
            searchBar.setText("")
            searchBar.visibility = View.GONE
            followerButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background)
            followingButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background)
            requestButton.getChildAt(0).setBackgroundResource(R.drawable.circle_background_colored)
        }
    }

    private fun updateUserList(research: String, userType: String){
        userList.clear()
        adapter.notifyDataSetChanged()

        val userRef = database.getReference("User/${auth.currentUser?.uid!!}/$userType")
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val currentUser = postSnapshot.getValue(User::class.java)
                    val username = currentUser!!.username!!.lowercase()
                    if (username.startsWith(research.lowercase()) && currentUser.userId != auth.currentUser?.uid){
                        userList.add(currentUser.userId!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}