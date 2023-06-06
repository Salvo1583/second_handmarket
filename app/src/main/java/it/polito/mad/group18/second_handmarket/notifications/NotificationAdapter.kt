package it.polito.mad.group18.second_handmarket.notifications

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color.RED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Message
import it.polito.mad.group18.second_handmarket.model.Repository
import it.polito.mad.group18.second_handmarket.ui.home.HomeAdapter
import it.polito.mad.group18.second_handmarket.ui.home.HomeDiffCallBack
import it.polito.mad.group18.second_handmarket.ui.myItemsList.MyItemList
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*




class NotificationAdapter(var messages : List<Message>, val ctx : Fragment) : RecyclerView.Adapter<NotificationAdapter.NotifVH>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_card, parent, false)

        return NotifVH(v)
    }

    override fun getItemCount(): Int = messages.size
    override fun onBindViewHolder(holder: NotifVH, position: Int) {
        holder.bind(messages[position],ctx)
    }


    class NotifVH(v: View) : RecyclerView.ViewHolder(v){

        fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

        val card = v.findViewById<MaterialCardView>(R.id.cardN)
        val title = v.findViewById<TextView>(R.id.titleN)
        val info = v.findViewById<TextView>(R.id.info)
        val date = v.findViewById<TextView>(R.id.date)
        val state = v.findViewById<TextView>(R.id.state)
        var key : String = ""
        var imageItem : ImageView = v.findViewById(R.id.notification_image)
        var keyProfile : String = ""
        var imageProfile : ImageView = v.findViewById(R.id.notification_profile)

        @SuppressLint("ResourceAsColor")
        fun bind(m : Message, ctx : Fragment)
        {
            val frg = ctx as NotificationFragment
            val n = ctx.findNavController()
            val bundle = Bundle()
            bundle.putString("group-18-keyCardSelected", m.itemId)
            bundle.putString("group-18-direction", "ListRecycler")
            if (m.type == "interest" || m.type == "review")
                bundle.putString("group-18-itemUserId", m.to )
            else
                bundle.putString("group-18-itemUserId", m.from )

            title.text = m.titleItem
            info.text = m.informationItem
            date.text = m.date.toString(("yyyy/MM/dd HH:mm:ss"))
            key=m.itemId
            keyProfile=m.from

            Repository().getImageItem(key).observe(ctx.viewLifecycleOwner, androidx.lifecycle.Observer {

                if(it.isNotEmpty())
                {
                    val arrayInputStream = ByteArrayInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(arrayInputStream)
                    imageItem.setImageBitmap(imageBitmap)
                }

            })

            Repository().getUserImage(keyProfile).observe(ctx.viewLifecycleOwner, androidx.lifecycle.Observer {

                if(it.isNotEmpty())
                {
                    val arrayInputStream = ByteArrayInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(arrayInputStream)
                    imageProfile.setImageBitmap(imageBitmap)
                }

            })

            if(m.read){
                state.text = "Readed"
            }else{
                state.setTextColor(RED)
                title.setTextColor(RED)
                state.text = "New Message"
            }

            card.setOnClickListener{
                n.navigate(R.id.action_notificationFragment_to_itemDetailsFragment, bundle)
                frg.ownVM.readMessage(m.msgId)
            }



        }

    }



}