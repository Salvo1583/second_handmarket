package it.polito.mad.group18.second_handmarket

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import it.polito.mad.group18.second_handmarket.viewModels.UserVM
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayInputStream

interface DrawerLocker {
    fun setDrawerLocked(shouldLock: Boolean)
}


class MainActivity : AppCompatActivity(), DrawerLocker {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var auth: FirebaseAuth
    lateinit var alertDialog: AlertDialog

    //SEZIONE PER GOOGLE CLOUD MESSAGING
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAdItygLs:APA91bHEGJBSJ85WylQ3c7HuuzuboP0fz6M56E9zttwdQa4gllwpFDFdCGOMWVzQGItmE7l_WQjqu0Hl7ml1L9XNJM6NOUb0peJJiBeOZtqSpZcBvwAjyDJZsCfk0yAtAq5J_WI7CPSB"
    private val contentType = "application/json"
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        val viewModel = ViewModelProvider(this).get(SharedVM::class.java)
        val userVM = ViewModelProvider(this).get(UserVM::class.java)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setIcon(R.drawable.ic_thriftville_icon_white)
        auth = FirebaseAuth.getInstance()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.imageTintList = ColorStateList.valueOf(Color.BLACK)
        Navigation.setViewNavController(fab, findNavController(R.id.nav_host_fragment))
        fab.setOnClickListener { view ->
            val bundle = Bundle()
            bundle.putString("group-18-direction", "FAB")
            bundle.putString("group-18-itemUserId", viewModel.userId)
            Navigation.findNavController(view)
                .navigate(R.id.action_myItemList_to_itemEditFragment, bundle)

        }
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.showProfileFragment,
                R.id.authentication,
                R.id.myItemList,
                R.id.notificationFragment,
                R.id.itemsOfInterestListFragment,
                R.id.boughtItemsListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        setDrawerLocked(true)

        //For the navigation Drawer profile
        if (auth.currentUser != null) {
           viewModel.userId = auth.currentUser!!.uid
           viewModel.userEmail = auth.currentUser!!.email!!
           userVM.getImage(viewModel.userId)
           val header = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
           userVM.image.observe(this, Observer {
               if (it.isNotEmpty() && it.size > 2) {
                   val arrayInputStream = ByteArrayInputStream(it)
                   header.findViewById<ImageView>(R.id.drawer_image)
                       .setImageBitmap(BitmapFactory.decodeStream(arrayInputStream))
               }
           })

          userVM.getUserInfo(viewModel.userId).observe(this, Observer {
                if (it.loaded) {
                    header.findViewById<TextView>(R.id.drawer_title).text = it.fullName
                    header.findViewById<TextView>(R.id.drawer_email).text = it.email
                }
            })

           //navigazione da drawe a profilo
           //val header = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)
           header.findViewById<ImageView>(R.id.drawer_image).setOnClickListener {
               var boundle = Bundle()
               boundle.putString("showProfileUserId", viewModel.userId)
               navController.navigate(
                   R.id.showProfileFragment,
                   boundle
               )
               val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
               drawer.closeDrawer(GravityCompat.START)
           }


            //Google Cloud Messaging:notifcation tap
            if(intent.extras != null)
            {
                if(intent.extras!!["notification"] != null && intent.extras!!["notification"] == "Yes" && viewModel.startFrom == "") {
                    viewModel.startFrom = "notification"
                    intent.extras!!["notification"] == null
                    Log.d("INTENTTRY", "VALORE: ${intent.extras}")
                }
            }

/*
           viewModel.getMyItemList().observe(this, Observer {
               if(it.isNotEmpty())
               {
                   for(elements in it)
                       FirebaseMessaging.getInstance().subscribeToTopic(elements.itemId+"_n")
               }
           })
*/
        } else {
            //Google Cloud Messaging:notifcation tap
            if(intent.extras != null)
            {
                if(intent.extras!!["notification"] != null && intent.extras!!["notification"] == "Yes" && viewModel.startFrom == ""){
                    viewModel.startFrom="notLogged"
                    navController.navigate(R.id.action_nav_home_to_authentication)
                }
            }

        }


    }

    public override fun onStart() {
        super.onStart()
        //Google Cloud Messaging:notifcation tap
        /*
        if(intent.extras != null && auth.currentUser != null)
        {
            if(intent.extras!!["notification"] != null && intent.extras!!["notification"] == "Yes")
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_nav_home_to_notificationFragment)
        }
        */

    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        hideKeyboard()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    }

    fun hideKeyboard() {
        val v: View? = this.currentFocus
        v?.let { view ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun setDrawerLocked(shouldLock: Boolean) {
        if (shouldLock) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    //SEZIONE PER GOOGLE CLOUD MESSAGING
    fun sendMessage(topicReal : String, title:String, message:String, buyer : String = "") {
        val topic = "/topics/$topicReal" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notificationBody = JSONObject()

        try {
            notificationBody.put("title", "ThriftVille")
            notificationBody.put("message", message)   //Enter your notification message
            notification.put("to", topic)
            notification.put("data", notificationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotification(notification)
    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
                //msg.setText("")
            },
            Response.ErrorListener {
                Toast.makeText(this@MainActivity, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    fun lockDeviceRotation(value: Boolean, auth : Boolean = false) {
        requestedOrientation = if (value) {
            val currentOrientation = resources.configuration.orientation
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(auth)
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                else
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            } else {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }
        }
    }

}


