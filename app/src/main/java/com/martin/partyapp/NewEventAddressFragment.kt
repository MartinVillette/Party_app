package com.martin.partyapp

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewEventAddressFragment : Fragment() {
    private var eventName: String? = null
    private var eventTimestamp: Long? = null
    private var eventAddress: Address = Address()

    private lateinit var addressEditText: EditText
    private lateinit var selectButton: Button
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap?=null

    companion object {
        private const val ARG_EVENT_NAME = "event_name"
        private const val ARG_EVENT_TIMESTAMP = "event_timestamp"

        fun newInstance(eventName: String, eventTimestamp: Long): NewEventAddressFragment {
            val fragment = NewEventAddressFragment()
            val args = Bundle().apply {
                putString(ARG_EVENT_NAME, eventName)
                putLong(ARG_EVENT_TIMESTAMP, eventTimestamp)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventName = it.getString(ARG_EVENT_NAME)
            eventTimestamp = it.getLong(ARG_EVENT_TIMESTAMP)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_new_event_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventNameText: TextView = view.findViewById(R.id.text_event_name)
        eventNameText.text = eventName

        val date = Date(eventTimestamp!!)
        val dateFormat = SimpleDateFormat("dd/MM/yy - HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val eventDateText: TextView = view.findViewById(R.id.text_event_date)
        eventDateText.text = formattedDate

        val fragmentManager = requireActivity().supportFragmentManager

        val nextButton: Button = view.findViewById(R.id.button_next)
        nextButton.setOnClickListener {
            if (eventAddress.addressName.isNotEmpty()){
                val transaction = fragmentManager.beginTransaction()
                val addressJson = Gson().toJson(eventAddress)
                val membersFragment = NewEventMembersFragment.newInstance(eventName!!, eventTimestamp!!, addressJson!!)
                transaction.replace(R.id.fragment_container, membersFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            } else {
                Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show()
            }

        }

        val previousButton: Button = view.findViewById(R.id.button_previous)
        previousButton.setOnClickListener {
            fragmentManager.popBackStack()
        }

        addressEditText = view.findViewById(R.id.address_edit_text)
        selectButton = view.findViewById(R.id.button_select_address)
        mapView = view.findViewById(R.id.map_view)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        mapView.getMapAsync { map ->
            googleMap = map

            selectButton.setOnClickListener {
                val address = addressEditText.text.toString()
                verifyAddress(address)
            }
        }
    }
    private fun verifyAddress(address:String):Boolean {
        if (address.isNotEmpty()){
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)!!
            if (results.isNotEmpty()){
                val location = results[0]

                eventAddress.addressName = location.getAddressLine(0)
                eventAddress.longitude = location.longitude
                eventAddress.latitude = location.latitude

                val markerOptions = MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(address)

                googleMap?.clear()
                googleMap?.addMarker(markerOptions)

                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(markerOptions.position, 15f)
                googleMap?.moveCamera(cameraUpdate)
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}