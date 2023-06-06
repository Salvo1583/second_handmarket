package it.polito.mad.group18.second_handmarket

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color.*
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.group18.second_handmarket.interestedUserList.InterestedUserListAdapter
import it.polito.mad.group18.second_handmarket.model.UserID
import it.polito.mad.group18.second_handmarket.viewModels.ItemDetailsVM
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.dialog_rating.*
import kotlinx.android.synthetic.main.item_description.*
import kotlinx.android.synthetic.main.item_description.buyerUser
import kotlinx.android.synthetic.main.item_description.categoryField
import kotlinx.android.synthetic.main.item_description.descriptionField
import kotlinx.android.synthetic.main.item_description.expireField
import kotlinx.android.synthetic.main.item_description.priceField
import kotlinx.android.synthetic.main.item_description.rating_description
import kotlinx.android.synthetic.main.item_description.subCategoryField
import kotlinx.android.synthetic.main.item_description.titleField
import kotlinx.android.synthetic.main.item_details_fragment.*
import kotlinx.android.synthetic.main.item_details_fragment.photo_annuncio
import kotlinx.android.synthetic.main.item_edit_fragment.*
import kotlinx.android.synthetic.main.rating_card.*
import java.io.ByteArrayInputStream


class ItemDetailsFragment() : Fragment(), OnMapReadyCallback {

    private val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    private var mapView: MapView? = null
    private var gmap: GoogleMap? = null
    private val sharedVM: SharedVM by activityViewModels()

    private var long: Double=0.0
    private var lat : Double= 0.0

    private val itemVM: ItemDetailsVM by viewModels()

    private lateinit var ratingDialog: AlertDialog
    private lateinit var layoutRating: View

