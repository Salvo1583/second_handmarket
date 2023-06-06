package it.polito.mad.group18.second_handmarket.notifications

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.group18.second_handmarket.model.Message
import it.polito.mad.group18.second_handmarket.model.Repository

class NotificationVIewModel : ViewModel() {

    var Messages : MutableLiveData<MutableList<Message>> = MutableLiveData<MutableList<Message>>().apply { this.value = mutableListOf<Message>() }


    fun getMessage(userId : String) : MutableLiveData<MutableList<Message>>
    {
        Messages = Repository().getMessageUsers(userId)
        return  Messages

    }

    fun filterMessages (userid : String) : MutableList<Message>
    {
        val myMessages = mutableListOf<Message>()

        for (el in Messages.value!!)
        {
            if (el.to == userid)
                myMessages.add(el)

        }

         return myMessages

    }

    fun readMessage(messageId : String){
        Repository().readMessage(messageId)
    }


}