package com.martin.partyapp

class Item (
    var itemId: String? = null,
    var itemName: String? = null,
    var itemQuantity: Int = 0,
    var itemUserMap: HashMap<String, Int> = hashMapOf(),
)