    private fun createRatingDialog() {
        layoutRating = layoutInflater.inflate(R.layout.dialog_rating, null)
        ratingDialog = this.let {
            val builder = AlertDialog.Builder(activity as MainActivity)
            builder.setView(layoutRating)
            builder.setCancelable(false)
            builder.create()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (sharedVM.userId == "")
            findNavController().navigate(R.id.action_nav_home_to_authentication)

        val vista = inflater.inflate(R.layout.item_details_fragment, container, false)

        var mapViewBundle: Bundle? = null
        mapView = vista.findViewById(R.id.map_view)
        Log.d("mappa","map: ${mapView}")
        mapView!!.onCreate(mapViewBundle)


        (activity as MainActivity).findViewById<FloatingActionButton>(R.id.fab).imageTintList =
            ColorStateList.valueOf(BLACK)

        setHasOptionsMenu(true)
        return vista
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).fab.hide()
        (activity as MainActivity).supportActionBar!!.show()
        createRatingDialog()
        val itemId = requireArguments().getString("group-18-keyCardSelected")
        val userId = arguments?.getString("group-18-itemUserId")
        val recView: RecyclerView = view.findViewById(R.id.rv_ID)
        itemCreator.paint?.isUnderlineText = true
        itemCreator.setOnClickListener {
            var boundle = Bundle()
            boundle.putString("showProfileUserId", userId!!)
            findNavController().navigate(
                R.id.action_itemDetailsFragment_to_showProfileFragment,
                boundle
            )
        }
        buyerUser.setOnClickListener {
            var boundle = Bundle()
            boundle.putString("showProfileUserId", itemVM.buyerUserId.value)
            findNavController().navigate(
                R.id.action_itemDetailsFragment_to_showProfileFragment,
                boundle
            )
        }

        if (userId != sharedVM.userId) {

            recView.visibility = View.GONE
            interestedUserTitle.visibility = View.GONE
            //divider4.visibility = View.GONE
            divider5.visibility = View.GONE

            (activity as MainActivity).fab.show()

            (activity as MainActivity).fab.setImageResource(R.drawable.ic_favorite_border_black_24dp)

            (activity as MainActivity).fab.setOnClickListener { view ->

                (activity as MainActivity).fab.setImageResource(R.drawable.ic_favorite_black_24dp)
                (activity as MainActivity).findViewById<FloatingActionButton>(R.id.fab).imageTintList =
                    ColorStateList.valueOf(YELLOW)
                itemVM.setInscription(itemId!!, sharedVM.userId, sharedVM.userEmail)

                //CLOUD MESSAGING
                val topic : String = itemId + "_changes"
                val msg = "New interest from ${sharedVM.userEmail}"
                FirebaseMessaging.getInstance().subscribeToTopic(topic)


                itemVM.sendNotifications(
                    sharedVM.userId,
                    msg,
                    "interest"
                )

                //CLOUD MESSAGING
                (activity as MainActivity).sendMessage(itemId+"_n", "Second-Hand Market", msg)

                Snackbar.make(
                    view,
                    "The owner has been notified, see Message Center for updates",
                    Snackbar.LENGTH_LONG
                ).setAction("Action", null).show()
            }
        } else {
            (activity as MainActivity).fab.hide()
        }

        rate_button.setOnClickListener {
            if (itemVM.rating.value == null)
                itemVM.createEmptyRating()
            ratingDialog.show()
            layoutRating.findViewById<RatingBar>(R.id.ratingBar).rating = if (itemVM.rating.value == null) 0f else itemVM.rating.value!!.value.toFloat()
            layoutRating.findViewById<EditText>(R.id.rating_text).setText(if (itemVM.rating.value == null) "" else itemVM.rating.value!!.description)
            layoutRating.findViewById<Button>(R.id.cancel_rate).setOnClickListener {
                itemVM.discardTempRating()
                ratingDialog.cancel()
            }
            layoutRating.findViewById<Button>(R.id.confirm_rate).setOnClickListener {
                if (itemVM.rating.value!!.value == 0.0) {
                    Toast.makeText(
                        activity as MainActivity,
                        "Please select a value from star rating.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    itemVM.saveRating(itemVM.itemId.value!!)
                    val msg = "Thanks For the Item, I have already Review It!"
                    itemVM.sendNotifications(
                        sharedVM.userId,
                        msg,
                        "review"
                    )
                    //CLOUD MESSAGING
                    (activity as MainActivity).sendMessage(itemVM.userId.value!!, "Second-Hand Market", msg)
                    ratingDialog.cancel()
                }
            }
            layoutRating.findViewById<RatingBar>(R.id.ratingBar)
                .setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                    run {
                        if (fromUser) {
                            itemVM.rating.value!!.value = rating.toDouble()
                        }
                    }
                }
            layoutRating.findViewById<EditText>(R.id.rating_text)
                .addTextChangedListener(object :
                    TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
//                                TODO("Not yet implemented")
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
//                                TODO("Not yet implemented")
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        itemVM.rating.value!!.description = s.toString()
                    }
                })

        }

        itemVM.getItemDetails(itemId!!).observe(viewLifecycleOwner, Observer { item ->
            if (item.loaded) {
                itemVM.setItem()
                lat= itemVM.coordinates.value!!.get(0)
                long= itemVM.coordinates.value!!.get(1)

                mapView = view.findViewById(R.id.map_view)
                mapView!!.getMapAsync(this)

                itemVM.getItemUSer(item.userId).observe(viewLifecycleOwner, Observer {
                    if (it.loaded) {
                        itemCreator.text = it.email
                    }
                })
                itemVM.getInterestedUserForItem(item.itemId).observe(viewLifecycleOwner, Observer {
                    if (it.isNotEmpty() && (arguments?.get("group-18-itemUserId") != sharedVM.userId)) {
                        if (it.contains(UserID(sharedVM.userId, sharedVM.userEmail))) {

                            (activity as MainActivity).fab.setImageResource(R.drawable.ic_favorite_black_24dp)
                            (activity as MainActivity).findViewById<FloatingActionButton>(R.id.fab).imageTintList =
                                ColorStateList.valueOf(YELLOW)
                            (activity as MainActivity).fab.setOnClickListener { view -> }
                        }

                    } else {
                        if (it.isNotEmpty()) {
                            recView.adapter = InterestedUserListAdapter(it, this)
                            recView.layoutManager = LinearLayoutManager(context)
                            interestedUserTitle.text = "Interested Users"
                            interestedUserTitle.setTextColor(parseColor("#212121"))
                            if(itemVM.status.value!="Sold"){
                                rv_ID.visibility = View.VISIBLE
                                divider5.visibility = View.VISIBLE
                            }

                        } else {

                            interestedUserTitle.text = "No Interested Users"
                            interestedUserTitle.setTextColor(parseColor("#C10404"))
                            rv_ID.visibility = View.GONE
                            // divider5.visibility = View.GONE
                        }
                    }

                })

                itemVM.rating.observe(viewLifecycleOwner, Observer {
                    if (it != null && it.value != 0.0) {
                        if (itemVM.isRatingFromDb(it)) {
                            ratingBar2.rating = it.value.toFloat()
                            if (it.description != "")
                                rating_description.text = it.description
                            rating_values.visibility = View.VISIBLE
                            rate_button.visibility = View.GONE
                        } else {
                            rate_button.visibility = View.VISIBLE
                            rate_button.performClick()
                        }
                    } else
                        if (itemVM.isItemBuyer(itemVM.itemId.value!!, sharedVM.userId)) {
                            rate_button.visibility = View.VISIBLE
                        }
                })

                // Da migliorare
                /*status.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr= ${itemVM.coordinates.value!![0]}, ${itemVM.coordinates.value!![1]}"))
                    startActivity(intent)
                }*/

            }
        })

        itemVM.itemTitle.observe(viewLifecycleOwner, Observer { titleField.text = it })
        itemVM.category.observe(viewLifecycleOwner, Observer { categoryField.text = "$it" })
        itemVM.price.observe(viewLifecycleOwner, Observer { priceField.text = "€ $it" })
        itemVM.location.observe(viewLifecycleOwner, Observer { locationField.text = "$it" })
        itemVM.description.observe(viewLifecycleOwner, Observer { descriptionField.text = "$it" })
        itemVM.subCategory.observe(viewLifecycleOwner, Observer { subCategoryField.text = "$it" })
        itemVM.expireDate.observe(viewLifecycleOwner, Observer { expireField.text = "$it" })
        itemVM.buyerUserId.observe(viewLifecycleOwner, Observer {
            if(it!=""){
                //rate_button.visibility = View.VISIBLE

                itemVM.getBuyerEmail(itemVM.buyerUserId.value!!).observe(viewLifecycleOwner, Observer { email->
                    if(email!=null){
                        Log.d("buyerEmail","L'email dell'user è $email")
                        buyerUser.text = email
                    }
                })
            }
        })

        itemVM.status.observe(viewLifecycleOwner, Observer {

            if (it == "Available") {
                rating_values.visibility = View.GONE
                rate_button.visibility = View.GONE
                status.setTextColor(parseColor("#00b300"))
                if (arguments?.get("group-18-itemUserId") != sharedVM.userId)
                    (activity as MainActivity).fab.show()
            } else if (it == "Unavailable") {
                rating_values.visibility = View.GONE
                rate_button.visibility = View.GONE
                status.setTextColor(parseColor("#ff6600"))
                if (arguments?.get("group-18-itemUserId") != sharedVM.userId)
                    (activity as MainActivity).fab.show()
            } else if(it=="Sold") { // se l'oggetto è stato venduto

                if(userId == sharedVM.userId){
                    Log.d("buyerEmail","L'email dell'user è")
                    interestedUserTitle.visibility = View.GONE
                    buyerLayout2.visibility = View.VISIBLE
                    rv_ID.visibility = View.GONE

                }

                (activity as MainActivity).invalidateOptionsMenu()
                //rate_button.visibility = View.VISIBLE
                divider5.visibility = View.VISIBLE
                status.setTextColor(parseColor("#C10404"))
                if (arguments?.get("group-18-itemUserId") != sharedVM.userId)
                    (activity as MainActivity).fab.hide()
            }

            status.text = it
        })


        itemVM.getImage(itemId!!)

        itemVM.image.observe(viewLifecycleOwner, Observer {

            if (it.isNotEmpty()) {
                if(it.size > 2) {
                    progressBar2.visibility = View.INVISIBLE
                    photo_annuncio.visibility = View.VISIBLE
                    val arrayInputStream = ByteArrayInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(arrayInputStream)
                    photo_annuncio.setImageBitmap(imageBitmap)
                }
                else
                {
                    if(it.size == 1)
                    {
                        //L'immagine non esiste
                        progressBar2.visibility = View.INVISIBLE
                        photo_annuncio.visibility = View.VISIBLE
                        photo_annuncio.setImageResource(R.drawable.photo_default)
                    }
                    else if (it.size == 2)
                    {
                        //l'immagine esiste ma non la riesce a scaricare'
                        progressBar2.visibility = View.VISIBLE
                        photo_annuncio.visibility = View.INVISIBLE
                    }

                }

            } else {
                progressBar2.visibility = View.VISIBLE
                photo_annuncio.visibility = View.INVISIBLE
            }
        })
    }

    override fun onResume() {
        mapView!!.onResume()
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_add_details_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menu.removeItem(R.id.action_settings)
        if (arguments?.get("group-18-itemUserId") != sharedVM.userId || itemVM.status.value=="Sold") {
            menu.removeItem(R.id.edit_item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_item -> {

                if (itemVM.itemId.value != "") {
                    val bundle = Bundle()
                    bundle.putString("group-18-keyCardSelected", itemVM.itemId.value.toString())
                    bundle.putString("group-18-direction", "ItemDetails")
                    view?.findNavController()
                        ?.navigate(R.id.action_itemDetailsFragment_to_itemEditFragment, bundle)
                }
            }
            else -> {
            }
        }
        return false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("mappa","ciao")
        gmap = googleMap
        gmap!!.setMinZoomPreference(12f)
        //val pos = LatLng(itemVM.coordinates.value?.get(0)!!,itemVM.coordinates.value?.get(1)!!)
        gmap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        val pos = LatLng(lat, long)

        val googlePlex = CameraPosition.builder()
            .target(LatLng(lat, long))
            .zoom(10f)
            .bearing(0f)
            .tilt(45f)
            .build()
        gmap!!.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null)

        gmap!!.addMarker(
            MarkerOptions()
            .position(LatLng(lat, long))
            .title(itemVM.location.value))

        gmap!!.moveCamera(CameraUpdateFactory.newLatLng(pos))
    }


}

