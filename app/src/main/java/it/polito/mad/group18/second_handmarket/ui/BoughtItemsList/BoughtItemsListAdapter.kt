package it.polito.mad.group18.second_handmarket.ui.BoughtItemsList

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.util.*


class BoughtItemsListAdapter(var adv : MutableList<Item>, val ctx : Fragment,val recView : RecyclerView) : RecyclerView.Adapter<BoughtItemsListAdapter.BoughtItemsVH>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoughtItemsListAdapter.BoughtItemsVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_cards_list, parent, false)

        return BoughtItemsListAdapter.BoughtItemsVH(v)
    }

    override fun getItemCount(): Int = adv.size
    override fun onBindViewHolder(holder: BoughtItemsVH, position: Int) {
        holder.bind(adv[position],ctx)
        //runLayoutAnimation(recView)
    }

    fun itemsSet(newItems: MutableList<Item>) {
        if (newItems != adv) {
            val diff = DiffUtil.calculateDiff(BoughtItemsDiffCallBack(adv, newItems))
            adv = newItems
            diff.dispatchUpdatesTo(this)
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


    class BoughtItemsVH(v: View) : RecyclerView.ViewHolder(v){

        fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
            val formatter = SimpleDateFormat(format, locale)
            return formatter.format(this)
        }

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


            card.setOnClickListener {
                n.navigate(
                    R.id.action_boughtItemsListFragment_to_itemDetailsFragment,
                    bundle
                )
            }
            modify.visibility = View.INVISIBLE

            card_status.visibility=View.INVISIBLE
            text_status.text="Bought"
            image_status.setImageResource(R.drawable.ic_check)

        }
    }
}