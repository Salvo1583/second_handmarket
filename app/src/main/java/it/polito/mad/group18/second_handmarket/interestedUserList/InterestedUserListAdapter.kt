package it.polito.mad.group18.second_handmarket.interestedUserList

import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.auth.User
import it.polito.mad.group18.second_handmarket.ItemDetailsFragment
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.model.Repository
import it.polito.mad.group18.second_handmarket.model.UserID
import it.polito.mad.group18.second_handmarket.ui.myItemsList.MyItemList
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*

class InterestedUserListAdapter(var adv: MutableList<UserID>, val ctx: Fragment) :
    RecyclerView.Adapter<InterestedUserListAdapter.InterestedUserListVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestedUserListVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.interested_user_list, parent, false)

        return InterestedUserListVH(v)
    }

    override fun getItemCount(): Int = adv.size

    override fun onBindViewHolder(holder: InterestedUserListVH, position: Int) {

        holder.bind(adv[position], ctx)
    }

    class InterestedUserListVH(v: View) : RecyclerView.ViewHolder(v) {
        val email: TextView = v.findViewById(R.id.email_IU)

        fun bind(u: UserID, ctx: Fragment) {
            val listContext = ctx as ItemDetailsFragment
            email.paint?.isUnderlineText = true
            email.text = u.email
            email.setOnClickListener {
                var boundle = Bundle()
                boundle.putString("showProfileUserId", u.userId)
                listContext.findNavController()
                    .navigate(R.id.action_itemDetailsFragment_to_showProfileFragment, boundle)
            }
        }


    }


}