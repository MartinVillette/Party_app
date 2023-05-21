package com.martin.partyapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpLogsFragment : Fragment() {
    private var userName: String? = null
    private lateinit var userNameText: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var errorText: TextView
    private var emailValidity: Boolean = false
    private var passwordValidity: Boolean = false
    private var confirmPasswordValidity: Boolean = false
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    companion object {
        private const val ARG_USER_NAME = "user_name"

        fun newInstance(userName: String): SignUpLogsFragment {
            val fragment = SignUpLogsFragment()
            val args = Bundle().apply {
                putString(ARG_USER_NAME, userName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userName = it.getString(ARG_USER_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_signup_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        userNameText = view.findViewById(R.id.text_username)
        userNameText.text = "Hi, $userName"

        emailEditText = view.findViewById(R.id.edit_email)
        passwordEditText = view.findViewById(R.id.edit_password)
        confirmPasswordEditText = view.findViewById(R.id.edit_confirm_password)
        errorText = view.findViewById(R.id.text_error)

        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                emailValidity = isEmailValid()
                if (!emailValidity){
                    errorText.text = "Email Error"
                }
            }
        }
        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                passwordValidity = isPasswordValid()
                if (!passwordValidity){
                    errorText.text = "Password Error"
                }
            }
        }
        confirmPasswordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                confirmPasswordValidity = isConfirmPasswordValid()
                if (!confirmPasswordValidity){
                    errorText.text = "Passwords don't match"
                }
            }
        }

        val fragmentManager = requireActivity().supportFragmentManager
        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            emailValidity = isEmailValid()
            passwordValidity = isPasswordValid()
            confirmPasswordValidity = isConfirmPasswordValid()
            if (emailValidity && passwordValidity && confirmPasswordValidity) {
                //ALl matches
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                signup(userName!!, email, password)
            } else {
                if (!emailValidity){
                    Toast.makeText(requireContext(), "Email Error", Toast.LENGTH_SHORT).show()
                } else if (!passwordValidity){
                    Toast.makeText(requireContext(), "Password Error", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
                }
            }
        }
        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            fragmentManager.popBackStack()
        }
    }

    private fun isEmailValid(): Boolean{
        val email = emailEditText.text.toString()
        return email.contains('@')
    }
    private fun isPasswordValid(): Boolean{
        val password = passwordEditText.text.toString()
        return password.length > 3
    }
    private fun isConfirmPasswordValid(): Boolean{
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        return password == confirmPassword
    }

    private fun signup(username:String, email:String, password:String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    addUserToData(username, email, auth.currentUser?.uid!!)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToData(username: String, email: String, userId: String){
        val userRef = database.getReference("User/$userId")
        userRef.setValue(User(userId,username,email))
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val fragmentManager = requireActivity().supportFragmentManager
                    val transaction = fragmentManager.beginTransaction()
                    val fragment = SignUpProfilePictureFragment.newInstance(userName!!)
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                } else {
                    Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
    }
}