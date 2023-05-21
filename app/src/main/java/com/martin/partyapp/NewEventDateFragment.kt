package com.martin.partyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.Calendar

class NewEventDateFragment : Fragment() {
    private var eventName: String? = null

    companion object {
        private const val ARG_EVENT_NAME = "event_name"

        fun newInstance(eventName: String): NewEventDateFragment {
            val fragment = NewEventDateFragment()
            val args = Bundle().apply {
                putString(ARG_EVENT_NAME, eventName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventName = it.getString(ARG_EVENT_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_event_date, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventNameText: TextView = view.findViewById(R.id.text_event_name)
        eventNameText.text = eventName

        val fragmentManager = requireActivity().supportFragmentManager

        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            val datePickerEvent: DatePicker = view.findViewById(R.id.date_event)

            val year = datePickerEvent.year
            val month = datePickerEvent.month
            val day = datePickerEvent.dayOfMonth
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val eventTimestamp = calendar.timeInMillis

            val transaction = fragmentManager.beginTransaction()
            val timeFragment = NewEventTimeFragment.newInstance(eventName!!, eventTimestamp)
            transaction.replace(R.id.fragment_container, timeFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            fragmentManager.popBackStack()
        }
    }

}