package it.polito.mad.group18.second_handmarket.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.AuthCredential
import it.polito.mad.group18.second_handmarket.model.Repository
import it.polito.mad.group18.second_handmarket.model.User

class UserVM : ViewModel() {
    var userDetails = MutableLiveData<User>().apply { value = User() }
    var userId = MutableLiveData<String>().apply { value = "" }
    var fullName = MutableLiveData<String>().apply { value = "" }
    var nickName = MutableLiveData<String>().apply { value = "" }
    var phone = MutableLiveData<String>().apply { value = "" }
    var email = MutableLiveData<String>().apply { value = "" }
    var location = MutableLiveData<String>().apply { value = "" }
    var totalRating = MutableLiveData<Double>().apply { value = 0.0 }
    var numRatings = MutableLiveData<Int>().apply { value = 0 }
    var ratingAvg = MutableLiveData<Double>().apply { value = 0.0 }
    var loadImage = MutableLiveData<Boolean?>().apply { value = null }
    var image = MutableLiveData<ByteArray>().apply { value = ByteArray(0) }
    var userPlace = MutableLiveData<Place?>().apply { this.value=null }
    var userLatitude = MutableLiveData<Double>().apply { this.value = 0.0 }
    var userLongitude = MutableLiveData<Double>().apply { this.value = 0.0 }
    var userIsPresent = MutableLiveData<String>().apply { this.value = ""}
    var imageChange: Boolean = false
    var googleDone : Boolean = false
    var credentialGoogle : AuthCredential? = null

    var loadedVM: Boolean = false

    fun obfuscatePhone(){
        var phoneRes: String = ""
        var firstDigit: Int = 0
        var currentDigit: Int = 2
        if (phone.value!![0] == '+'){
            phoneRes += phone.value!![0].toString() + phone.value!![1] + phone.value!![2]
            firstDigit = 3
            currentDigit = 5
        }
        phoneRes += phone.value!![firstDigit].toString() + phone.value!![firstDigit+1]
        while (currentDigit < phone.value!!.length-3){
            phoneRes += '*'
            currentDigit++
        }
        phoneRes += phone.value!![phone.value!!.length-3].toString() + phone.value!![phone.value!!.length-2] + phone.value!![phone.value!!.length-1]
        phone.value = phoneRes
    }

     fun setUser(){
         userId.value = userDetails.value!!.userId
         fullName.value = userDetails.value!!.fullName
         nickName.value = userDetails.value!!.nickName
         phone.value = userDetails.value!!.phoneNumber
         email.value = userDetails.value!!.email
         location.value = userDetails.value!!.geoArea
         totalRating.value = userDetails.value!!.totalRating
         ratingAvg.value = totalRating.value!! / userDetails.value!!.numRatings
         numRatings.value = userDetails.value!!.numRatings
         userLatitude.value=userDetails.value!!.latitude
         userLongitude.value=userDetails.value!!.longitude

     }

    fun setUserNew(uID : String, ff : String, nn :String, p : String, e : String, l : String)
    {
        userId.value = uID
        fullName.value = ff
        nickName.value = nn
        phone.value = p
        email.value = e
        location.value = l
    }

    fun updateUser(userId: String,
                   fullName: String,
                   nickName: String,
                   phone: String,
                   email: String,
                   location: String,
                   image: ByteArray,
                   imageChange : Boolean = false){
        val u = User(userId, fullName, nickName, email, phone, location, totalRating.value!!, numRatings.value!!,"users/${userId}.png", false,latitude =userLatitude.value!!,longitude =userLongitude.value!!)
        setUserNew(userId, fullName, nickName, phone, email, location)

        if(imageChange)
            Repository().setUserImage(userId, image, loadImage)

        Repository().setUser(u)

    }

    fun getImage(userId: String){
        image = Repository().getUserImage(userId)

    }

    fun getUserInfo(userId: String) : MutableLiveData<User>{
        userDetails = Repository().getUser(userId)

        return userDetails
    }

    fun getUserWithEmail(email: String) : MutableLiveData<String>{
        userIsPresent = Repository().getUserWithEmail(email)

        return userIsPresent
    }
}