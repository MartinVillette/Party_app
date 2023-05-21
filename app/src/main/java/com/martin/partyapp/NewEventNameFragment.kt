package com.martin.partyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class NewEventNameFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_event_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            val eventName:String = view.findViewById<EditText>(R.id.edit_event_name).text.toString()
            if (eventName.trim() != ""){
                val fragmentManager = requireActivity().supportFragmentManager
                val transaction = fragmentManager.beginTransaction()
                val dateFragment = NewEventDateFragment.newInstance(eventName)
                transaction.replace(R.id.fragment_container, dateFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(requireContext(), "Event Name missing", Toast.LENGTH_SHORT).show()
            }
        }
    }
}