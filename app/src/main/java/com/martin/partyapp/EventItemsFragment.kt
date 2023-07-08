package com.martin.partyapp

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EventItemsFragment: Fragment() {

    private lateinit var eventId: String
    private lateinit var event: Event
    private lateinit var authUser: User

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var addItemButton: LinearLayout

    private lateinit var itemList: ArrayList<Item>
    private lateinit var adapter: EventItemAdapter
    private lateinit var eventItemRecyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event_items, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        addItemButton = view.findViewById(R.id.button_add_item)
        addItemButton.setOnClickListener {
            showAddItemPopup()
        }

        itemList = ArrayList()
        adapter = EventItemAdapter(requireContext(), itemList, event)
        eventItemRecyclerView = view.findViewById(R.id.items_recycler_view)
        eventItemRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventItemRecyclerView.adapter = adapter

        for ((_,item) in event.itemList){
            itemList.add(item)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventDescriptionActivity){
            eventId = context.eventId
            event = context.event
            authUser = context.authUser
        }
    }

    private fun showAddItemPopup(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add new Item")

        val itemNameInput = EditText(requireContext())
        itemNameInput.hint = "Item Name"
        val itemQuantityInput = EditText(requireContext())
        itemQuantityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        itemQuantityInput.hint = "Item Quantity"


        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(itemNameInput)
        layout.addView(itemQuantityInput)
        builder.setView(layout)

        builder.setPositiveButton("Add") { _, _ ->
            val itemName = itemNameInput.text.toString()
            val itemQuantity = itemQuantityInput.text.toString().toIntOrNull()
            if (itemName.isNotEmpty() && itemQuantity != null){
                val newItem = Item()
                newItem.itemName = itemName
                newItem.itemQuantity = itemQuantity
                val newItemRef = database.getReference("Event/$eventId/itemList").push()
                newItem.itemId = newItemRef.key ?: ""
                newItemRef.setValue(newItem)
                    .addOnSuccessListener {
                        itemList.add(newItem)
                        adapter.notifyDataSetChanged()
                    }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
        itemNameInput.requestFocus()
    }
}