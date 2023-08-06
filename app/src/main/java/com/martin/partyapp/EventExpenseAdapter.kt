package com.martin.partyapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventExpenseAdapter (private val context: Context, private val usersColor: HashMap<String, Int>, private val expenses: List<Expense>, private val eventId: String) :
    RecyclerView.Adapter<EventExpenseAdapter.EventExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventExpenseViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.event_expense_layout, parent, false)
        return EventExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventExpenseAdapter.EventExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, EventExpenseActivity::class.java)
            intent.putExtra("eventId", eventId)
            intent.putExtra("expenseId", expense.expenseId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    inner class EventExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var expenseNameText: TextView
        private lateinit var expensePriceText: TextView
        private lateinit var userWhoPaidText: TextView

        @SuppressLint("SetTextI18n")
        fun bind(expense: Expense){
            expenseNameText = itemView.findViewById(R.id.text_expense_name)
            expensePriceText = itemView.findViewById(R.id.text_price)
            userWhoPaidText = itemView.findViewById(R.id.text_user_who_paid)

            expenseNameText.text = expense.expenseName
            expensePriceText.text = "${expense.price}€"
            userWhoPaidText.text = "@${expense.userWhoPaid!!.username}"

            val color = usersColor[expense.userWhoPaid!!.userId]!!
            userWhoPaidText.setTextColor(color)
        }
    }
}