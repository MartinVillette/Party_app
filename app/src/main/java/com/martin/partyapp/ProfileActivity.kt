
package com.martin.partyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var database: FirebaseDatabase
    private lateinit var user: User

    private lateinit var userNameText: TextView
    private lateinit var userProfilePicture: ImageView
    private lateinit var toolbarBack: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        database = FirebaseDatabase.getInstance()

        userId = intent.getStringExtra("userId")!!
        toolbarBack = findViewById(R.id.button_back_toolbar)
        toolbarBack.setOnClickListener {
            finish()
        }

        val userRef = database.getReference("User/$userId")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    user = snapshot.getValue(User::class.java)!!
                    updateUserInfo(user)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateUserInfo(user:User){
        userProfilePicture = findViewById(R.id.image_profile)
        userNameText = findViewById(R.id.text_username)

        Glide.with(this)
            .load(user.profilePictureUrl)
            .circleCrop()
            .into(userProfilePicture)
        userNameText.text = user.username
    }
}