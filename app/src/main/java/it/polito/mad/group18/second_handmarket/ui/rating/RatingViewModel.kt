package it.polito.mad.group18.second_handmarket.ui.rating

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Repository

class RatingViewModel : ViewModel() {
    private lateinit var itemOfUserWithRating: MutableLiveData<MutableList<Item>>
    private lateinit var numberRatingsOfUser: MutableLiveData<Int>

    fun getItemOfUserWithRating(userId: String) : MutableLiveData<MutableList<Item>> {
        itemOfUserWithRating = Repository().getItemListUserWithRating(userId)
        return itemOfUserWithRating
    }

    fun getNumberRatingsOfUser(userId: String) : MutableLiveData<Int> {
        numberRatingsOfUser = Repository().getNumberRatingsOfUser(userId)
        return numberRatingsOfUser
    }
}