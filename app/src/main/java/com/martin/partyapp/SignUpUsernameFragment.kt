package com.martin.partyapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUpUsernameFragment : Fragment() {
    private lateinit var database: FirebaseDatabase
    private lateinit var userNameEditText: EditText
    private lateinit var userNameValidityText: TextView
    private var userNameValidity: Boolean = false
    private var userNameList: ArrayList<String> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_username, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()

        userNameValidityText = view.findViewById(R.id.text_username_validity)

        userNameEditText = view.findViewById(R.id.edit_username)
        userNameEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userNameValidity = isUserNameValid(userNameEditText.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            val userName = userNameEditText.text.toString()
            if (userName.trim() != ""){
                if (isUserNameValid(userName)){
                    //Valid, go next
                    val fragmentManager = requireActivity().supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val fragment = SignUpLogsFragment.newInstance(userName)
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                } else {
                    Toast.makeText(requireContext(), "Username not valid", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Username missing", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun updateUserNameList(){
        val userRef = database.getReference("User")
        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userNameList.clear()
                for (userSnapshot in snapshot.children){
                    val user = userSnapshot.getValue(User::class.java)
                    user?.let{
                        userNameList.add(it.username!!)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun isUserNameValid(userName: String): Boolean {
        updateUserNameList()
        val regex = Regex("^[\\w\\.\\s]+$")
        if (!userName.matches(regex)){
            userNameValidityText.text = "Can't have special characters"
            return false
        }
        if (userNameList.contains(userName)){
            userNameValidityText.text = "This username already exists"
            return false
        }
        userNameValidityText.text = ""
        return true
    }
}