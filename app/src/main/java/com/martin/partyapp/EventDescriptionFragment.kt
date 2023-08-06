package com.martin.partyapp

import android.app.AlertDialog
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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
import com.google.firebase.database.ktx.getValue
import io.opencensus.metrics.export.Value
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventDescriptionFragment : Fragment() {

    private lateinit var authUser: User
    //private lateinit var event: Event
    private var eventTimestamp: Long ?= null
    private lateinit var eventAddress: Address
    private lateinit var eventId: String

    private lateinit var quitEventButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var membersButton: LinearLayout
    private lateinit var itemsButton: LinearLayout
    private lateinit var expensesButton: LinearLayout

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
        expensesButton = view.findViewById(R.id.button_expenses)
        expensesButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventExpensesFragment())
            transaction.commit()
        }

        quitEventButton = view.findViewById(R.id.button_quit_event)
        quitEventButton.setOnClickListener {
            showQuitEventPopup()
        }

        dateText = view.findViewById(R.id.text_event_date)
        hourText = view.findViewById(R.id.text_event_hour)
        addressText = view.findViewById(R.id.text_event_address)

        val eventDateRef = database.getReference("Event/$eventId/eventTimestamp")
        eventDateRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val timestamp = snapshot.getValue(Long::class.java)
                    timestamp?.let{
                        eventTimestamp = it
                        val date = Date(it)
                        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                        val hourFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val formattedDate = dateFormat.format(date)
                        val formattedHour = hourFormat.format(date)

                        dateText.text = formattedDate
                        hourText.text = formattedHour
                    }
                    val eventAddressRef = database.getReference("Event/$eventId/eventAddress")
                    eventAddressRef.addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val address = snapshot.getValue(Address::class.java)
                                address?.let {
                                    eventAddress = it
                                    addressText.text = it.addressName
                                    mapView = view.findViewById(R.id.map_view)

                                    mapView.onCreate(savedInstanceState)
                                    mapView.onResume()

                                    mapView.getMapAsync { map ->
                                        googleMap = map
                                        val markerOptions = MarkerOptions()
                                            .position(LatLng(eventAddress.latitude!!, eventAddress.longitude!!))

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
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
        /*
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

         */
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            authUser = context.authUser
            //event = context.event
        }
    }


    private fun showQuitEventPopup(){
        val dialogBuilder = AlertDialog.Builder(context)
        val popupView = LayoutInflater.from(context).inflate(R.layout.unfollow_popup, null)

        val userNameText = popupView.findViewById<TextView>(R.id.text_username)
        userNameText.text = "Are you sure you want to quit the event ?"

        dialogBuilder.setView(popupView)
        val alertDialog = dialogBuilder.create()

        val unfollowButton: Button = popupView.findViewById(R.id.button_unfollow)
        unfollowButton.text = "Quit"
        unfollowButton.setOnClickListener {
            quitEvent()
            alertDialog.dismiss()
        }

        val cancelButton: Button = popupView.findViewById(R.id.button_cancel)
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }
    private fun quitEvent(){
        // remove the Event from the User's event List
        val authUserRef = database.getReference("User/${auth.currentUser?.uid!!}")
        val updateData = HashMap<String, Any>()
        authUser.removeUserFromEvent(eventId)
        updateData["eventIds"] = authUser.eventIds
        authUserRef.updateChildren(updateData)

        // remove User from the Event
        val eventUsersIdsRef = database.getReference("Event/$eventId/usersIds")
        eventUsersIdsRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val usersIds = ArrayList<String>()
                for (postSnapshot in snapshot.children){
                    val userId = postSnapshot.getValue(String::class.java)
                    userId?.let{
                        if (it != auth.currentUser?.uid!!){
                            usersIds.add(it)
                        }
                    }
                }
                eventUsersIdsRef.setValue(usersIds)
                    .addOnSuccessListener {
                        val activity = requireActivity()
                        if (!activity.isFinishing) {
                            activity.finish()
                        }
                    }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun redirectGoogleMap(){
        val address = eventAddress.addressName
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        Log.e("Map", address)
        if (intent.resolveActivity(requireContext().packageManager) != null){
            Log.e("Map", "start")
            startActivity(intent)
        }
    }
}