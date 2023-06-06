package it.polito.mad.group18.second_handmarket.ui.BoughtItemsList

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Repository

class BoughtItemsListVM: ViewModel() {

    //L'inizializzazione qui non è necessaria'
    lateinit var myBoughtItemsObjects : MutableLiveData<MutableList<Item>> //= MutableLiveData<MutableList<Item>>().apply{this.value = mutableListOf<Item>()}
    //var myItems : MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>().apply{this.value = mutableListOf<Item>()}
    lateinit var myBoughtItemsList : MutableLiveData<MutableList<String>>



    fun getBoughtItemsList(userId: String) : MutableLiveData<MutableList<String>>
    {

        myBoughtItemsList= Repository().getItemBoughtList(userId)

        return  myBoughtItemsList
    }

    fun getBoughtItemsObjects(interestedItems: MutableList<String>) : MutableLiveData<MutableList<Item>>
    {

        myBoughtItemsObjects = Repository().getItemsObjectFromList(interestedItems, true)

        return  myBoughtItemsObjects
    }

}