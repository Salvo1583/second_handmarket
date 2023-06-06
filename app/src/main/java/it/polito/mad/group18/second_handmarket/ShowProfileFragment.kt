package it.polito.mad.group18.second_handmarket

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import it.polito.mad.group18.second_handmarket.viewModels.UserVM
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.profile_show_fragment.*
import java.io.ByteArrayInputStream

class ShowProfileFragment : Fragment(),OnMapReadyCallback {
    private var isMe: Boolean = false
    private var showUserId: String? = null

    private val sharedVM: SharedVM by activityViewModels()
    private val userVM: UserVM by viewModels()

    private var mapView: MapView? = null
    private var gmap: GoogleMap? = null
    private var long: Double=0.0
    private var lat : Double= 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (sharedVM.userId == ""){
            findNavController().navigate(R.id.action_nav_home_to_authentication)
        }
        setHasOptionsMenu(true)
        val vista = inflater.inflate(R.layout.profile_show_fragment, container, false)

        var mapViewBundle: Bundle? = null
        mapView = vista.findViewById(R.id.mapView2)
        Log.d("mappa","map: ${mapView}")
        mapView!!.onCreate(mapViewBundle)
        setHasOptionsMenu(true)
        return vista
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar!!.show()
        ratingUser.paint.isUnderlineText = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ratingUser.paint.underlineColor = R.color.colorAccent
        }
        showUserId = arguments?.getString("showProfileUserId") ?: sharedVM.userId
        isMe = showUserId == sharedVM.userId
        userVM.fullName.observe(viewLifecycleOwner, Observer { fullNameField.text = it })
        userVM.nickName.observe(viewLifecycleOwner, Observer { nickField.text = it })
        userVM.phone.observe(viewLifecycleOwner, Observer { phoneField.text = it })
        userVM.email.observe(viewLifecycleOwner, Observer { emailField.text = it })
        userVM.location.observe(viewLifecycleOwner, Observer { locationField.text = it })

        userVM.getImage(showUserId!!)

        userVM.image.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()){

                if(it.size > 2) {
                    val arrayInputStream = ByteArrayInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(arrayInputStream)
                    imageProfile.setImageBitmap(imageBitmap)
                    progressBar4.visibility = View.INVISIBLE
                    imageProfile.visibility = View.VISIBLE
                }
                else
                {
                    if(it.size == 1)
                    {
                        //L'immagine non esiste
                        progressBar4.visibility = View.INVISIBLE
                        imageProfile.visibility = View.VISIBLE
                        imageProfile.setImageResource(R.drawable.male_profile_image)
                    }
                    else if (it.size == 2)
                    {
                        //l'immagine esiste ma non la riesce a scaricare'
                        progressBar4.visibility = View.VISIBLE
                        imageProfile.visibility = View.INVISIBLE
                    }
                }
            }else{
                progressBar4.visibility = View.VISIBLE
                imageProfile.visibility = View.INVISIBLE
            }
        })

        userVM.numRatings.observe(viewLifecycleOwner, Observer {
            if (it > 0) {
                ratingUser.text = String.format("%.1f", userVM.ratingAvg.value)
                ratingUser.visibility = View.VISIBLE
            } else {
                ratingUser.visibility = View.INVISIBLE
            }
        })

        userVM.getUserInfo(showUserId!!).observe(viewLifecycleOwner, Observer {
            if(it.loaded){
                userVM.setUser()

                lat= userVM.userLatitude.value!!
                long= userVM.userLongitude.value!!
                mapView = view.findViewById(R.id.mapView2)
                mapView!!.getMapAsync(this)

                // set map opening on location fiedls
                if (it.latitude != 0.0 || it.longitude != 0.0) {
                    locationField.setOnClickListener {
                        openMapPosition(userVM.userLatitude.value!!, userVM.userLongitude.value!!)
                    }
                    locationIcon.setOnClickListener {
                        openMapPosition(userVM.userLatitude.value!!, userVM.userLongitude.value!!)
                    }
                }

                if (!isMe) {
                    userVM.obfuscatePhone()
                    fullNameField.visibility = View.INVISIBLE
                }
            }
        })

        ratingUser.setOnClickListener {
            val b = Bundle()
            b.putString("group18-ratings-userId", showUserId)
            findNavController().navigate(R.id.action_showProfileFragment_to_ratingFragment, b)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isMe) {
            inflater.inflate(R.menu.profile_show_menu, menu)
            menu.removeItem(R.id.action_settings)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.edit_prof -> {
                view?.findNavController()?.navigate(R.id.action_showProfileFragment_to_editProfileFragment)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
        (activity as MainActivity).fab.hide()
    }

    override fun onStart() {
        super.onStart()
        if(!isMe)
            (activity as MainActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as MainActivity).supportActionBar!!.show()
    }
    override fun onMapReady(googleMap: GoogleMap?) {
        gmap = googleMap
        gmap!!.setMinZoomPreference(12f)

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
                .title(userVM.location.value))

        gmap!!.moveCamera(CameraUpdateFactory.newLatLng(pos))
    }

    private fun openMapPosition(lat: Double, lng: Double) {
        val label = ""
        val uriBegin = "geo:$lat,$lng"
        val query: String = userVM.location.value!!
        val encodedQuery = Uri.encode(query)
        val place = "$uriBegin?q=$encodedQuery&z=16"
        //val place: String = String.format("geo:0,0?=%s,%s", lat, lng)
        //val gmmIntentUri = Uri.parse(uriBegin + Uri.encode(query))
        val gmmIntentUri: Uri = Uri.parse(place)
        var mapIntent: Intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity((activity as MainActivity).packageManager) != null)
            startActivity(mapIntent)
    }
}