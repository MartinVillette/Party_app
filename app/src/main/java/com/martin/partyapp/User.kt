package com.martin.partyapp

import android.util.Log

class User(
    var userId: String? = null,
    var username: String? = null,
    var email: String? = null,
    val userFCMToken: String? = null,
    var profilePictureUrl: String? = null,
    var followers: MutableList<User> = mutableListOf(),
    var following: MutableList<User> = mutableListOf(),
    var requests: MutableList<User> = mutableListOf(),
    var eventIds: ArrayList<String> = arrayListOf(),
    //var events: MutableList<Event> = mutableListOf(),
) {

    fun addFollower(user:User){
        val followersIds: List<String> = followers.map { it.userId!! }
        if (!followersIds.contains(user.userId)){
            followers.add(user)
        }
    }

    fun addFollowing(user:User){
        val followingIds: List<String> = following.map { it.userId!! }
        if (!followingIds.contains(user.userId)){
            following.add(user)
        }
    }

    fun addFollowingRequest(user: User){
        val requestsIds: List<String> = requests.map { it.userId!! }
        if (!requestsIds.contains(user.userId)){
            requests.add(user)
        }
    }

    fun removeFollower(user: User){
        val followersIds: List<String> = followers.map { it.userId!! }
        if (followersIds.contains(user.userId)){
            var i = 0
            while (followers[i].userId != user.userId){
                i++
            }
            followers.removeAt(i)
        }
    }

    fun removeFollowing(user: User){
        val followingIds: List<String> = following.map { it.userId!! }
        if (followingIds.contains(user.userId)){
            var i = 0
            while (following[i].userId != user.userId){
                i++
            }
            following.removeAt(i)
        }
    }

    fun acceptFollowingRequest(user: User){
        removeRequest(user)
        val followersIds: List<String> = followers.map { it.userId!! }
        if (!followersIds.contains(user.userId)){
            followers.add(user)
        }
    }

    fun declineFollowingRequest(user: User){
        removeRequest(user)
    }

    private fun removeRequest(user: User){
        val requestIds: List<String> = requests.map { it.userId!! }
        if (requestIds.contains(user.userId)){
            var i = 0
            while (requests[i].userId != user.userId){
                i++
            }
            requests.removeAt(i)
        }
    }

    fun addEvent(event: Event){
        if (!eventIds.contains(event.eventId)){
            eventIds.add(event.eventId!!)
        }
    }

    fun removeUserFromEvent(eventId: String){
        if (eventIds.contains(eventId)){
            var i = 0
            while (eventIds[i] != eventId){
                i++
            }
            eventIds.removeAt(i)
        }
    }
}