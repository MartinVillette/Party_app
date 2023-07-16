package com.martin.partyapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NewEventTimeFragment : Fragment() {
    private var eventName: String? = null
    private var eventDate: Long? = null

    companion object {
        private const val ARG_EVENT_NAME = "event_name"
        private const val ARG_EVENT_DATE = "event_date"

        fun newInstance(eventName: String, eventDate: Long): NewEventTimeFragment {
            val fragment = NewEventTimeFragment()
            val args = Bundle().apply {
                putString(ARG_EVENT_NAME, eventName)
                putLong(ARG_EVENT_DATE, eventDate)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventName = it.getString(ARG_EVENT_NAME)
            eventDate = it.getLong(ARG_EVENT_DATE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_event_time, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventNameText: TextView = view.findViewById(R.id.text_event_name)
        eventNameText.text = eventName

        val date = Date(eventDate!!)
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        val eventDateText: TextView = view.findViewById(R.id.text_event_date)
        eventDateText.text = formattedDate

        val timePickerEvent: TimePicker = view.findViewById(R.id.time_event)
        timePickerEvent.setIs24HourView(true)

        val timeCheckBox: CheckBox = view.findViewById(R.id.checkbox_time)
        timeCheckBox.setOnCheckedChangeListener {_, isChecked ->
            if (isChecked){
                timePickerEvent.visibility = View.INVISIBLE
            } else {
                timePickerEvent.visibility = View.VISIBLE
            }
        }

        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (timeCheckBox.isChecked){
                //No specific Hour
                calendar.time = date
            } else {
                //Specific hour
                val initCalendar: Calendar = Calendar.getInstance()
                initCalendar.time = date

                val hour = timePickerEvent.hour
                val minute = timePickerEvent.minute
                val calendar = Calendar.getInstance()
                calendar.set(initCalendar.get(Calendar.YEAR),initCalendar.get(Calendar.MONTH),initCalendar.get(Calendar.DAY_OF_MONTH),hour,minute)
            }
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            val addressFragment = NewEventAddressFragment.newInstance(eventName!!, calendar.timeInMillis)
            transaction.replace(R.id.fragment_container, addressFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }
    }
}