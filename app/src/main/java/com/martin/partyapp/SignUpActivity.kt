package com.martin.partyapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var editUsername: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var buttonLogin: Button
    private lateinit var buttonBackToolbar: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var fragmentManager: FragmentManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //auth = FirebaseAuth.getInstance()

        buttonBackToolbar = findViewById(R.id.button_back_toolbar)
        buttonBackToolbar.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        fragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, SignUpUsernameFragment())
        transaction.commit()

        /*
        editUsername = findViewById(R.id.edit_username)
        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        buttonSignUp = findViewById(R.id.button_signup)
        buttonLogin = findViewById(R.id.button_login)

        buttonLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        buttonSignUp.setOnClickListener {
            var username = editUsername.text.toString()
            var email = editEmail.text.toString()
            var password = editPassword.text.toString()

            signup(username, email, password)
        }

        */
    }

    private fun signup(username:String, email:String, password:String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    addUserToData(username, email, auth.currentUser?.uid!!)
                    val intent = Intent(this@SignUpActivity, ContactListActivity::class.java)
                    startActivity(intent)

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this@SignUpActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToData(username: String, email: String, userId: String){
        database = FirebaseDatabase.getInstance().getReference("User")
        database.child(userId).setValue(User(userId,username,email))
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Data inserted success", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }

}