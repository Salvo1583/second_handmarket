package it.polito.mad.group18.second_handmarket.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Repository

class HomeViewModel : ViewModel(){

    //L'inizializzazione qui non è necessaria'
    lateinit var itemOnSale : MutableLiveData<MutableList<Item>> //= MutableLiveData<MutableList<Item>>().apply{this.value = mutableListOf<Item>()}
    //L'inizializzazione qui non è necessaria'
    lateinit var myItems : MutableLiveData<MutableList<Item>> //= MutableLiveData<MutableList<Item>>().apply{this.value = mutableListOf<Item>()}
    //var myItems : MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>().apply{this.value = mutableListOf<Item>()}
    lateinit var myInterestedItems : MutableLiveData<MutableList<String>>


    fun getItemOnSale (userId : String): MutableLiveData<MutableList<Item>>
    {
            itemOnSale = Repository().getItemListNoUser(userId)


        return itemOnSale


    }

    fun getMyItemList (userId: String) : MutableLiveData<MutableList<Item>>
    {

            myItems = Repository().getItemListUser(userId)

        return myItems

    }

    fun getImageItem (itemId : String)
    {




    }


    fun getInterestedItems(userId: String) : MutableLiveData<MutableList<String>>
    {

        myInterestedItems= Repository().getItemInterestedList(userId)

       return  myInterestedItems
    }

    fun getInterestedItemsObjects(interestedItems: MutableList<String>) : MutableLiveData<MutableList<Item>>
    {

        myItems = Repository().getItemsObjectFromList(interestedItems)

        return  myItems
    }

}