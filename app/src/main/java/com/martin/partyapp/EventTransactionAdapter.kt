package com.martin.partyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Mms.Sent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.opencensus.metrics.export.Value
import org.w3c.dom.Text

class EventTransactionAdapter (val context: Context, private val transactionList: ArrayList<Transaction>, private val usersColor: HashMap<String, Int>, private val eventId: String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVED = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_RECEIVED){
            val view = LayoutInflater.from(context).inflate(R.layout.balance_received_layout, parent, false)
            ReceivedViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.balance_sent_layout, parent, false)
            SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val transaction = transactionList[position]

        if (holder.itemViewType == ITEM_SENT) {
            //sent message
            val viewHolder = holder as SentViewHolder
            viewHolder.bind(transaction, usersColor)
            viewHolder.itemLayout.setOnClickListener {
                toggleBalance(transaction)
            }
        } else {
            //received message
            val viewHolder = holder as ReceivedViewHolder
            viewHolder.bind(transaction, usersColor)
            viewHolder.itemLayout.setOnClickListener {
                toggleBalance(transaction)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val transaction = transactionList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid == transaction.userIdFrom) {
            ITEM_SENT
        } else {
            ITEM_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    private fun toggleBalance(transaction:Transaction) {
        val updateData = HashMap<String, Any>()
        updateData["transactionMade"] = !transaction.transactionMade
        FirebaseDatabase.getInstance().getReference("Event/$eventId/transactions/${transaction.transactionId}").updateChildren(updateData)
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val userNameReceiverText: TextView = itemView.findViewById(R.id.text_username_receiver)
        private val amountText: TextView = itemView.findViewById(R.id.text_amount)
        private val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_balance)
        val itemLayout: RelativeLayout = itemView.findViewById(R.id.balance_item_layout)

        fun bind(transaction: Transaction, usersColor: HashMap<String, Int>){
            val userRef = FirebaseDatabase.getInstance().getReference("User/${transaction.userIdTo}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        snapshot.getValue(User::class.java)?.let{
                            userNameReceiverText.text = "@${it.username}"
                            amountText.text = "${transaction.amount}€"
                        }
                        if (transaction.transactionMade){
                            userNameReceiverText.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey))
                            amountText.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey))
                        } else {
                            usersColor[transaction.userIdTo]?.let{
                                userNameReceiverText.setTextColor(it)
                            }
                            amountText.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
            checkBox.isChecked = transaction.transactionMade
        }
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val userNameSenderText: TextView = itemView.findViewById(R.id.text_username_sender)
        private val amountText: TextView = itemView.findViewById(R.id.text_amount)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_balance)
        val itemLayout: RelativeLayout = itemView.findViewById(R.id.balance_item_layout)

        fun bind(transaction: Transaction, usersColor: HashMap<String, Int>){
            val userRef = FirebaseDatabase.getInstance().getReference("User/${transaction.userIdFrom}")
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        snapshot.getValue(User::class.java)?.let{
                            userNameSenderText.text = "@${it.username}"
                            amountText.text = "${transaction.amount}€"
                        }
                        if (transaction.transactionMade){
                            userNameSenderText.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey))
                            amountText.setTextColor(ContextCompat.getColor(itemView.context, R.color.grey))
                        } else {
                            usersColor[transaction.userIdFrom]?.let{
                                userNameSenderText.setTextColor(it)
                            }
                            amountText.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
            checkBox.isChecked = transaction.transactionMade
        }
    }
}