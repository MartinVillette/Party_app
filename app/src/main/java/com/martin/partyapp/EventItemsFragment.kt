package com.martin.partyapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EventItemsFragment: Fragment() {
    private lateinit var eventId: String
    private lateinit var event: Event
    private lateinit var authUser: User

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var addItemButton: LinearLayout

    private lateinit var membersButton: LinearLayout
    private lateinit var descriptionButton: LinearLayout
    private lateinit var expensesButton: LinearLayout

    private var itemList: ArrayList<Item> = ArrayList()
    private lateinit var adapter: EventItemAdapter
    private lateinit var eventItemRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_items, container, false)
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
        descriptionButton = view.findViewById(R.id.button_description)
        descriptionButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventDescriptionFragment())
            transaction.commit()
        }
        expensesButton = view.findViewById(R.id.button_expenses)
        expensesButton.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, EventExpensesFragment())
            transaction.commit()
        }

        addItemButton = view.findViewById(R.id.button_add_item)
        addItemButton.setOnClickListener {
            showAddItemPopup()
        }

        eventItemRecyclerView = view.findViewById(R.id.items_recycler_view)
        eventItemRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val eventRef = database.getReference("Event/$eventId")
        eventRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    event = snapshot.getValue(Event::class.java)!!
                    itemList.clear()
                    for ((_,item) in event.itemList){
                        itemList.add(item)
                    }
                    adapter = EventItemAdapter(requireContext(), itemList, event, authUser) { item ->
                        showItemPopUp(item)
                    }
                    eventItemRecyclerView.adapter = adapter
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            authUser = context.authUser
        }
    }

    private fun showItemPopUp(item: Item){
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.event_item_popup_layout, null)

        val itemNameText = popupView.findViewById<TextView>(R.id.text_item_name)
        itemNameText.text = item.itemName!!

        val userQuantityList = ArrayList<ItemUserQuantity>()
        for (itemUserQuantity in item.itemUserQuantityList){
            if (itemUserQuantity.quantity > 0){
                userQuantityList.add(itemUserQuantity)
            }
        }
        val itemInfoAdapter = EventItemInfoAdapter(requireContext(), userQuantityList)

        val eventItemInfoRecyclerView = popupView.findViewById<RecyclerView>(R.id.item_info_recycler_view)
        eventItemInfoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventItemInfoRecyclerView.adapter = itemInfoAdapter

        bottomSheetDialog.setContentView(popupView)
        bottomSheetDialog.window?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.setBackgroundResource(R.drawable.custom_bottom_sheet_dialog_background)
        bottomSheetDialog.show()
    }

    private fun showAddItemPopup(){
        val dialogBuilder = AlertDialog.Builder(context)
        val popupView = LayoutInflater.from(context).inflate(R.layout.add_item_popup, null)

        val itemNameInput: EditText = popupView.findViewById(R.id.edit_item_name)
        val itemQuantityInput: EditText = popupView.findViewById(R.id.edit_item_quantity)
        val addItemButton: Button = popupView.findViewById(R.id.button_add_item)
        val cancelButton: Button = popupView.findViewById(R.id.button_cancel)

        dialogBuilder.setView(popupView)
        val alertDialog = dialogBuilder.create()

        addItemButton.setOnClickListener {
            val itemName = itemNameInput.text.toString()
            if (itemName.isNotEmpty()) {
                val newItem = Item()
                newItem.itemName = itemName
                newItem.itemQuantity = itemQuantityInput.text.toString().toIntOrNull() ?: 1
                val newItemRef = database.getReference("Event/$eventId/itemList").push()
                newItem.itemId = newItemRef.key ?: ""
                newItemRef.setValue(newItem)
                    .addOnSuccessListener {
                        alertDialog.dismiss()
                    }
            } else {
                Toast.makeText(requireContext(), "Enter an item name", Toast.LENGTH_SHORT)
            }
        }

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }
}