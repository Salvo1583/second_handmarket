package it.polito.mad.group18.second_handmarket.model

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class Repository {

    private var db = Firebase.firestore
    private var storage = Firebase.storage

    fun setUser(user: User) {

        val refUser = db.collection("Users").document(user.userId)

        db.runBatch { batch ->
            batch.set(refUser, user)
        }
            .addOnCompleteListener { Log.d("userDB", "setUser SUCCESS") }
            .addOnFailureListener { Log.d("userDB", "setUser FAILURE: ${it.message}") }
    }

    fun getUser(userId: String): MutableLiveData<User> {
        val user: MutableLiveData<User> = MutableLiveData<User>().apply { this.value = User() }

        db.collection("Users")
            .document(userId)
            .get()
            .addOnSuccessListener {

                Log.d("userDB", "Document snapshot content:\n${it.toString()}")
                val userVal = it.toObject(User::class.java)
                userVal!!.loaded = true
                user.postValue(userVal)

            }
            .addOnFailureListener { Log.d("userDB", "getUser FAILURE: ${it.message}") }


        return user
    }

    fun getUserWithEmail(email: String): MutableLiveData<String> {
        val confirm: MutableLiveData<String> = MutableLiveData<String>().apply { this.value = "" }

        db.collection("Users")
            .whereEqualTo("email", email).get()
            .addOnSuccessListener {
                val tempList = mutableListOf<User>()
                    if (it != null && it.documents.isNotEmpty()) {
                        for (doc in it.documents)
                            tempList.add(doc.toObject(User::class.java)!!)

                        confirm.postValue("Present")

                    }
                    else
                        confirm.postValue("Not_Present")

            }.addOnFailureListener {
                Log.d("Real", "Errore real time update on getUserWithEmail ${it.message}")
            }

        return confirm
    }

    fun setUserImage(userId: String, image: ByteArray, loadedProfileImage: MutableLiveData<Boolean?>){
        val storageRef = storage.reference
        val userRef = storageRef.child("users/${userId}.png")
        userRef.putBytes(image)
            .addOnSuccessListener { Log.d("userDB", "User profile photo update SUCCESS")
                loadedProfileImage.postValue(true)
            }
            .addOnFailureListener { Log.w("userDB", "User profile photo update FAILURE:\n${it.message}")
                loadedProfileImage.postValue(false)
            }

    }

    fun getUserImage(userId: String) : MutableLiveData<ByteArray>{
        var userImage = MutableLiveData<ByteArray>().apply { value = ByteArray(0) }
        val storageRef = storage.reference
        val userRef = storageRef.child("users/${userId}.png")
        val maxSize: Long = 1024*1024*10 // ten MB of maximum size for the photo
        userRef.getBytes(maxSize)
            .addOnSuccessListener {
                Log.d("userDB", "User profile photo retrieved SUCCESS")
                userImage.postValue(it)

            }
            .addOnFailureListener { Log.w("userDB", "User profile photo not retrieved FAILURE:\n${it.message}")
                if (it.message!!.toLowerCase().contains("Object does not exist at location.".toLowerCase()))
                {
                    // if this error appear, the image was never saved
                    Log.d("userDB", "ENTRA 1")
                    userImage.postValue(ByteArray(1))
                }
                else {
                    // if this error appear, the image exists but Google Storage does not respond
                    Log.d("userDB", "ENTRA 2")
                    userImage.postValue(ByteArray(2))
                }

            }
        return userImage
    }

    fun setNewItem(item: Item):MutableLiveData<Item> {
        val itemRef = db.collection("Items").document()
        item.itemId = itemRef.id
        itemRef.set(item)
            .addOnSuccessListener { Log.d("item", "setNewItem SUCCESS") }
            .addOnFailureListener { Log.d("item", "setNewItem FAILURE: ${it.message}") }
        return MutableLiveData<Item>(item)
    }

    fun setImageItem(itemId : String, image:ByteArray, loaded : MutableLiveData<Boolean?>)
    {
        var storageRef = storage.reference
        var itemRef = storageRef.child("items/$itemId.png")

        itemRef.putBytes(image)
            .addOnFailureListener { Log.d("item", "Upload Image FAILED: ${it.message}")
                loaded.postValue(false)}
            .addOnSuccessListener { Log.d("item", "Upload Image SUCCESS")
            loaded.postValue(true)}
    }

    fun getImageItem (itemId: String) : MutableLiveData<ByteArray>
    {
        var imageDB : MutableLiveData<ByteArray> = MutableLiveData<ByteArray>().apply { this.value = ByteArray(0) }
        val storageRef= storage.reference
        val itemRef = storageRef.child("items/$itemId.png")
        val TEN_MEGABYTE: Long = 1024 * 1024 * 10
        itemRef.getBytes(TEN_MEGABYTE)
            .addOnSuccessListener {
                Log.d("item", "DOWNLOAD Image SUCCESS")
                imageDB.postValue(it)
            }.addOnFailureListener { Log.w("item", "Item photo not retrieved FAILURE:\n${it.message}")
                if (it.message!!.toLowerCase().contains("Object does not exist at location.".toLowerCase()))
                {
                    // if this error appear, the image was never saved
                    Log.d("item", "ENTRA 1")
                    imageDB.postValue(ByteArray(1))
                }
                else {
                    // if this error appear, the image exists but Google Storage does not respond
                    Log.d("item", "ENTRA 2")
                    imageDB.postValue(ByteArray(2))
                }

            }

         return imageDB
    }

    fun updateItem(item: Item) {
        val refItem = db.collection("Items").document(item.itemId)

        db.runBatch { batch ->
            batch.set(refItem, item)
        }
            .addOnCompleteListener { Log.d("item", "updateItem SUCCESS") }
            .addOnFailureListener { Log.d("item", "updateItem FAILURE: ${it.message}") }

    }


    fun getItem(itemId: String): MutableLiveData<Item> {
        val item: MutableLiveData<Item> = MutableLiveData<Item>().apply { this.value = Item() }

        db.collection("Items")
            .document(itemId)
            .get()
            .addOnSuccessListener {

                val itemVal = it.toObject(Item::class.java)
                itemVal!!.loaded = true
                item.postValue(itemVal)

            }
            .addOnFailureListener { Log.d("item", "getItem FAILURE: ${it.message}") }

        return item
    }


    fun getItemNew(itemId: String): MutableLiveData<Item> {
        val item: MutableLiveData<Item> = MutableLiveData<Item>().apply { this.value = Item() }

        db.collection("Items").document(itemId).addSnapshotListener{ query, e->
            if (e != null)
                Log.d("getitemNew", "Error realTime UpdateitemDeails")
            else
            {
                if (query != null && query.exists()){
                    val itemVal = query.toObject(Item::class.java)
                    itemVal!!.loaded = true
                    item.postValue(itemVal)
                }
                 else
                Log.d ("getitemNew", "Current data: null")
            }

        }

        return item
    }

    fun getItemListUser(userId: String): MutableLiveData<MutableList<Item>> {
        val items: MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>().apply {
            this.value = mutableListOf<Item>()
        }

        db.collection("Items")
            .whereEqualTo("userId", userId).addSnapshotListener { res, e ->
                if (e != null)
                    Log.d("Real", "Errore real time update ${e.message}")
                else {
                    val tempList = mutableListOf<Item>()
                    if (res!!.documents.isNotEmpty()) {
                        for (doc in res.documents)
                            tempList.add(doc.toObject(Item::class.java)!!)

                        items.postValue(tempList)

                    }
                }
            }

        return items

    }

    fun getItemListUserWithRating(userId: String) : MutableLiveData<MutableList<Item>> {
        val items = MutableLiveData<MutableList<Item>>().apply { value = mutableListOf<Item>() }
        db.collection("Items")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Log.d("ratingDB", "Error in getting items with rating. Failure: ${exception.message}")
                } else {
                    val tempList = mutableListOf<Item>()
                    if (querySnapshot!!.documents.isNotEmpty()) {
                        for (doc in querySnapshot.documents) {
                            val item = doc.toObject(Item::class.java)!!
                            if (item.rating != null) {
                                tempList.add(item)
                            }
                        }
                        items.postValue(tempList)
                    }
                }
            }
        return items
    }

    fun getNumberRatingsOfUser(userId: String) : MutableLiveData<Int> {
        val ratings = MutableLiveData<Int>().apply { value = 0 }
        db.collection("Users")
            .document(userId)
            .get()
            .addOnFailureListener { Log.w("ratingDB", "Error in getting user number of ratings of user $userId. Failure: ${it.message}") }
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)!!
                ratings.postValue(user.numRatings)
            }
        return ratings
    }

   /* suspend fun getInterestedItemListUser(userId: String): MutableLiveData<MutableList<Item>> =
        withContext(Dispatchers.IO){

        /*val interestedItems = MutableLiveData<MutableList<String>>().apply {
            this.value = mutableListOf<String>()
        }*/

        val itemsList = async {
            getItemInterestedList(userId)
        }

        try{
            val l = itemsList.await()

            return@withContext interestedItemsList
        } catch (error: Throwable){
            Log.d(
                "interestedItem",
                "qualcosa è andato storto nel recuperare gli items $error"
            )
            return@withContext interestedItemsList
        }

    }
