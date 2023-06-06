package it.polito.mad.group18.second_handmarket

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import com.google.android.material.textfield.TextInputLayout.INVISIBLE
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rtchagas.pingplacepicker.PingPlacePicker
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import it.polito.mad.group18.second_handmarket.viewModels.UserVM
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.item_edit_fragment.*
import kotlinx.android.synthetic.main.profile_edit_fragment.*
import kotlinx.android.synthetic.main.profile_edit_fragment.addImageButton
import kotlinx.android.synthetic.main.profile_edit_fragment.background
import kotlinx.android.synthetic.main.profile_edit_fragment.emailField
import kotlinx.android.synthetic.main.profile_edit_fragment.fullNameField
import kotlinx.android.synthetic.main.profile_edit_fragment.guideline8
import kotlinx.android.synthetic.main.profile_edit_fragment.imageProfile
import kotlinx.android.synthetic.main.profile_edit_fragment.locationField
import kotlinx.android.synthetic.main.profile_edit_fragment.nickField
import kotlinx.android.synthetic.main.profile_edit_fragment.passwordField
import kotlinx.android.synthetic.main.profile_edit_fragment.phoneField
import kotlinx.android.synthetic.main.profile_edit_fragment.register
import org.json.JSONObject
import java.io.*

class EditProfileFragment : Fragment() {
    private var profileImage: Bitmap? = null
    private var profileImageByte: ByteArray? = null
    private lateinit var auth: FirebaseAuth
    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private val sharedVM: SharedVM by activityViewModels()
    private val userVM: UserVM by viewModels()

    private var registration: Boolean? = null
    private var googleSI : String? = null

    private var layout_tot : ConstraintLayout? = null
    private var progressBarGoogle : ProgressBar? = null


    companion object {
        private val PERMISSION_CODE_CAMERA = 1000
        private val PERMISSION_CODE_GALLERY = 1001
        private val IMAGE_PICK_GALLERY = 10
        private val REQUEST_IMAGE_CAPTURE = 11
        private val pingActivityRequestCode = 101
        var image_uri: Uri? = null
    }

