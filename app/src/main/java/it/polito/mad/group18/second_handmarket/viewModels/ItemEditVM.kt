package it.polito.mad.group18.second_handmarket.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import it.polito.mad.group18.second_handmarket.model.*
import java.util.*

class ItemEditVM() : ViewModel() {


    var photo : ByteArray = ByteArray(0)

    var itemDetails: MutableLiveData<Item> = MutableLiveData<Item>().apply { this.value= Item() }
    var title: MutableLiveData<String> = MutableLiveData<String>().apply { this.value= "" }
    var description: MutableLiveData<String> = MutableLiveData<String>().apply { this.value= "" }
    var price: MutableLiveData<String> = MutableLiveData<String>().apply { this.value= "" }
    var category = MutableLiveData<String>().apply { this.value= "" }
    var subCategory = MutableLiveData<String>().apply { this.value= "" }
    var location = MutableLiveData<String>().apply { this.value= "" }
    var expireDate = MutableLiveData<String>().apply { this.value= "" }
    var userId = MutableLiveData<String>().apply { this.value= "" }
    var itemId = MutableLiveData<String>().apply { this.value= "" }
    var status = MutableLiveData<String>().apply { this.value= "" }
    var temporaryStatus:String = ""
    val categoryL = CategoryList()
    var loadImage : MutableLiveData<Boolean?> = MutableLiveData<Boolean?>().apply { this.value = null }
    var image : MutableLiveData<ByteArray> = MutableLiveData<ByteArray>().apply { this.value = ByteArray(0)}
    var interestedUsers : MutableLiveData<MutableList<UserID>> = MutableLiveData<MutableList<UserID>>()
        .apply { this.value =  mutableListOf<UserID>()}
    //var interestedUsersEmail = mutableListOf<String?>()
    var interestedUsers2 = LinkedHashMap<String, String>()
    var itemPlace = MutableLiveData<Place?>().apply { this.value=null }
    var loadedVM: Boolean = false
    var itemLatitude :  MutableLiveData<Double> = MutableLiveData<Double>().apply { this.value = 0.0 }
    var itemLongitude  :  MutableLiveData<Double> = MutableLiveData<Double>().apply { this.value = 0.0 }
    var imageChange: Boolean = false
    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)


    init{



    }

    fun setNewItem(
        userId:String,
        title : String,
        category :String,
        subCategory : String,
        description : String,
        price : String ,
        expireDate:String,
        location :String,
        status :String,
        image : ByteArray)
    {
        var item = Item("", userId, title, category, subCategory, description, price, expireDate, location, status,latitude =itemLatitude.value!!,longitude =itemLongitude.value!!)
        itemDetails = Repository().setNewItem(item)
        val id = itemDetails.value!!.itemId
        setItem()
        Repository().setImageItem(id, image, loadImage)
    }

    fun updateItem(itemId : String,
                   userId:String,
                   title : String,
                   category :String,
                   subCategory : String,
                   description : String,
                   price : String ,
                   expireDate:String,
                   location :String,
                   status :String,
                   image : ByteArray,
                   imageChange : Boolean = false)
    {
        Log.d("stato", "updateItem")
        var item: Item=Item()
        if(temporaryStatus==""){
            item= Item(itemId, userId, title, category, subCategory, description, price, expireDate, location, status,latitude = itemLatitude.value!!,longitude = itemLongitude.value!!)
        }else{
            item = Item(itemId, userId, title, category, subCategory, description, price, expireDate, location, temporaryStatus, latitude = itemLatitude.value!!,longitude = itemLongitude.value!!)
        }

        Repository().updateItem(item)
        itemDetails = MutableLiveData<Item>().apply { this.value= item }
        setItem()
        if(imageChange)
            Repository().setImageItem(itemId, image, loadImage)
    }


    fun getItemDetails(itemId : String) : MutableLiveData<Item>{

        itemDetails= Repository().getItemNew(itemId)
        return itemDetails

    }

    fun getImage(itemId: String){
        image = Repository().getImageItem(itemId)
        loadImage.value = true
    }



    fun setItem(){
        Log.d("stato", "setItem")
        title.value = itemDetails.value!!.title
        description.value = itemDetails.value!!.description
        price.value = itemDetails.value!!.price
        category.value = itemDetails.value!!.category
        subCategory.value = itemDetails.value!!.subCategory
        location.value = itemDetails.value!!.location
        itemLatitude.value = itemDetails.value!!.latitude
        itemLongitude.value = itemDetails.value!!.longitude
        expireDate.value = itemDetails.value!!.expireDate
        userId.value = itemDetails.value!!.userId
        itemId.value = itemDetails.value!!.itemId
        status.value= itemDetails.value!!.status

    }

    fun changeStatus(status : String)
    {
        Repository().changeStatusItem(status, itemId.value!!)

    }
    fun sendNotifications(userId : String, info:String, status : String = "", buyer : UserID = UserID())
    {
        val m = Message(userId, itemId.value!!, title.value!!, info)
        if(status != "Sold")
            Repository().createNotification(m, interestedUsers.value!!)
        else
        {
            val noBuyerList = mutableListOf<UserID>()
            val buyerList = mutableListOf<UserID>()

            buyerList.add(buyer)

            for (el in interestedUsers.value!!) {
                if (el != buyer)
                    noBuyerList.add(el)
            }

            if (noBuyerList.isNotEmpty())
                Repository().createNotification(m, noBuyerList)

            m.informationItem = "Congratulation, You bought this item, Review It"
            Repository().createNotification(m, buyerList)
        }


    }
    fun getInterestedUserForItem(itemId : String) : MutableLiveData<MutableList<UserID>>
    {
        interestedUsers = Repository().getInterestedUsersToItem(itemId)
        Log.d("interestedUsers","${interestedUsers.value}")
        return interestedUsers
    }

    fun setItemBought(userId: String, itemId: String){
        val item = Item(itemId, this.userId.value!!, title.value!!, category.value!!, subCategory.value!!, description.value!!, price.value!!, expireDate.value!!, location.value!!, status.value!!, userId,latitude = itemLatitude.value!!,longitude = itemLongitude.value!!)
        Repository().updateItem(item)
        Repository().buyItem(userId,itemId)

    }

    fun sendNotificationsToBuyer(userId : String, info:String)
    {
        val m = Message(userId, itemId.value!!, title.value!!, info)
        var list= mutableListOf<UserID>()
        Repository().createNotification(m, interestedUsers.value!!)


    }

}