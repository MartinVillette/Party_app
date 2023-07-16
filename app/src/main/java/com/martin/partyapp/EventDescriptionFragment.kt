package com.martin.partyapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDescriptionFragment : Fragment() {

    private lateinit var authUser: User
    private lateinit var event: Event
    private lateinit var eventId: String

    private lateinit var quitEventButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var membersButton: LinearLayout
    private lateinit var itemsButton: LinearLayout

    private lateinit var mapLayout: LinearLayout

    private lateinit var dateText: TextView
    private lateinit var hourText: TextView
    private lateinit var addressText: TextView
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_description, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        membersButton = view.findViewById(R.id.button_members)
        membersButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventMembersFragment())
            transaction.commit()
        }
        itemsButton = view.findViewById(R.id.button_items)
        itemsButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventItemsFragment())
            transaction.commit()
        }

        quitEventButton = view.findViewById(R.id.button_quit_event)
        quitEventButton.setOnClickListener {
            quitEvent()
        }

        dateText = view.findViewById(R.id.text_event_date)
        hourText = view.findViewById(R.id.text_event_hour)
        addressText = view.findViewById(R.id.text_event_address)

        val date = Date(event.eventTimestamp!!)
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val formattedHour = hourFormat.format(date)

        dateText.text = formattedDate
        hourText.text = formattedHour
        addressText.text = event.eventAddress!!.addressName

        mapView = view.findViewById(R.id.map_view)

        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        mapView.getMapAsync { map ->
            googleMap = map
            val markerOptions = MarkerOptions()
                .position(LatLng(event.eventAddress!!.latitude!!, event.eventAddress!!.longitude!!))

            googleMap?.clear()
            googleMap?.addMarker(markerOptions)

            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(markerOptions.position, 15f)
            googleMap?.moveCamera(cameraUpdate)
        }

        mapLayout = view.findViewById(R.id.layout_map)
        mapLayout.setOnClickListener {
            redirectGoogleMap()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            event = context.event
            authUser = context.authUser
        }
    }

    private fun quitEvent(){
        // remove the Event from the User's event List
        val authUserRef = database.getReference("User/${auth.currentUser?.uid!!}")
        authUser.removeUserFromEvent(eventId)
        authUserRef.setValue(authUser)

        // remove User from the Event
        val eventRef = database.getReference("Event/$eventId")
        event.removeUserFromEvent(auth.currentUser?.uid!!)
        eventRef.setValue(event)

        val intent = Intent(requireContext(), EventListActivity::class.java)
        startActivity(intent)
    }

    private fun redirectGoogleMap(){
        val address = event.eventAddress!!.addressName
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        Log.e("Map", address)
        if (intent.resolveActivity(requireContext().packageManager) != null){
            Log.e("Map", "start")
            startActivity(intent)
        }
    }
}