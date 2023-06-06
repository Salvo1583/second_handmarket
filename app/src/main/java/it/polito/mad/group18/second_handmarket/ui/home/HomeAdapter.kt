package it.polito.mad.group18.second_handmarket.ui.home

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Repository
import it.polito.mad.group18.second_handmarket.ui.myItemsList.MyItemList
import java.io.ByteArrayInputStream
import java.util.*

class HomeAdapter(var adv: MutableList<Item>, val ctx: Fragment, var onSale: Boolean, val recView : RecyclerView) :
    RecyclerView.Adapter<HomeAdapter.HomeVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_cards_list, parent, false)

        return HomeVH(v, onSale)
    }

    override fun getItemCount(): Int = adv.size

    override fun onBindViewHolder(holder: HomeVH, position: Int) {

        holder.bind(adv[position], ctx)
        //runLayoutAnimation(recView)
    }

    fun itemsSet(newItems: MutableList<Item>) {
        if (newItems != adv) {
            val diff = DiffUtil.calculateDiff(HomeDiffCallBack(adv, newItems))
            adv = newItems
            diff.dispatchUpdatesTo(this)
        }

    }

    fun filterTitle(allItemOnSale: MutableList<Item>, title: String) {
        var newList = mutableListOf<Item>()

        if (allItemOnSale.isNotEmpty()) {
            for (el in allItemOnSale) {
                if (el.title.toLowerCase(Locale.ROOT).contains(title) ||
                    el.location.toLowerCase(Locale.ROOT).contains(title) ||
                    el.price == title ||
                    el.category.toLowerCase(Locale.ROOT).contains(title)
                )
                    newList.add(el)
            }

            itemsSet(newList)
        }


    }

    private fun runLayoutAnimation(recyclerView: RecyclerView) {
        val context = recyclerView.context
        var controller = AnimationUtils.loadLayoutAnimation(
            context,
            R.anim.layout_animation_left_to_right
        )
        recyclerView.layoutAnimation = controller
        recyclerView.scheduleLayoutAnimation()
    }

    class HomeVH(v: View, val onSale: Boolean) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
        val location: TextView = v.findViewById(R.id.location)
        val expiredate: TextView = v.findViewById(R.id.expiredate)
        val price: TextView = v.findViewById(R.id.price)
        val card: MaterialCardView = v.findViewById(R.id.card)
        val modify = v.findViewById<ImageButton>(R.id.modify)
        var key: String = ""
        var imageItem: ImageView = v.findViewById(R.id.itemImage)
        var imageView: ImageView = v.findViewById(R.id.imageView)
        val progresBar: ProgressBar = v.findViewById(R.id.progressBar)
        val text_status: TextView=v.findViewById(R.id.text_status)
        val card_status: View=v.findViewById(R.id.card_status)
        val image_status: ImageView=v.findViewById(R.id.image_status)

        fun bind(h: Item, ctx: Fragment) {
            key = h.itemId
            title.text = h.title
            location.text = h.location
            expiredate.text = h.expireDate
            price.text = h.price
            imageItem.setImageResource(R.drawable.photo_default)

            Repository().getImageItem(key)
                .observe(ctx.viewLifecycleOwner, androidx.lifecycle.Observer {

                    if (it.isNotEmpty()) {

                       if(it.size > 2) {
                           val arrayInputStream = ByteArrayInputStream(it)
                           val imageBitmap = BitmapFactory.decodeStream(arrayInputStream)
                           imageItem.setImageBitmap(imageBitmap)
                           progresBar.visibility = View.INVISIBLE
                           imageView.visibility = View.VISIBLE
                           imageItem.visibility = View.VISIBLE
                       }
                        else
                       {
                           if(it.size == 1)
                           {
                               //L'immagine non esiste
                               progresBar.visibility = View.INVISIBLE
                               imageItem.visibility = View.VISIBLE
                               imageView.visibility = View.VISIBLE
                               imageItem.setImageResource(R.drawable.photo_default)
                           }
                           else if (it.size == 2)
                           {
                               //l'immagine esiste ma non la riesce a scaricare'
                               progresBar.visibility = View.VISIBLE
                               imageView.visibility = View.INVISIBLE
                               imageItem.visibility = View.INVISIBLE
                           }
                       }
                    } else {
                        progresBar.visibility = View.VISIBLE
                        imageView.visibility = View.INVISIBLE
                        imageItem.visibility = View.INVISIBLE
                    }

                })


            val n = ctx.findNavController()
            val bundle = Bundle()
            bundle.putString("group-18-keyCardSelected", key)
            bundle.putString("group-18-direction", "ListRecycler")
            bundle.putString("group-18-itemUserId", h.userId)

            //card.setOnClickListener{n.navigate(R.id.action_nav_home_to_itemDetailsFragment, bundle)}

            if (!onSale) {
                val frg = ctx as MyItemList



                card.setOnClickListener {
                    n.navigate(
                        R.id.action_myItemList_to_itemDetailsFragment,
                        bundle
                    )
                }
                modify.setOnClickListener {
                    frg.sharedVM.direction = "ItemDetails"
                    frg.sharedVM.itemId = key
                    bundle.putString("group-18-keyCardSelected", key)
                    bundle.putString("group-18-direction", "CardModify")
                    n.navigate(R.id.action_myItemList_to_itemEditFragment, bundle)
                }

                if(h.status=="Sold"){
                    modify.visibility = View.GONE
                    card_status.setBackgroundColor(Color.parseColor("#C10404"))
                    text_status.text="Sold"
                    image_status.setImageResource(R.drawable.ic_shopping)
                }else if(h.status=="Available"){

                    card_status.setBackgroundColor(Color.parseColor("#00b300"))
                    text_status.text="Available"
                    image_status.setImageResource(R.drawable.ic_check)
                }else if(h.status=="Unavailable"){
                    card_status.setBackgroundColor(Color.parseColor("#ff6600"))
                    text_status.text="Unavailable"
                    image_status.setImageResource(R.drawable.ic_block)
                }


            } else {
                card.setOnClickListener {
                    n.navigate(
                        R.id.action_nav_home_to_itemDetailsFragment,
                        bundle
                    )
                }
                modify.visibility = View.GONE

                if(h.status=="Sold"){
                    card_status.setBackgroundColor(Color.parseColor("#C10404"))
                    text_status.text="Sold"
                    image_status.setImageResource(R.drawable.ic_shopping)
                }else if(h.status=="Available"){

                    card_status.setBackgroundColor(Color.parseColor("#00b300"))
                    text_status.text="Available"
                    image_status.setImageResource(R.drawable.ic_check)
                }else if(h.status=="Unavailable"){
                    card_status.setBackgroundColor(Color.parseColor("#ff6600"))
                    text_status.text="Unavailable"
                    image_status.setImageResource(R.drawable.ic_block)
                }

            }
        }


    }


}