package it.polito.mad.group18.second_handmarket.ui.myItemsList

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.ui.home.HomeAdapter
import it.polito.mad.group18.second_handmarket.ui.home.HomeViewModel
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import kotlinx.android.synthetic.main.app_bar_main.*

class MyItemList: Fragment() {

    val sharedVM : SharedVM by activityViewModels()

    private val ownVM : HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(sharedVM.userId == "")
            findNavController().navigate(R.id.action_nav_home_to_authentication)


        (activity as MainActivity).supportActionBar!!.show()
        (activity as MainActivity).fab.imageTintList = ColorStateList.valueOf(Color.BLACK)
        (activity as MainActivity).fab.setImageResource(R.drawable.add_fab)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //This element is setted to show the message when the list is empty
        val t : TextView = view.findViewById(R.id.text_hide)
        //This following instruction is needed to show the FAB button, because it can be hidden
        (activity as MainActivity).findViewById<FloatingActionButton>(R.id.fab).show()

        (activity as MainActivity).fab.setOnClickListener { view ->
            val bundle = Bundle()
            bundle.putString("group-18-direction", "FAB")
            bundle.putString("group-18-itemUserId", sharedVM.userId)
            Navigation.findNavController(view).navigate(R.id.action_myItemList_to_itemEditFragment,bundle)

        }

        ownVM.getMyItemList(sharedVM.userId).observe(viewLifecycleOwner, Observer {

            if(it.isNotEmpty()) {
                t.visibility = View.INVISIBLE
                val rv: RecyclerView = view.findViewById(R.id.rv)
                rv.adapter = HomeAdapter(it, this, false, rv)
                rv.layoutManager = LinearLayoutManager(context)
                sharedVM.myItems.value = it
            }
            else
                t.visibility = View.VISIBLE



        })

    }




}