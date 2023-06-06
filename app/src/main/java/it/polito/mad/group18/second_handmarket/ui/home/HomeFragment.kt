package it.polito.mad.group18.second_handmarket.ui.home

import android.app.SearchManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.model.Item
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import it.polito.mad.group18.second_handmarket.viewModels.UserVM
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.ByteArrayInputStream


class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    val sharedVM: SharedVM by activityViewModels()

    private val ownVM: HomeViewModel by viewModels()
    private val userVM : UserVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        auth = FirebaseAuth.getInstance()

        if(sharedVM.startFrom == "notification")
        {
            sharedVM.startFrom = "done"
            findNavController().navigate(R.id.action_nav_home_to_notificationFragment)
        }


        if (auth.currentUser == null) {
            (activity as MainActivity).lockDeviceRotation(true, true)
            findNavController().navigate(R.id.action_nav_home_to_authentication)
        }
        else {
            sharedVM.userId = auth.currentUser!!.uid
            sharedVM.userEmail = auth.currentUser!!.email!!
            userVM.getImage(sharedVM.userId)
            val header = (activity as MainActivity).findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
            userVM.image.observe(viewLifecycleOwner, Observer {
                if (it.isNotEmpty() && it.size > 2) {
                    val arrayInputStream = ByteArrayInputStream(it)
                    header.findViewById<ImageView>(R.id.drawer_image)
                        .setImageBitmap(BitmapFactory.decodeStream(arrayInputStream))
                }
            })

            userVM.getUserInfo(sharedVM.userId).observe(viewLifecycleOwner, Observer {
                if (it.loaded) {
                    header.findViewById<TextView>(R.id.drawer_title).text = it.fullName
                    header.findViewById<TextView>(R.id.drawer_email).text = it.email
                }
            })


            //navigazione da drawe a profilo
            //val header = (activity as MainActivity).findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
            header.findViewById<ImageView>(R.id.drawer_image).setOnClickListener {
                var boundle = Bundle()
                boundle.putString("showProfileUserId", sharedVM.userId)
                findNavController().navigate(
                    R.id.showProfileFragment,
                    boundle
                )
                val drawer = (activity as MainActivity).findViewById<DrawerLayout>(R.id.drawer_layout)
                drawer.closeDrawer(GravityCompat.START)
            }

            sharedVM.getMyItemList().observe(viewLifecycleOwner, Observer {
                if(it.isNotEmpty())
                {
                    for(elements in it)
                        FirebaseMessaging.getInstance().subscribeToTopic(elements.itemId+"_n")
                }
            })

            sharedVM.getInterestedItemsList().observe(viewLifecycleOwner, Observer {
                if(it.isNotEmpty())
                {
                    for(elements in it)
                        FirebaseMessaging.getInstance().subscribeToTopic(elements+ "_changes")
                }
            })

            FirebaseMessaging.getInstance().subscribeToTopic(sharedVM.userId)
        }

        (activity as MainActivity).supportActionBar!!.show()

        (activity as MainActivity).fab.imageTintList = ColorStateList.valueOf(Color.BLACK)

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //This element is setted to show the message when the list is empty
        val recView: RecyclerView = view.findViewById(R.id.rv)
        val t: TextView = view.findViewById(R.id.text_hide)
        t.visibility = View.INVISIBLE
        //This following instruction is needed to show the FAB button, because it can be hidden
        (activity as MainActivity).fab.show()

        (activity as MainActivity).fab.setImageResource(R.drawable.ic_update_black_24dp)

        (activity as MainActivity).fab.setOnClickListener { view ->

            ownVM.getItemOnSale(sharedVM.userId).observe(viewLifecycleOwner, Observer {
                if (it.isNotEmpty()) {
                    if(recView.adapter == null)
                    {
                        recView.adapter = HomeAdapter(it, this, true, recView)
                        recView.layoutManager = LinearLayoutManager(context)
                        t.visibility = View.INVISIBLE
                    }
                    else
                    {
                        val adapt = recView.adapter as HomeAdapter

                        adapt.itemsSet(it)
                        t.visibility = View.INVISIBLE
                    }
                }
            })
            Snackbar.make(
                view,
                "Update in progress...",
                Snackbar.LENGTH_LONG
            ).setAction("Action", null).show()

        }

        ownVM.getItemOnSale(sharedVM.userId).observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                recView.adapter = HomeAdapter(it, this, true, recView)
                recView.layoutManager = LinearLayoutManager(context)
                t.visibility = View.INVISIBLE
            } else
                t.visibility = View.VISIBLE


        })

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(
            R.menu.options_menu,
            menu
        ) // Put your search menu in "menu_search" menu file.

        val sv = menu.findItem(R.id.search).actionView as SearchView
        sv.isIconifiedByDefault = false
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        sv.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        sv.queryHint = "Search for Whatever"
        sv.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                sv.clearFocus()
                if (ownVM.itemOnSale.value!!.isNotEmpty()) {
                    val rv: RecyclerView = (activity as MainActivity).findViewById(R.id.rv)
                    val adapt = rv.adapter as HomeAdapter
                    ownVM.itemOnSale.value?.let { adapt.filterTitle(it, query.toLowerCase()) }

                }
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                //adapter.getFilter().filter(query)
                if (ownVM.itemOnSale.value!!.isNotEmpty()) {
                    val rv: RecyclerView = (activity as MainActivity).findViewById(R.id.rv)
                    val adapt = rv.adapter as HomeAdapter
                    if (query.isEmpty())
                        ownVM.itemOnSale.value?.let { adapt.itemsSet(it) }
                    else
                        ownVM.itemOnSale.value?.let { adapt.filterTitle(it, query.toLowerCase()) }

                    val t: TextView = (activity as MainActivity).findViewById(R.id.text_hide)

                    if(adapt.adv.isEmpty())
                        t.visibility = View.VISIBLE
                    else
                        t.visibility = View.INVISIBLE
                }

                return true
            }
        })

        // Define the listener
        val expandListener = object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                // Do something when action item collapses
                if ((ownVM.itemOnSale.value!!.isNotEmpty())) {
                    val rv: RecyclerView = (activity as MainActivity).findViewById(R.id.rv)
                    val adapt = rv.adapter as HomeAdapter
                    ownVM.itemOnSale.value?.let { adapt.itemsSet(it) }
                    sv.setQuery("", true)
                }
                (activity as MainActivity).hideKeyboard()
                return true // Return true to collapse action view
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                // Do something when expanded
                return true // Return true to expand action view
            }
        }

        // Get the MenuItem for the action item
        val actionMenuItem = menu?.findItem(R.id.search)

        // Assign the listener to that action item
        actionMenuItem?.setOnActionExpandListener(expandListener)

        // Any other things you have to do when creating the options menu...
        super.onCreateOptionsMenu(menu, inflater)
    }


}
