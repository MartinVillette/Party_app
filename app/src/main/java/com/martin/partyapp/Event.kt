package com.martin.partyapp

import android.graphics.Color
import kotlin.random.Random

class Event (
    var eventId: String? = null,
    var eventName: String? = null,
    var eventTimestamp: Long? = null,
    var users: MutableList<User> = mutableListOf(),
    var messages: MutableList<Message> = mutableListOf(),
    //var itemList: MutableList<Item> = mutableListOf(),
    var itemList: HashMap<String, Item> = hashMapOf(),
    var usersColor: HashMap<String, Int> = hashMapOf(),
) {
    fun addUserToEvent(user: User){
        val usersIds: List<String> = users.map { it.userId!! }

        if (!usersIds.contains(user.userId)){
            users.add(user)

            val random = Random(System.currentTimeMillis())
            val hue = random.nextInt(360).toFloat()
            val saturation = random.nextInt(40,60).toFloat() / 100f
            val value = random.nextInt(75,90).toFloat() / 100f

            val color = Color.HSVToColor(floatArrayOf(hue, saturation, value))

            //val color = Color.rgb(random.nextInt(256),random.nextInt(256),random.nextInt(256))
            usersColor[user.userId!!] = color
        }
    }

    fun removeUserFromEvent(userId: String){
        val usersIds: List<String> = users.map { it.userId!! }
        if (usersIds.contains(userId)){
            var i = 0
            while (users[i].userId != userId){
                i++
            }
            users.removeAt(i)
        }
    }
}