    private fun saveProfile() {


        if (validateForm()) {
            background.visibility = View.INVISIBLE
            progressBarPE.visibility = View.VISIBLE

            userVM.updateUser(
                sharedVM.userId,
                fullNameField.text.toString(),
                nickField.text.toString(),
                phoneField.text.toString(),
                emailField.text.toString(),
                locationField.text.toString(),
                profileImageByte!!,
                userVM.imageChange
            )

            if(userVM.imageChange) {

                userVM.loadImage.observe(viewLifecycleOwner, Observer {
                    if (it != null) {

                        //Used for modify run-time the value of the Header of the Navigation Drawer
                        val header =
                            (activity as MainActivity).findViewById<NavigationView>(R.id.nav_view)
                                .getHeaderView(0)
                        header.findViewById<TextView>(R.id.drawer_title).text =
                            userVM.fullName.value
                        header.findViewById<TextView>(R.id.drawer_email).text = userVM.email.value
                        if(it == true)
                        header.findViewById<ImageView>(R.id.drawer_image).setImageBitmap(profileImage!!)

                        Toast.makeText(
                            activity as MainActivity,
                            "Profile Data Correctly uploaded",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (it == false) {
                            Toast.makeText(
                                activity as MainActivity,
                                "Image Profile not uploaded due to a server error",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        if (registration == null || registration == false) {
                            if (googleSI != "True")
                                this.findNavController()
                                    .navigate(R.id.action_editProfileFragment_to_showProfileFragment2)
                            else
                                this.findNavController()
                                    .navigate(R.id.action_editProfileFragment_to_nav_home)
                        } else
                            this.findNavController()
                                .navigate(R.id.action_editProfileFragment_to_nav_home)

                        (activity as MainActivity).lockDeviceRotation(false)
                    }
                })
            }
            else {
                //Used for modify run-time the value of the Header of the Navigation Drawer
                val header =
                    (activity as MainActivity).findViewById<NavigationView>(R.id.nav_view)
                        .getHeaderView(0)
                header.findViewById<TextView>(R.id.drawer_title).text =
                    userVM.fullName.value
                header.findViewById<TextView>(R.id.drawer_email).text = userVM.email.value
                header.findViewById<ImageView>(R.id.drawer_image).setImageBitmap(profileImage)


                if (registration == null || registration == false) {
                    if (googleSI != "True")
                        this.findNavController()
                            .navigate(R.id.action_editProfileFragment_to_showProfileFragment2)
                    else
                        this.findNavController()
                            .navigate(R.id.action_editProfileFragment_to_nav_home)
                } else
                    this.findNavController()
                        .navigate(R.id.action_editProfileFragment_to_nav_home)

                (activity as MainActivity).lockDeviceRotation(false)
            }


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        auth = FirebaseAuth.getInstance()
        googleSI = arguments?.getString("GoogleSignIn")
        registration = arguments?.getBoolean("Registration")
        if(googleSI == "True" && userVM.googleDone == false)
        {
            //userVM.googleDone = true
            (activity as MainActivity).lockDeviceRotation(true)
            configureGoogleSignIn()
            signInWithGoogle()
        }
        else {


            if (registration == null || registration == false) {
                if(googleSI != "True")
                    sharedVM.userId = auth.currentUser!!.uid
            }
        }
        return inflater.inflate(R.layout.profile_edit_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar!!.show()
        userVM.fullName.observe(viewLifecycleOwner, Observer { fullNameField.setText(it) })
        userVM.nickName.observe(viewLifecycleOwner, Observer { nickField.setText(it) })
        userVM.phone.observe(viewLifecycleOwner, Observer { phoneField.setText(it) })
        userVM.email.observe(viewLifecycleOwner, Observer { emailField.setText(it) })
        userVM.location.observe(viewLifecycleOwner, Observer { locationField.setText(it) })

        layout_tot = view.findViewById(R.id.background)
        progressBarGoogle = view.findViewById(R.id.progressBarGoogle)

        if(googleSI == "True" && userVM.googleDone == false)
        {
            userVM.googleDone = true
            layout_tot!!.visibility = View.INVISIBLE
            progressBarGoogle!!.visibility = View.VISIBLE

        }else if(userVM.googleDone == true)
        {
            layout_tot!!.visibility = View.VISIBLE
            progressBarGoogle!!.visibility = View.INVISIBLE
        }

        if (registration == null || registration == false) {
            // disable editing of email and password field
            emailField.isFocusable = false
            emailField.isEnabled = false
            emailField.keyListener = null
            emailContainer.helperText = ""
            passwordField.isFocusable = false
            passwordField.isEnabled = false
            passwordField.keyListener = null
            emailContainer.endIconMode = END_ICON_NONE
            passwordContainer.endIconMode = END_ICON_NONE
            passwordContainer.visibility = View.GONE
            fullNameContainer.helperText = getString(R.string.help_fullname_inserted)
            // password and button not visible
            if(googleSI == "True")
            {
                (activity as MainActivity).supportActionBar!!.hide()
                register.setOnClickListener {
                    (activity as MainActivity).lockDeviceRotation(true)
                    completeRegistration()
                }
            }
            else {
                register.visibility = View.GONE
            }
            if ((activity as MainActivity).resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                guideline8.setGuidelinePercent(0.3f)

            if(googleSI != "True") {

                if (!userVM.loadedVM) {
                    userVM.getImage(sharedVM.userId)
                    userVM.getUserInfo(sharedVM.userId).observe(viewLifecycleOwner, Observer {
                        if (it.loaded) {
                            userVM.setUser()
                            userVM.loadedVM = true
                            //fullNameField.gravity = Gravity.CENTER;
                        }
                    })
                }
            }
        } else {
            (activity as MainActivity).supportActionBar!!.hide()
            register.setOnClickListener {
                (activity as MainActivity).lockDeviceRotation(true)
                createAccount(emailField.text.toString(), passwordField.text.toString())
            }
        }


        userVM.image.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                if(it.size > 2) {
                    profileImageByte = it
                    val arrayInputStream = ByteArrayInputStream(it)
                    this.profileImage = BitmapFactory.decodeStream(arrayInputStream)
                    imageProfile.setImageBitmap(profileImage)
                    progressBar5.visibility = View.INVISIBLE
                    imageProfile.visibility = View.VISIBLE
                }else
                {
                    if(it.size == 1)
                    {
                        //L'immagine non esiste
                        progressBar5.visibility = View.INVISIBLE
                        imageProfile.visibility = View.VISIBLE
                        imageProfile.setImageResource(R.drawable.male_profile_image)
                    }
                    else if (it.size == 2)
                    {
                        //l'immagine esiste ma non la riesce a scaricare'
                        profileImageByte = it
                        progressBar5.visibility = View.VISIBLE
                        imageProfile.visibility = View.INVISIBLE
                    }
                }
            } else {
                if (registration == null || registration == false) {
                    progressBar5.visibility = View.VISIBLE
                    imageProfile.visibility = View.INVISIBLE
                }

                if (googleSI == "True")
                {
                    progressBar5.visibility = View.INVISIBLE
                    imageProfile.visibility = View.VISIBLE
                }
            }
        })

        //Da aggiungere un bottone
        locationField.setOnClickListener {
            showPlacePicker()
        }

        locationField.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus)
                v.performClick()
        }

    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        (activity as MainActivity).menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_edit_menu, menu)
        menu.removeItem(R.id.action_settings)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        (activity as MainActivity).hideKeyboard()
        when (item.itemId) {
            R.id.save_prof -> {
                (activity as MainActivity).lockDeviceRotation(true)
                saveProfile()
            }
            else -> {
            }
        }
        return false
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.photo -> {
                openCamera()
            }
            R.id.gallery -> {
                openGallery()
            }
            else -> return super.onContextItemSelected(item)
        }
        return super.onContextItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        registerForContextMenu(addImageButton)
        addImageButton.setOnClickListener {
            (activity as MainActivity).openContextMenu(it)
        }
        registerForContextMenu(imageProfile)
        imageProfile.setOnClickListener {
            (activity as MainActivity).openContextMenu(it)
        }
    }

    override fun onPause() {
        super.onPause()
        userVM.fullName.value = fullNameField.text.toString()
        userVM.nickName.value = nickField.text.toString()
        userVM.phone.value = phoneField.text.toString()
        if (registration != null && registration!!)
            userVM.email.value = emailField.text.toString()
        userVM.location.value = locationField.text.toString()
        if (profileImageByte != null) {
            userVM.image.value = this.profileImageByte
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fab.hide()
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((activity as MainActivity).checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //ACCESSO NON CONSENTITO
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE_GALLERY)
            } else {
                //ACCESSO CONSENTITO
                pickImageFromGallery()
            }
        } else {
            //SISTEMA OPERATIVO TROPPO VECCHIO
            pickImageFromGallery()
        }
    }

    private fun openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((activity as MainActivity).checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                (activity as MainActivity).checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            ) {
                //ACCESSO NON CONSENTITO
                val permissions =
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permissions, PERMISSION_CODE_CAMERA)

            } else {
                //ACCESSO CONSENTITO
                pickImageFromCamera()
            }
        } else {
            // Sistema operativo troppo vecchio
            pickImageFromCamera()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_GALLERY -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery()
                } else {
                    Toast.makeText(
                        activity as MainActivity,
                        "Permission denied in getting image from gallery",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            PERMISSION_CODE_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromCamera()
                } else {
                    Toast.makeText(
                        activity as MainActivity,
                        "Permission denied in opening camera",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_GALLERY) {
            image_uri = data?.data
            launchImageCrop(image_uri)
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            launchImageCrop(image_uri)
        } else if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            userVM.imageChange = true
            val imageStream: InputStream =
                (activity as MainActivity).contentResolver.openInputStream(result.uri!!)!!
            profileImage = BitmapFactory.decodeStream(imageStream)
            imageStream.close()
            imageProfile.setImageBitmap(profileImage)
            val imageByteStream = ByteArrayOutputStream()
            profileImage!!.compress(Bitmap.CompressFormat.PNG, 100, imageByteStream)
            profileImageByte = imageByteStream.toByteArray()
            userVM.image.value = profileImageByte
            imageByteStream.close()
        }else if( (requestCode == pingActivityRequestCode) && (resultCode == Activity.RESULT_OK)){
            val place: Place? = PingPlacePicker.getPlace(data!!)
            userVM.userPlace.value = place
            locationField.setText(place!!.address.toString())
            userVM.userLatitude.value = place!!.latLng!!.latitude
            userVM.userLongitude.value = place.latLng!!.longitude
        }else if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("kkk", "1")
                userVM.getUserWithEmail(account!!.email!!).observe(viewLifecycleOwner, Observer { string ->
                    if(string == "Present") {
                        Log.d("kkk", "2")
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        auth.signInWithCredential(credential).addOnCompleteListener {
                            if (it.isSuccessful) {
                                //startActivity(HomeActivity.getLaunchIntent(this))
                                Log.d("kkk", "createUserWithEmail2:success")
                                val user = auth.currentUser
                                sharedVM.userId = user!!.uid
                                sharedVM.userEmail = user!!.email!!
                                /* userVM.updateUser(
                                     sharedVM.userId,
                                     fullNameField.text.toString(),
                                     nickField.text.toString(),
                                     phoneField.text.toString(),
                                     emailField.text.toString(),
                                     locationField.text.toString(),
                                     profileImageByte!!
                                 )
                                 */
                                Log.d("kkk", "createUserWithGoogle2:SUCCESS ${user!!.uid}")
                                (activity as MainActivity).lockDeviceRotation(false)
                                findNavController().navigate(R.id.action_editProfileFragment_to_nav_home)
                            } else {
                                Toast.makeText(activity as MainActivity, "Google sign in failed:(", Toast.LENGTH_LONG).show()
                            }
                        }

                    }
                    else if(string == "Not_Present") {
                        (activity as MainActivity).lockDeviceRotation(false)
                        Log.d("kkk", "3")
                        layout_tot!!.visibility = View.VISIBLE
                        progressBarGoogle!!.visibility = View.INVISIBLE
                        firebaseAuthWithGoogle(account)
                    }
                })


            } catch (e: ApiException) {
                Toast.makeText(activity as MainActivity, "Google sign in failed:( : ${e.message}", Toast.LENGTH_LONG).show()
                //if(e.message!!.contains("12501") )
                findNavController().navigate(R.id.action_editProfileFragment_to_authentication)
            }
        }
    }

    private fun launchImageCrop(uri: Uri?) {
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(150, 150)
            .setCropShape(CropImageView.CropShape.RECTANGLE)// default is rectangle
            .start(activity as MainActivity, this)
    }

    private fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY)
    }

    private fun pickImageFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New pictures")
        values.put(MediaStore.Images.Media.DESCRIPTION, "from the camera")
        image_uri = (activity as MainActivity).contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun createAccount(email: String, password: String) {
        Log.d("kkk", "createAccount:$email")
        if (!validateForm()) {
            return
        }

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(activity as MainActivity) { task ->
                // Sign in success, update UI with the signed-in user's information
                Log.d("kkk", "createUserWithEmail:success")
                val user = auth.currentUser
                sharedVM.userId = user!!.uid
                sharedVM.userEmail = user!!.email!!
                userVM.updateUser(
                    sharedVM.userId,
                    fullNameField.text.toString(),
                    nickField.text.toString(),
                    phoneField.text.toString(),
                    emailField.text.toString(),
                    locationField.text.toString(),
                    profileImageByte!!
                )
                Log.d("kkk", "createUserWithEmail:SUCCESS ${user!!.uid}")
                saveProfile()
                //updateUI(false)
            }
            .addOnFailureListener { ex ->
                Log.w("kkk", "createUserWithEmail:failure $ex")
                Toast.makeText(
                    activity as MainActivity, "Registration failed: ${ex.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        userVM.loadImage.observe(viewLifecycleOwner, Observer {})
        // [END create_user_with_email]

    }

    private fun validateForm(): Boolean {
        var valid = true

        if (profileImageByte == null) {
            Toast.makeText(
                activity as MainActivity,
                "Please insert your profile image",
                Toast.LENGTH_LONG
            ).show()
            valid = false
        }

        val email = emailField.text.toString()
        if (TextUtils.isEmpty(email)) {
            emailField.error = "Required."
            valid = false
            emailField.requestFocus()
        } else {
            emailField.error = null
        }

        if (registration == true) {
            val password = passwordField.text.toString()
            if (TextUtils.isEmpty(password)) {
                passwordField.error = "Required."
                valid = false
                passwordField.requestFocus()
            } else {
                passwordField.error = null
            }
        }

        val nickname = nickField.text.toString()
        if (TextUtils.isEmpty(nickname)) {
            nickField.error = "Required."
            valid = false
            nickField.requestFocus()
        } else {
            nickField.error = null
        }

        val fullname = fullNameField.text.toString()
        if (TextUtils.isEmpty(fullname)) {
            fullNameField.error = "Required."
            valid = false
            fullNameField.requestFocus()
        } else {
            fullNameField.error = null
        }

        val phone = phoneField.text.toString()
        if (TextUtils.isEmpty(phone)) {
            phoneField.error = "Required."
            valid = false
            phoneField.requestFocus()
        } else {
            phoneField.error = null
        }

        val geo = locationField.text.toString()
        if (TextUtils.isEmpty(geo)) {
            locationField.error = "Required."
            valid = false
        } else {
            locationField.error = null
        }

        if(valid == false){
            (activity as MainActivity).lockDeviceRotation(false)

        }


        return valid

    }

    private fun showPlacePicker() {

        val builder = PingPlacePicker.IntentBuilder()

        builder.setAndroidApiKey("AIzaSyBixzGJUct9RQDvFWQ8BHjWzHKa6tClTY0")
            .setMapsApiKey("AIzaSyBixzGJUct9RQDvFWQ8BHjWzHKa6tClTY0")

        // If you want to set a initial location
        // rather then the current device location.
        // pingBuilder.setLatLng(LatLng(37.4219999, -122.0862462))
        val placeIntent = builder.build(activity as MainActivity)
        startActivityForResult(placeIntent, pingActivityRequestCode)


    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity as MainActivity, mGoogleSignInOptions)
    }

    private fun signInWithGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        userVM.credentialGoogle = credential
        userVM.fullName.value = acct.displayName
        userVM.email.value = acct.email
        emailField.isEnabled = false
        emailField.isFocusable = false
        emailField.keyListener = null
        if(acct.photoUrl != null) {

            Glide.with(this)
                .asBitmap()
                .load(acct.photoUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        profileImage = resource
                        imageProfile.setImageBitmap(profileImage)
                        val imageByteStream = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.JPEG, 100, imageByteStream)
                        profileImageByte  = imageByteStream.toByteArray()
                        imageByteStream.close()
                        userVM.imageChange = true
                        //imageView.setImageBitmap(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }

                })
        }

    }

    private fun completeRegistration()
    {
        if (!validateForm()) {
            return
        }

        auth.signInWithCredential(userVM.credentialGoogle!!).addOnCompleteListener {
            if (it.isSuccessful) {
                //startActivity(HomeActivity.getLaunchIntent(this))
                Log.d("kkk", "createUserWithEmail1:success")
                val user = auth.currentUser
                sharedVM.userId = user!!.uid
                sharedVM.userEmail = user!!.email!!
               /* userVM.updateUser(
                    sharedVM.userId,
                    fullNameField.text.toString(),
                    nickField.text.toString(),
                    phoneField.text.toString(),
                    emailField.text.toString(),
                    locationField.text.toString(),
                    profileImageByte!!
                )
                */
                Log.d("kkk", "createUserWithGoogle1:SUCCESS ${user!!.uid}")
                saveProfile()
            } else {
                Toast.makeText(activity as MainActivity, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }
}