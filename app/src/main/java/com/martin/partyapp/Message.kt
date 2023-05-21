package com.martin.partyapp

import java.sql.Timestamp

class Message (
    var messageId: String? = null,
    var senderUser: User? = null,
    var content: String? = null,
    var date: Long? = null,
)