package com.martin.partyapp

import android.graphics.Color
import android.util.Log
import androidx.core.graphics.ColorUtils
import com.google.firebase.database.FirebaseDatabase
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Formatter
import kotlin.random.Random

class Event (
    var eventId: String? = null,
    var eventName: String? = null,
    var eventTimestamp: Long? = null,
    var eventAddress: Address? = null,
    var usersIds: ArrayList<String> = arrayListOf(),
    //var messages: MutableList<Message> = mutableListOf(),
    var messages: HashMap<String, Message> = hashMapOf(),
    var itemList: HashMap<String, Item> = hashMapOf(),
    var usersColor: HashMap<String, Int> = hashMapOf(),
    var expenses: HashMap<String, Expense> = hashMapOf(),
    var transactions: HashMap<String, Transaction> = hashMapOf(),
    var balanceMap: HashMap<String, Float> = hashMapOf(),
) {
    fun addUserToEvent(user: User){
        if (!usersIds.contains(user.userId)){
            usersIds.add(user.userId!!)

            //val color = Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256))
            if (!usersColor.containsKey(user.userId)){
                val color = if (usersIds.size == 1){
                    generateRandomPastelColor()
                } else {
                    pastelColor()
                }
                usersColor[user.userId!!] = color
            }
        }
    }

    private fun generateRandomPastelColor(): Int{
        val random = Random(System.currentTimeMillis())
        val hue = random.nextInt(360).toFloat()
        //val saturation = 0.5f
        //val luminosity = 0.7f
        val saturation = 0.97f
        val luminosity = 0.88f
        return ColorUtils.HSLToColor(floatArrayOf(hue, saturation, luminosity))
    }

    private fun pastelColor():Int{
        val existingHues = ArrayList<Float>()

        for ((_, userColor) in usersColor){
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(userColor, hsl)
            existingHues.add(hsl[0])
        }
        var hue: Float = Float.NaN
        if (existingHues.size > 1){
            existingHues.sort()
            var maxDistance = Float.MIN_VALUE
            var maxIndex = 0
            for (i in 0 until existingHues.size - 1){
                val distance = existingHues[i+1] - existingHues[i]
                if (distance > maxDistance){
                    maxDistance = distance
                    maxIndex = i
                }
            }
            val distance = 360 - existingHues.last() + existingHues.first()
            if (distance > maxDistance){
                maxDistance = distance
                maxIndex = existingHues.size - 1
            }

            hue = existingHues[maxIndex] + (maxDistance / 2)
        } else {
            hue = (180 + existingHues[0]) % 360
        }
        //val saturation = 0.5f
        //val luminosity = 0.7f
        val saturation = 0.97f
        val luminosity = 0.88f
        return ColorUtils.HSLToColor(floatArrayOf(hue, saturation, luminosity))
    }

    fun removeUserFromEvent(userId: String){
        if (usersIds.contains(userId)){
            var i = 0
            while (usersIds[i] != userId){
                i++
            }
            usersIds.removeAt(i)
        }
        if (usersColor.containsKey(userId)){
            usersColor.remove(userId)
        }
    }
    fun addItem(item:Item){
        itemList[item.itemId!!] = item
    }

    fun removeItem(item:Item){
        itemList.remove(item.itemId!!)
    }

    fun addExpense(expense:Expense){
        expenses[expense.expenseId!!] = expense
    }
    fun removeExpense(expense: Expense){
        expenses.remove(expense.expenseId!!)
    }

    fun makeBalance(database: FirebaseDatabase){
        transactions.clear()
        balanceMap.clear()

        for (userId in usersIds){
            balanceMap[userId] = 0f
        }

        for ((_,expense) in expenses) {
            val amountPerUser = expense.price / expense.usersConcerned.size
            balanceMap[expense.userWhoPaid!!.userId!!] = balanceMap.getValue(expense.userWhoPaid!!.userId!!) + expense.price
            for (userConcerned in expense.usersConcerned) {
                balanceMap[userConcerned.userId!!] = balanceMap.getValue(userConcerned.userId!!) - amountPerUser
            }
        }

        val eventRef = database.getReference("Event/$eventId")
        val updateData = HashMap<String, Any>()
        updateData["balanceMap"] = balanceMap
        updateData["transactions"] = transactions
        eventRef.updateChildren(updateData)

        val debtors = balanceMap.filterValues { it < 0 }
        val creditors = balanceMap.filterValues { it > 0 }

        for ((debtorUserId, debtorAmount) in debtors) {
            for ((creditorUserId, creditorAmount) in creditors) {
                val transactionAmount = minOf(-debtorAmount, creditorAmount)

                if (transactionAmount > 0) {
                    val transaction = Transaction()
                    transaction.userIdFrom = debtorUserId
                    transaction.userIdTo = creditorUserId

                    val bigDecimal = BigDecimal(transactionAmount.toString())
                    val roundedBigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN)
                    transaction.amount = roundedBigDecimal.toFloat()

                    val transactionRef = database.getReference("Event/$eventId/transactions").push()
                    transaction.transactionId = transactionRef.key ?: ""

                    transactionRef.setValue(transaction)
                    transactions[transaction.transactionId!!] = transaction

                    balanceMap[debtorUserId] = balanceMap.getValue(debtorUserId) + transactionAmount
                    balanceMap[creditorUserId] = balanceMap.getValue(creditorUserId) - transactionAmount

                    if (balanceMap[debtorUserId] == 0f) {
                        break
                    }
                }
            }
        }
    }
}