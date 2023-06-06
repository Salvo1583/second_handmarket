package it.polito.mad.group18.second_handmarket.model

import java.util.*

data class Message(
    var from: String = "",
    var itemId: String = "",
    var titleItem: String = "",
    var informationItem: String = "",
    var read: Boolean = false,
    var to: String = "",
    var msgId: String = "",
    var date: Date = Calendar.getInstance().time,
    var type : String ="")