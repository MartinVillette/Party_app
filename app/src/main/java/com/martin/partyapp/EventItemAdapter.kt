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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlin.random.Random

class EventItemAdapter(private val context: Context, private val itemList: ArrayList<Item>, private val event: Event) :
    RecyclerView.Adapter<EventItemAdapter.EventItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_description_item_layout, parent, false)
        return EventItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemView.setOnLongClickListener {
            showEditItemPopUp(item)
            true
        }
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

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

    inner class EventItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.item_name)
        private val buttonRemove: ImageButton = itemView.findViewById(R.id.button_remove_item)
        private val buttonLayoutOtherUsers: LinearLayout = itemView.findViewById(R.id.layout_item_other_users)
        private val buttonLayoutAuthUser: LinearLayout = itemView.findViewById(R.id.layout_item_auth_user)
        private val buttonLayoutMissing: LinearLayout = itemView.findViewById(R.id.layout_item_missing)

        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val currentUserId:String = auth.currentUser?.uid!!
        private val database:FirebaseDatabase = FirebaseDatabase.getInstance()

        fun bind(item: Item) {
            itemName.text = item.itemName

            buttonRemove.setOnClickListener {
                val itemRef = database.getReference("Event/${event.eventId}/itemList/${item.itemId}")
                itemRef.removeValue()
                    .addOnSuccessListener {
                        itemList.remove(item)
                        notifyDataSetChanged()
                    }
            }

            buttonLayoutOtherUsers.removeAllViews()
            buttonLayoutAuthUser.removeAllViews()
            buttonLayoutMissing.removeAllViews()

            var quantityLeft:Int = item.itemQuantity!!

            for ((userId,quantity) in item.itemUserMap!!){
                val userIsAuthUser = (userId == auth.currentUser?.uid!!)
                for (i in 1..quantity){
                    val button = Button(itemView.context)
                    var layoutParams = LinearLayout.LayoutParams(35,35)
                    layoutParams.leftMargin = 5
                    layoutParams.rightMargin = 5
                    button.layoutParams = layoutParams
                    button.setBackgroundResource(R.drawable.circle_background)
                    button.backgroundTintList = ColorStateList.valueOf(event.usersColor[userId]!!)

                    if (userIsAuthUser){
                        if (i == item.itemUserMap[currentUserId]){
                            button.setOnClickListener {
                                item.itemUserMap[currentUserId] = 0
                                updateItem(item)
                            }
                        } else {
                            button.setOnClickListener {
                                item.itemUserMap[currentUserId] = i
                                updateItem(item)
                            }
                        }
                        buttonLayoutAuthUser.addView(button)
                    } else {
                        buttonLayoutOtherUsers.addView(button)
                    }
                }
                quantityLeft -= quantity
            }

            for (i in 1..quantityLeft){
                val button = Button(itemView.context)
                val color = Color.rgb(255,255,255)
                var layoutParams = LinearLayout.LayoutParams(35,35)
                layoutParams.leftMargin = 5
                layoutParams.rightMargin = 5
                button.layoutParams = layoutParams
                button.setBackgroundResource(R.drawable.circle_background_edge_white)

                button.setOnClickListener {
                    if (item.itemUserMap.containsKey(currentUserId)){
                        item.itemUserMap[currentUserId] = item.itemUserMap[currentUserId]!! + i
                    } else {
                        item.itemUserMap[currentUserId] = i
                    }
                    updateItem(item)
                }
                buttonLayoutMissing.addView(button)
            }
        }

        private fun updateItem(item:Item){
            val itemRef = database.getReference("Event/${event.eventId}/itemList/${item.itemId}")
            itemRef.setValue(item)
                .addOnSuccessListener {
                    notifyDataSetChanged()
                }
        }
    }
}