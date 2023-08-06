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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage

class NewExpensePictureFragment : Fragment() {
    private var expenseId: String? = null
    private lateinit var event: Event

    private lateinit var imageView: ImageView
    private lateinit var pictureLayout: RelativeLayout
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var pickImageContract: ActivityResultLauncher<String>

    companion object {
        private const val ARG_EXPENSE_ID = "expense_id"

        fun newInstance(expenseId: String): NewExpensePictureFragment {
            val fragment = NewExpensePictureFragment()
            val args = Bundle().apply {
                putString(ARG_EXPENSE_ID, expenseId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            expenseId = it.getString(NewExpensePictureFragment.ARG_EXPENSE_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_expense_picture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()


        /*
        val expenseRef = database.getReference("Event/$eventId/expenses/$expenseId")
        expenseRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    snapshot.getValue(Expense::class.java)?.let{
                        expense = it

                        val expenseNameText: TextView = view.findViewById(R.id.text_expense_name)
                        expenseNameText.text = expense.expenseName

                        val expensePriceText: TextView = view.findViewById(R.id.text_expense_price)
                        expensePriceText.text = expense.price.toString() + "€"

                        val userWhoPaidText: TextView = view.findViewById(R.id.text_user_who_paid)
                        val userRef = database.getReference("User/${expense.userWhoPaid}")
                        userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    val user = snapshot.getValue(User::class.java)
                                    user?.let {
                                        userWhoPaidText.text = "@" + it.username
                                        //userWhoPaidText.setTextColor(event.usersColor[expense.userWhoPaid!!.userId]!!)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

         */

        pictureLayout = view.findViewById(R.id.layout_picture)
        pictureLayout.setOnClickListener {
            openGallery()
        }

        pickImageContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri:Uri? ->
            if (uri != null) {
                val imageUri: Uri = uri
                val imageRef = storage.getReference("Image/expensePictures/$expenseId.jpg")
                val uploadTask = imageRef.putFile(imageUri!!)

                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val pictureUrl = uri.toString()
                        val expenseRef = database.getReference("Event/${event.eventId!!}/expenses/$expenseId")
                        expenseRef.child("pictureUrl").setValue(pictureUrl)

                        imageView = view.findViewById(R.id.image)
                        Glide.with(requireActivity())
                            .load(uri)
                            .into(imageView)
                    }
                }.addOnFailureListener {}
            }
        }

        val finishButton: Button = view.findViewById(R.id.button_finish)
        finishButton.setOnClickListener {
            //create
            requireActivity().finish()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NewExpenseActivity){
            event = context.event
        }
    }

    private fun openGallery(){
        pickImageContract.launch("image/*")
    }

}