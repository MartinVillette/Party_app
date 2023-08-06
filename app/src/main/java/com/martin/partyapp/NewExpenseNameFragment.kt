package com.martin.partyapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class NewExpenseNameFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_expense_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            val expenseName:String = view.findViewById<EditText>(R.id.edit_expense_name).text.toString()
            if (expenseName.trim() != ""){
                val fragmentManager = requireActivity().supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                val priceFragment = NewExpensePriceFragment.newInstance(expenseName)
                transaction.replace(R.id.fragment_container, priceFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(requireContext(), "Expense Name missing", Toast.LENGTH_SHORT).show()
            }
        }
    }
}