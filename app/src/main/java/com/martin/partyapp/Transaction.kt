package com.martin.partyapp

class Transaction (
    var transactionId: String ?= null,
    var userIdFrom: String ?= null,
    var userIdTo: String ?= null,
    var transactionMade: Boolean = false,
    var amount: Float ?= 0f,
)