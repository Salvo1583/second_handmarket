package it.polito.mad.group18.second_handmarket.notifications

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.ui.home.HomeAdapter
import it.polito.mad.group18.second_handmarket.ui.home.HomeViewModel
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM

class NotificationFragment : Fragment() {

    val ownVM : NotificationVIewModel by viewModels()
    val sharedVM : SharedVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.notification, container, false)
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //This element is setted to show the message when the list is empty
        val t : TextView = view.findViewById(R.id.text_hideN)
        t.visibility = View.INVISIBLE
        //This following instruction is needed to show the FAB button, because it can be hidden
        (activity as MainActivity).findViewById<FloatingActionButton>(R.id.fab).hide()

        ownVM.getMessage(sharedVM.userId).observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()) {
                val myMessages = ownVM.filterMessages(sharedVM.userId).sortedByDescending{ it.date }
                if(myMessages.isNotEmpty()){
                val rv: RecyclerView = view.findViewById(R.id.rvN)
                rv.adapter = NotificationAdapter(myMessages,this)
                rv.layoutManager = LinearLayoutManager(context)
                t.visibility = View.INVISIBLE
            }

            }
            else
                t.visibility = View.VISIBLE
        })



    }

}