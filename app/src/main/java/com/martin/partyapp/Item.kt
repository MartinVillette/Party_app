package com.martin.partyapp

class Item (
    var itemId: String? = null,
    var itemName: String? = null,
    var itemQuantity: Int = 0,
    var itemUserQuantityList: ArrayList<ItemUserQuantity> = arrayListOf()
    //var itemUserMap: HashMap<User, Int> = hashMapOf(),
)