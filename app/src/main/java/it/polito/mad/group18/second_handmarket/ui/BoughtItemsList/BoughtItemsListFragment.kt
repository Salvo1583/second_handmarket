package it.polito.mad.group18.second_handmarket.ui.BoughtItemsList

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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import kotlinx.android.synthetic.main.app_bar_main.*

class BoughtItemsListFragment: Fragment() {

    val sharedVM : SharedVM by activityViewModels()

    private val ownVM : BoughtItemsListVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if(sharedVM.userId == "")
            findNavController().navigate(R.id.action_nav_home_to_authentication)


        (activity as MainActivity).supportActionBar!!.show()
        (activity as MainActivity).fab.imageTintList = ColorStateList.valueOf(Color.BLACK)
        (activity as MainActivity).fab.hide()
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //This element is setted to show the message when the list is empty
        val recView: RecyclerView = view.findViewById(R.id.rv)
        val t : TextView = view.findViewById(R.id.text_hide)
        //This following instruction is needed to show the FAB button, because it can be hidden
        (activity as MainActivity).findViewById<FloatingActionButton>(R.id.fab).hide()

        ownVM.getBoughtItemsList(sharedVM.userId).observe(viewLifecycleOwner, Observer { list ->
            if(list.isNotEmpty()) {
                t.visibility = View.INVISIBLE
                ownVM.getBoughtItemsObjects(list).observe(viewLifecycleOwner, Observer {

                    Log.d("nestedObserve", "dovrei avere degli item disponibili")
                    /*val rv: RecyclerView = view.findViewById(R.id.rv)
                    rv.adapter = ItemsOfInterestListFragmentAdapter(it, this)
                    rv.layoutManager = LinearLayoutManager(context)*/
                    if (it.isNotEmpty()) {
                        if(recView.adapter == null)
                        {
                            Log.d("recycleView", "NON Sto aggiornando la lista")
                            recView.adapter = BoughtItemsListAdapter(it, this,recView)
                            recView.layoutManager = LinearLayoutManager(context)
                            t.visibility = View.INVISIBLE
                        }
                        else
                        {
                            Log.d("recycleView", "Sto aggiornando la lista")
                            val adapt = recView.adapter as BoughtItemsListAdapter
                            adapt.itemsSet(it)
                            t.visibility = View.INVISIBLE
                        }
                    }
                    else
                        t.visibility = View.VISIBLE
                })
            }else
                t.visibility = View.VISIBLE
        })

    }



}