*/




    fun getItemsObjectFromList(l: MutableList<String>, bought : Boolean = false): MutableLiveData<MutableList<Item>>{

        val interestedItemsList: MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>().apply {
            this.value = mutableListOf<Item>()
        }
        if(l.isNotEmpty()) {
            val tempList = mutableListOf<Item>()
            //val tempList2 = mutableListOf<Item>()
            for (itemId in l) {
                Log.d("interestedItem", "prendo gli item dal db")
                db.collection("Items")
                    .whereEqualTo("itemId", itemId)
                    .get()
                    .addOnSuccessListener {
                        if (it != null && it.documents.isNotEmpty()) {
                            Log.d("interestedItem", "ho trovato gli item")
                            for (item in it) {
                                val doc = item.toObject(Item::class.java)!!
                                if(doc.status != "Sold")
                                    tempList.add(doc)
                                else if(bought == true)
                                    tempList.add(doc)
                            }
                            Log.d("interestedItem", "ho trovato gli item, ${tempList}")
                        }
                        interestedItemsList.postValue(tempList)
                    }
                    .addOnFailureListener {
                        Log.d("interestedItem", "qualcosa è andato storto nel recuperare gli items")
                    }
            }
            //interestedItemsList.postValue(tempList)
        }
        return interestedItemsList
    }

    fun getItemInterestedList(userId: String): MutableLiveData<MutableList<String>>{
        val interestedItems = MutableLiveData<MutableList<String>>().apply {
            this.value = mutableListOf<String>()
        }

        db.collection("Users")
            .document(userId)
            .collection("Interested_Items")
            .get()
            .addOnSuccessListener {

                if (it != null && it.documents.isNotEmpty()){
                    val tempList = mutableListOf<String>()
                    for(doc in it) {
                        tempList.add(doc.getString("itemId")!!)
                        Log.d("interestedItem","${doc.getString("itemId")}")
                    }
                    interestedItems.postValue(tempList)
                }

            }
            .addOnFailureListener{Log.d ("item", "Current data: null for list of interested users")}
        return interestedItems
    }



    fun getItemBoughtList(userId: String): MutableLiveData<MutableList<String>>{
        val boughtItems = MutableLiveData<MutableList<String>>().apply {
            this.value = mutableListOf()
        }

        db.collection("Users")
            .document(userId)
            .collection("Bought_Items")
            .get()
            .addOnSuccessListener {

                if (it != null && it.documents.isNotEmpty()){
                    val tempList = mutableListOf<String>()
                    for(doc in it) {
                        tempList.add(doc.getString("itemId")!!)
                        Log.d("boughtItem","${doc.getString("itemId")}")
                    }
                    boughtItems.postValue(tempList)
                }

            }
            .addOnFailureListener{Log.d ("item", "Current data: null for list of interested users")}
        return boughtItems
    }


    @SuppressLint("SimpleDateFormat")
    fun getItemListNoUser(userId: String): MutableLiveData<MutableList<Item>> {
        val items: MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>().apply {
            this.value = mutableListOf<Item>()
        }

        val today = Calendar.getInstance().time

        val sdf =  SimpleDateFormat("d/MM/yyyy")

        val todayString = sdf.format(today)


        val queryG = db.collection("Items").whereGreaterThan("userId", userId)
        val queryL = db.collection("Items").whereLessThan("userId", userId)


        val resQueryG = queryG.get()
        val resQueryL = queryL.get()

        val result: Task<List<QuerySnapshot>> = Tasks.whenAllSuccess(resQueryG, resQueryL)
        result.addOnSuccessListener {
            val tempList = mutableListOf<Item>()
            for (query in it)
                for (document in query.documents) {
                    val doc = document.toObject(Item::class.java)

                    if(doc!!.status != "Sold") {
                        val expireDate : Date = sdf.parse(doc.expireDate)!!
                        val expString : String = sdf.format(expireDate)
                        if(!expireDate.before(today) || expString == todayString)
                            tempList.add(doc)
                    }

                }
            items.postValue(tempList)


        }.addOnFailureListener { Log.d("item", "getItemListNoUser FAILURE: ${it.message}") }

        return items

    }

    fun getItemListNoUserNewVersion(userId: String): MutableLiveData<MutableList<Item>> {
        val items: MutableLiveData<MutableList<Item>> = MutableLiveData<MutableList<Item>>().apply {
            this.value = mutableListOf<Item>()
        }

        db.collection("Items").addSnapshotListener { query, e ->
            if (e != null)
                Log.d("item", "Error realTime Update List noUser item on SALE")
            else
            {
                Log.d("CONTA", "si real time listener")
                val res = query!!.documents
                if (res.isNotEmpty()) {
                    val tempList = mutableListOf<Item>()
                    for (doc in res) {
                        val obj = doc.toObject(Item::class.java)
                        if (obj!!.userId != userId)
                            tempList.add(obj)

                    }

                    if (tempList.isNotEmpty())
                        items.postValue(tempList)
                }

            }

        }

        return items
    }

    fun interestToItem (itemId: String, userId : UserID)
    {
        db.collection("Items")
            .document(itemId)
            .collection("Interested_Users")
            .document(userId.userId)
            .set(userId)
            .addOnFailureListener { Log.d("item", "Problem with loaded of subscription") }

        db.collection("Users")
            .document(userId.userId)
            .collection("Interested_Items")
            .document(itemId)
            .set(object {
                var itemId =itemId
            })
            .addOnFailureListener { Log.d("item", "Problem with loaded of subscription") }

    }

    fun changeStatusItem(status : String, itemId : String)
    {
        db.collection("Items")
            .document(itemId)
            .update("status", status)
            .addOnSuccessListener { Log.d("item", "Item Status successfully updated!") }
            .addOnFailureListener { e -> Log.d("item", "Error updating item status: ${e.message}") }




    }

    fun getInterestedUsersToItem(itemId: String) : MutableLiveData<MutableList<UserID>>
    {
        val users: MutableLiveData<MutableList<UserID>> = MutableLiveData<MutableList<UserID>>().apply {
            this.value = mutableListOf<UserID>()
        }
        db.collection("Items")
            .document(itemId)
            .collection("Interested_Users")
            .addSnapshotListener{ query, e->
                if (e != null)
                    Log.d("item", "Error realTime Update List interested Users")
                else
                {
                    if (query != null && query.documents.isNotEmpty()){
                        val uL = mutableListOf<UserID>()
                        for(doc in query)
                            uL.add(doc.toObject(UserID::class.java))

                        users.postValue(uL)
                    }
                    else
                        Log.d ("interestedItem", "Current data: null for list of interested users")
                }

            }



        return users

    }


    fun createNotification(msg : Message, users : MutableList<UserID>)
    {
        for(i in users)
        {
            val msgDoc = db.collection("Messages").document()

            msg.msgId = msgDoc.id
            msg.to = i.userId

            msgDoc.set(msg).addOnFailureListener {
                Log.d("item", "Messaggio non inviato") }

        }

    }

    fun createNotificationForOwnerItem(msg : Message, users : String)
    {

            val msgDoc = db.collection("Messages").document()

            msg.msgId = msgDoc.id
            msg.to = users

            msgDoc.set(msg).addOnFailureListener {
                Log.d("item", "Messaggio non inviato") }

    }

    fun getMessageUsers(userId : String) : MutableLiveData<MutableList<Message>>
    {
        val messages = MutableLiveData<MutableList<Message>>().apply { this.value = mutableListOf<Message>()}

        db.collection("Messages")
            .addSnapshotListener{query, e ->
                if (e != null)
                    Log.d("item", "Errore caricamento messaggi")
                else
                {
                    if (query != null && query.documents.isNotEmpty())
                    {
                        val list = mutableListOf<Message>()
                        for (doc in query.documents)
                            list.add(doc.toObject(Message::class.java)!!)

                        messages.postValue(list)

                    }



                }
            }

        return messages
    }

    fun readMessage(messageId : String)
    {
        db.collection("Messages")
            .document(messageId)
            .update("read", true)
            .addOnSuccessListener { Log.d("message", "Message Status successfully updated!") }
            .addOnFailureListener { e -> Log.d("message", "Error updating message status: ${e.message}") }

    }

    fun buyItem(userId: String, itemId: String){
        // insert itemId inside user bought item list
        db.collection("Users")
            .document(userId)
            .collection("Bought_Items")
            .document(itemId)
            .set(object {
                var itemId =itemId
            })
            .addOnFailureListener { Log.d("item", "Problem with loaded of sell") }

        // change buyerUserId of item
        val refItem = db.collection("Items").document(itemId)
        db.runTransaction { transaction ->
            transaction.update(refItem, "buyerUserId", userId)
        }.addOnSuccessListener { result ->
            Log.d("ratingInsert", "Transaction result $result")
        }.addOnFailureListener { e ->
            Log.w("ratingInsert", "Transaction failure: ${e.message}")
        }
    }

    fun getBuyerEmail(buyerId:String) : MutableLiveData<String>{
        var buyerEmail = MutableLiveData<String>()
        db.collection("Users")
            .document(buyerId)
            .get()
            .addOnSuccessListener {
               var buyerUser = it.toObject(User::class.java)!!.email
                buyerEmail.postValue(buyerUser)
            }
            .addOnFailureListener { Log.d("buyerEmail", "Error in retrieving buyer user email. Failure: ${it.message}") }
        return buyerEmail
    }

    fun registerRating(itemId: String, rating: Rating){
        val itemRef = db.collection("Items").document(itemId)
        var itemRated = Item()
        db.collection("Items").document(itemId).get()
            .addOnFailureListener { Log.d("item", "Error in retrieving item to be rated. Failure: ${it.message}") }
            .addOnSuccessListener {
                itemRated = it.toObject(Item::class.java)!!
            }
        itemRated.rating = rating
        db.runBatch { batch ->
            batch.set(itemRef, itemRated)
        }
            .addOnCompleteListener { Log.d("item", "Correctly rated item $itemId") }
            .addOnFailureListener { Log.d("item", "Error in inserting the rating on item $itemId. Failure: ${it.message}") }
    }

    fun retrieveRatings(userId: String) : MutableLiveData<MutableList<Item>> {
        val ratings = MutableLiveData<MutableList<Item>>().apply { value = mutableListOf<Item>() }
        db.collection("Items")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Log.d("ratingDB", "Error in getting ratings of user $userId. Failure: ${exception.message}")
                } else {
                    if (querySnapshot != null && querySnapshot.documents.isNotEmpty()) {
                        val list = mutableListOf<Item>()
                        for (doc in querySnapshot.documents) {
                            val item = doc.toObject(Item::class.java)!!
                            if (item.rating != null)
                                list.add(item)
                        }
                        ratings.postValue(list)
                    }
                }
            }
        return ratings
    }

}

