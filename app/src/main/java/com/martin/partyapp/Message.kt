package com.martin.partyapp

class Message (
    var messageId: String? = null,
    var senderUser: User? = null,
    var content: String? = null,
    var date: Long? = null,
    var viewers: ArrayList<String> = arrayListOf(),
)