package com.martin.partyapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignUpProfilePictureFragment : Fragment() {
    private var userName: String? = null
    private lateinit var userNameText: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var profilePictureLayout: RelativeLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var pickImageContract: ActivityResultLauncher<String>

    companion object {
        private const val ARG_USER_NAME = "user_name"

        fun newInstance(userName: String): SignUpProfilePictureFragment {
            val fragment = SignUpProfilePictureFragment()
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
        return inflater.inflate(R.layout.fragment_signup_profile_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        userNameText = view.findViewById(R.id.text_username)
        userNameText.text = userName

        imageProfile = view.findViewById(R.id.image_profile)

        profilePictureLayout = view.findViewById(R.id.layout_profile_picture)
        profilePictureLayout.setOnClickListener {
            //val galleryIntent = Intent(Intent.ACTION_PICK)
            //galleryIntent.type = "image/*"
            //imagePickerActivityResult.launch(galleryIntent)
            openGallery()
        }

        pickImageContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri:Uri? ->
            if (uri != null) {
                val imageUri: Uri = uri
                val userId = auth.currentUser?.uid!!
                val imageRef = storage.getReference("Image/profilePictures/${userId}.jpg")
                val uploadTask = imageRef.putFile(imageUri!!)

                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val profilePictureUrl = uri.toString()
                        val userRef = database.getReference("User/$userId")
                        userRef.child("profilePictureUrl").setValue(profilePictureUrl)

                        Glide.with(requireActivity())
                            .load(uri)
                            .circleCrop()
                            .into(imageProfile)
                    }
                }.addOnFailureListener {}
            }
        }

        val finishButton: Button = view.findViewById(R.id.button_finish)
        finishButton.setOnClickListener {
            //create
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openGallery(){
        pickImageContract.launch("image/*")
    }

    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult( ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null){
                    val userId = auth.currentUser?.uid!!
                    val imageRef = storage.getReference("Image/profilePictures/${userId}.jpg")
                    val uploadTask = imageRef.putFile(imageUri!!)

                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val profilePictureUrl = uri.toString()
                            val userRef = database.getReference("User/$userId")
                            userRef.child("profilePictureUrl").setValue(profilePictureUrl)

                            Glide.with(requireActivity())
                                .load(it)
                                .circleCrop()
                                .into(imageProfile)
                        }
                    }.addOnFailureListener {

                    }
                }
            }
        }
}