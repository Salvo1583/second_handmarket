package it.polito.mad.group18.second_handmarket.ui.rating

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_reviews.*

class RatingFragment : Fragment() {
    private val sharedVM: SharedVM by activityViewModels()
    private val ratingVM: RatingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        if (sharedVM.userId == "")
            findNavController().navigate(R.id.action_nav_home_to_authentication)
        (activity as MainActivity).supportActionBar!!.show()
//        (activity as MainActivity).fab.setImageResource(R.drawable.ic_update_black_24dp)
//        (activity as MainActivity).fab.imageTintList = ColorStateList.valueOf(Color.BLACK)
        (activity as MainActivity).fab.hide()
        return inflater.inflate(R.layout.fragment_reviews, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recView: RecyclerView = view.findViewById(R.id.ratingRV)
        val ratedUserId: String = requireArguments().getString("group18-ratings-userId") ?: ""
/*
        (activity as MainActivity).fab.setOnClickListener { view ->
            ratingVM.getItemOfUserWithRating(ratedUserId).observe(viewLifecycleOwner, Observer {
                if (it.isNotEmpty()){
                    if (recView.adapter == null) {
                        recView.adapter = RatingAdapter(it, this, true)
                        recView.layoutManager = LinearLayoutManager(context)
                    } else {
                        val adapt = recView.adapter as RatingAdapter
                        adapt.itemSet(it)
                    }
                }
            })
            Snackbar.make(view, "Update in progress...", Snackbar.LENGTH_LONG).show()
        }
*/
        ratingVM.getItemOfUserWithRating(ratedUserId).observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                recView.adapter = RatingAdapter(it, this, true)
                recView.layoutManager = LinearLayoutManager(context)
            }
        })
        ratingVM.getNumberRatingsOfUser(ratedUserId).observe(viewLifecycleOwner, Observer {
            numberOfRatings.text = if (it == 1) getString(R.string.number_rating) else getString(R.string.number_ratings, it)
        })
    }
}