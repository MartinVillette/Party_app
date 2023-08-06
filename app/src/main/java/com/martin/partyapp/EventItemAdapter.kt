package com.martin.partyapp

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random

class EventItemAdapter(private val context: Context, private val itemList: ArrayList<Item>, private val event: Event, private val currentUser: User, private val onItemClick: (Item) -> Unit) :
    RecyclerView.Adapter<EventItemAdapter.EventItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_description_item_layout, parent, false)
        return EventItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemView.setOnLongClickListener {
            showEditItemPopup(item)
            true
        }
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
        holder.currentUser = currentUser
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun showEditItemPopup(item: Item){
        val database:FirebaseDatabase = FirebaseDatabase.getInstance()

        val dialogBuilder = AlertDialog.Builder(context)
        val popupView = LayoutInflater.from(context).inflate(R.layout.edit_item_popup, null)

        val itemNameInput: EditText = popupView.findViewById(R.id.edit_item_name)
        itemNameInput.setText(item.itemName)
        val itemQuantityInput: EditText = popupView.findViewById(R.id.edit_item_quantity)
        itemQuantityInput.setText(item.itemQuantity.toString())
        val editItemButton: Button = popupView.findViewById(R.id.button_edit_item)
        val cancelButton: Button = popupView.findViewById(R.id.button_cancel)

        dialogBuilder.setView(popupView)
        val alertDialog = dialogBuilder.create()

        editItemButton.setOnClickListener {
            val itemName = itemNameInput.text.toString()
            if (itemName.isNotEmpty()){
                item.itemName = itemName
                item.itemQuantity = itemQuantityInput.text.toString().toIntOrNull() ?: 1
                val itemRef = database.getReference("Event/${event.eventId}/itemList/${item.itemId}")
                itemRef.setValue(item)
                    .addOnSuccessListener {
                        notifyDataSetChanged()
                        alertDialog.dismiss()
                    }
            } else {
                Toast.makeText(context, "Enter an item name", Toast.LENGTH_SHORT)
            }
        }
        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }
    /*
    private fun showEditItemPopUp(item: Item){
        val database:FirebaseDatabase = FirebaseDatabase.getInstance()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Edit the item")

        val itemNameInput = EditText(context)
        itemNameInput.setText(item.itemName)
        val itemQuantityInput = EditText(context)
        itemQuantityInput.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        itemQuantityInput.setText(item.itemQuantity.toString())


        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.addView(itemNameInput)
        layout.addView(itemQuantityInput)
        builder.setView(layout)

        builder.setPositiveButton("Edit") { _, _ ->
            val itemName = itemNameInput.text.toString()
            val itemQuantity = itemQuantityInput.text.toString().toIntOrNull()
            if (itemName.isNotEmpty() && itemQuantity != null){
                item.itemName = itemName
                item.itemQuantity = itemQuantity
                val itemRef = database.getReference("Event/${event.eventId}/itemList/${item.itemId}")
                itemRef.setValue(item)
                    .addOnSuccessListener {
                        notifyDataSetChanged()
                    }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

     */

    inner class EventItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.item_name)
        private val buttonRemove: ImageButton = itemView.findViewById(R.id.button_remove_item)
        private val buttonLayoutOtherUsers: LinearLayout = itemView.findViewById(R.id.layout_item_other_users)
        private val buttonLayoutAuthUser: LinearLayout = itemView.findViewById(R.id.layout_item_auth_user)
        private val buttonLayoutMissing: LinearLayout = itemView.findViewById(R.id.layout_item_missing)

        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val database:FirebaseDatabase = FirebaseDatabase.getInstance()
        var currentUser: User? = null

        fun bind(item: Item) {
            itemName.text = item.itemName

            buttonRemove.setOnClickListener {
                val itemRef = database.getReference("Event/${event.eventId}/itemList/${item.itemId}")
                itemRef.removeValue()
                    .addOnSuccessListener {
                        event.removeItem(item)
                        itemList.remove(item)
                        notifyDataSetChanged()
                    }
            }

            buttonLayoutOtherUsers.removeAllViews()
            buttonLayoutAuthUser.removeAllViews()
            buttonLayoutMissing.removeAllViews()

           addButtons(item)
        }

        private fun addButtons(item:Item){
            var quantityLeft:Int = item.itemQuantity!!
            var authUserQuantity: ItemUserQuantity? = getItemUserQuantityByUser(item.itemUserQuantityList, currentUser!!)
            for (userQuantity in item.itemUserQuantityList){
                val userIsAuthUser = (userQuantity.user!!.userId == auth.currentUser?.uid!!)
                for (i in 1 .. userQuantity.quantity){
                    val button = Button(itemView.context)
                    var layoutParams = LinearLayout.LayoutParams(40,40)
                    layoutParams.leftMargin = 5
                    layoutParams.rightMargin = 5
                    button.layoutParams = layoutParams
                    button.setBackgroundResource(R.drawable.circle_background)
                    button.backgroundTintList = ColorStateList.valueOf(event.usersColor[userQuantity.user!!.userId]!!)

                    if (userIsAuthUser && authUserQuantity != null){
                        if (i == authUserQuantity!!.quantity){
                            button.setOnClickListener {
                                authUserQuantity.quantity = 0
                                updateItem(item)
                            }
                        } else {
                            button.setOnClickListener {
                                authUserQuantity.quantity = i
                                updateItem(item)
                            }
                        }
                        buttonLayoutAuthUser.addView(button)
                    } else {
                        buttonLayoutOtherUsers.addView(button)
                    }
                }
                quantityLeft -= userQuantity.quantity
            }

            for (i in 1..quantityLeft){
                val button = Button(itemView.context)
                var layoutParams = LinearLayout.LayoutParams(40,40)
                layoutParams.leftMargin = 5
                layoutParams.rightMargin = 5
                button.layoutParams = layoutParams
                button.setBackgroundResource(R.drawable.circle_background_edge_white)

                button.setOnClickListener {

                    if (authUserQuantity != null){
                        authUserQuantity.quantity = authUserQuantity.quantity + i
                    } else {
                        val itemUserQuantity = ItemUserQuantity()
                        itemUserQuantity.user = currentUser
                        itemUserQuantity.quantity = i
                        item.itemUserQuantityList.add(itemUserQuantity)
                    }


                    updateItem(item)
                }
                buttonLayoutMissing.addView(button)

            }
        }
        private fun getItemUserQuantityByUser(itemUserQuantityList: ArrayList<ItemUserQuantity>, user:User): ItemUserQuantity? {
            for (itemUserQuantity in itemUserQuantityList){
                if (itemUserQuantity.user!!.userId == user.userId){
                    return itemUserQuantity
                }
            }
            return null
        }

        private fun updateItem(item:Item){
            val itemRef = database.getReference("Event/${event.eventId}/itemList/${item.itemId}")
            val updateData = HashMap<String, Any>()
            updateData["itemUserQuantityList"] = item.itemUserQuantityList
            itemRef.updateChildren(updateData)
                .addOnSuccessListener {
                    notifyDataSetChanged()
                }
        }
    }
}