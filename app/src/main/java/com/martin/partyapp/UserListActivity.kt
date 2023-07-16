package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListActivity : AppCompatActivity() {

    private lateinit var toolbarBack: ImageButton
    private lateinit var searchBar: EditText
    private lateinit var userList: ArrayList<String>
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var adapter: UserAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            val intent = Intent(this@UserListActivity, ContactListActivity::class.java)
            startActivity(intent)
        }

        userList = ArrayList()

        adapter = UserAdapter(this, userList)

        userRecyclerView = findViewById(R.id.user_recycler_view)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.adapter = adapter

        searchBar = findViewById(R.id.edit_research)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUserList(searchBar.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout){
            //logout
            auth.signOut()
            val intent = Intent(this@UserListActivity, LoginActivity::class.java)
            finish()
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.events){
            val intent = Intent(this@UserListActivity, EventListActivity::class.java)
            startActivity(intent)
            return true
        }
        return true
    }

    private fun updateUserList(research: String){
        if (research != ""){
            database.getReference("User").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()
                    for (postSnapshot in snapshot.children){
                        val currentUser = postSnapshot.getValue(User::class.java)
                        val username = currentUser!!.username!!.lowercase()
                        if (username.startsWith(research.lowercase()) && currentUser.userId != auth.currentUser?.uid){
                            userList.add(currentUser.userId!!)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
    }
}