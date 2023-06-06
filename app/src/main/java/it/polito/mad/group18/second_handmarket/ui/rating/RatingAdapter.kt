package it.polito.mad.group18.second_handmarket.ui.rating

import android.content.res.Configuration
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Repository

class RatingAdapter(var adv: MutableList<Item>, val context: Fragment, var onSale: Boolean): RecyclerView.Adapter<RatingAdapter.RatingVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingAdapter.RatingVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rating_card, parent, false)
        return RatingVH(v, onSale)
    }

    override fun getItemCount(): Int = adv.size

    override fun onBindViewHolder(holder: RatingAdapter.RatingVH, position: Int) {
        holder.bind(adv[position], context)
    }

    fun itemSet(newItems: MutableList<Item>) {
        if (newItems != adv) {
            val diff = DiffUtil.calculateDiff(RatingDiffCallBack(adv, newItems))
            adv = newItems
            diff.dispatchUpdatesTo(this)
        }
    }

    class RatingVH(v: View, val onSale: Boolean): RecyclerView.ViewHolder(v) {
        val ratingBar = v.findViewById<RatingBar>(R.id.readRatingBar)
        val ratingValue = v.findViewById<TextView>(R.id.ratingValue)
        val rater = v.findViewById<TextView>(R.id.buyerUser)
        val itemRated = v.findViewById<TextView>(R.id.itemRef)
        val ratingDescription = v.findViewById<TextView>(R.id.rating_description)
        var keyItem: String = ""
        var keyBuyerUser: String = ""
        lateinit var userPublicId: MutableLiveData<String>

        fun bind(item: Item, context: Fragment) {
            keyItem = item.itemId
            keyBuyerUser = item.buyerUserId
            ratingBar.rating = item.rating!!.value.toFloat()
            ratingValue.text = String.format("%.1f", ratingBar.rating)
            itemRated.text = HtmlCompat.fromHtml(context.getString(R.string.item_rated_review, item.title), HtmlCompat.FROM_HTML_MODE_LEGACY)
            ratingDescription.text = item.rating!!.description
            userPublicId = Repository().getBuyerEmail(keyBuyerUser)
            userPublicId.observe(context, Observer {
                rater.text = HtmlCompat.fromHtml(context.getString(R.string.rater_user_review, it), HtmlCompat.FROM_HTML_MODE_LEGACY)
            })
            if (ratingDescription.text == "") {
                ratingDescription.visibility = View.GONE
            } else {
                ratingDescription.visibility = View.VISIBLE
                val act = context.activity as MainActivity
                if (act.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    ratingDescription.maxLines = 3
                ratingDescription.setOnClickListener {
                    if (ratingDescription.ellipsize == null) {
                        val act = context.activity as MainActivity
                        if (act.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            ratingDescription.maxLines = 2
                        } else {
                            ratingDescription.maxLines = 3
                        }
                        ratingDescription.ellipsize = TextUtils.TruncateAt.END
                    } else {
                        ratingDescription.maxLines = Int.MAX_VALUE
                        ratingDescription.ellipsize = null
                    }
                }
            }
        }
    }
}