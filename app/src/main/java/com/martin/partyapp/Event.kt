package com.martin.partyapp

import java.sql.Timestamp


class Event (
    var eventId: String? = null,
    var eventName: String? = null,
    var eventTimestamp: Long? = null,
    var users: MutableList<User> = mutableListOf(),
    var messages: MutableList<Message> = mutableListOf(),
) {
    fun addUserToEvent(user: User){
        val usersIds: List<String> = users.map { it.userId!! }
        if (!usersIds.contains(user.userId)){
            users.add(user)
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