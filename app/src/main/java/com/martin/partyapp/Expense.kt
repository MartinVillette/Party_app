package com.martin.partyapp

class Expense (
    var expenseId: String?= null,
    var expenseName: String?= null,
    var price: Float = 0f,
    var userWhoPaid: User?= null,
    var usersConcerned: ArrayList<User> = ArrayList(),
    var pictureUrl : String? = null,
)