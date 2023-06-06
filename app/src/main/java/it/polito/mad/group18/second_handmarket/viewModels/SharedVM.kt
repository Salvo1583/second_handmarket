package it.polito.mad.group18.second_handmarket.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Message
import it.polito.mad.group18.second_handmarket.model.Repository

class SharedVM : ViewModel() {

    var userId : String = ""
    var itemId : String = ""
    var direction : String= ""
    var userEmail : String =""
    var myItems : MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>()
        .apply { this.value =  mutableListOf<Item>()}
    var interestedItems : MutableLiveData<MutableList<String>> = MutableLiveData<MutableList<String>>()
        .apply { this.value =  mutableListOf<String>()}
    var startFrom : String = ""

    fun getMyItemList() : MutableLiveData<MutableList<Item>>
    {
        Log.d("topicUnsubscribe", "UserId inside SharedVM: $userId")
        myItems = Repository().getItemListUser(userId)
        return  myItems
    }

    fun getInterestedItemsList(): MutableLiveData<MutableList<String>>
    {
        interestedItems = Repository().getItemInterestedList(userId)
        return interestedItems

    }

}