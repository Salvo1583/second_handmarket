package it.polito.mad.group18.second_handmarket.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group18.second_handmarket.model.*

class ItemDetailsVM: ViewModel() {

    var itemDetails: MutableLiveData<Item> = MutableLiveData<Item>().apply { this.value= Item() }
    var itemTitle: MutableLiveData<String> = MutableLiveData<String>().apply { this.value= "" }
    var description: MutableLiveData<String> = MutableLiveData<String>().apply { this.value= "" }
    var price: MutableLiveData<String> = MutableLiveData<String>().apply { this.value= "" }
    var category = MutableLiveData<String>().apply { this.value= "" }
    var subCategory = MutableLiveData<String>().apply { this.value= "" }
    var location = MutableLiveData<String>().apply { this.value= "" }
    var expireDate = MutableLiveData<String>().apply { this.value= "" }
    var userId = MutableLiveData<String>().apply { this.value= "" }
    var itemId = MutableLiveData<String>().apply { this.value= "" }
    var image : MutableLiveData<ByteArray> = MutableLiveData<ByteArray>().apply { this.value = ByteArray(0)}
    var status : MutableLiveData<String> = MutableLiveData<String>().apply { this.value =""}
    var buyerUserId = MutableLiveData<String>().apply { this.value = "" }
    var buyerUserEmail = MutableLiveData<String>().apply { this.value = "" }
    var rating = MutableLiveData<Rating?>().apply { this.value = null }
    var coordinates :  MutableLiveData<Array<Double>> = MutableLiveData<Array<Double>>().apply { this.value = arrayOf(0.0,0.0) }
    var interestedUsers : MutableLiveData<MutableList<UserID>> = MutableLiveData<MutableList<UserID>>()
        .apply { this.value =  mutableListOf<UserID>()}
    var itemUser : MutableLiveData<User> = MutableLiveData<User>().apply { this.value = User()}




    fun getItemDetails(itemId : String) : MutableLiveData<Item>{

        itemDetails= Repository().getItemNew(itemId)

        return itemDetails

    }

    fun getImage(itemId: String){
        image = Repository().getImageItem(itemId)
    }

    fun setItem(){

        itemTitle.value = itemDetails.value!!.title
        description.value = itemDetails.value!!.description
        price.value = itemDetails.value!!.price
        category.value = itemDetails.value!!.category
        subCategory.value = itemDetails.value!!.subCategory
        location.value = itemDetails.value!!.location
        Log.d("mappa","Set Item ${itemDetails.value!!.latitude}, ${itemDetails.value!!.longitude}")

        coordinates.value!![0] = itemDetails.value!!.latitude
        coordinates.value!![1]=itemDetails.value!!.longitude

        expireDate.value = itemDetails.value!!.expireDate
        userId.value = itemDetails.value!!.userId
        itemId.value = itemDetails.value!!.itemId
        status.value = itemDetails.value!!.status
        buyerUserId.value = itemDetails.value!!.buyerUserId
        if (itemDetails.value!!.rating != null)
            rating.value = Rating(itemDetails.value!!.rating!!.value, itemDetails.value!!.rating!!.description)


    }

    fun setInscription(itemId : String, userID : String, email : String)
    {
        val u = UserID(userID, email)
        Repository().interestToItem(itemId, u)


    }


    fun getInterestedUserForItem(itemId : String) : MutableLiveData<MutableList<UserID>>
    {
        interestedUsers = Repository().getInterestedUsersToItem(itemId)
        return interestedUsers
    }


    fun sendNotifications(fromUser : String, info:String, type : String)
    {
        val m = Message(fromUser, itemId.value!!, itemTitle.value!!, info, type=type)

        Repository().createNotificationForOwnerItem(m, userId.value!!)

    }

    fun getItemUSer( userID: String) : MutableLiveData<User> {

        itemUser= Repository().getUser(userID)
        return itemUser
    }

    fun isItemBuyer(itemId: String, userID: String) : Boolean {
        return itemDetails.value!!.buyerUserId == userID
    }

    fun saveRating(itemId: String) {
        val item = Item(itemId, userId.value!!, itemTitle.value!!, category.value!!, subCategory.value!!, description.value!!, price.value!!,
            expireDate.value!!, location.value!!, status.value!!, buyerUserId.value!!, rating.value ,latitude =  coordinates.value!![0],longitude = coordinates.value!![1])
        Repository().updateItem(item)
        setItem()
    }

    fun createEmptyRating() {
        rating.value = Rating()
    }

    fun discardTempRating() {
        rating.value = null
    }

    fun isRatingFromDb(rating: Rating) : Boolean {
        return (rating.value != 0.0 && itemDetails.value!!.rating != null && itemDetails.value!!.rating!!.value == rating.value)
    }

    fun getBuyerEmail(buyerId: String):MutableLiveData<String>{
        buyerUserEmail = Repository().getBuyerEmail(buyerId)
        return buyerUserEmail
    }
}

