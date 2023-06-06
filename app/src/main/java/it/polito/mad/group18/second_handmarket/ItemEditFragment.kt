package it.polito.mad.group18.second_handmarket

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.rtchagas.pingplacepicker.PingPlacePicker
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import it.polito.mad.group18.second_handmarket.model.UserID
import it.polito.mad.group18.second_handmarket.viewModels.ItemEditVM
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.item_edit_fragment.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class ItemEditFragment : Fragment() {
    private var profileIm: Bitmap? = null
    private var profileByte: ByteArray = ByteArray(0)


    lateinit var button_add: ImageView
    lateinit var photo_button: ImageView

    companion object {
        private val PERMISSION_CODE_CAMERA = 1000
        private val PERMISSION_CODE_GALLERY = 1001
        private val IMAGE_PICK_GALLERY = 10
        private val REQUEST_IMAGE_CAPTURE = 11
        private val pingActivityRequestCode = 101
        var image_uri: Uri? = null
    }

    private val sharedVM: SharedVM by activityViewModels()

    private val itemVM: ItemEditVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        if (sharedVM.userId == "")
            findNavController().navigate(R.id.action_nav_home_to_authentication)

        setHasOptionsMenu(true);
        (activity as MainActivity).fab.imageTintList = ColorStateList.valueOf(Color.BLACK)
        return inflater.inflate(R.layout.item_edit_fragment, container, false)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        (activity as MainActivity).menuInflater.inflate(R.menu.context_menu, menu)
        super.onCreateContextMenu(menu, v, menuInfo)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var buyerAdapter: ArrayAdapter<String?> =
            ArrayAdapter<String?>((activity as MainActivity), android.R.layout.simple_spinner_item)
        buyerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buyerAdapter.addAll(itemVM.interestedUsers2.keys)
        buyerSpinner.adapter = buyerAdapter

        if (arguments?.get("group-18-keyCardSelected") != null) {
            if (!itemVM.loadedVM) {

                itemVM.getItemDetails(arguments?.get("group-18-keyCardSelected").toString())
                    .observe(viewLifecycleOwner, Observer {
                        if (it.loaded) {
                            if (it.status == "Sold") {
                                radioGroupStatus.visibility = View.GONE
                                statusTitleField.visibility = View.GONE
                                //layout_ItemEdit.removeView(statusTitleField)
                                //layout_ItemEdit.removeViewInLayout(buttonsLayout)
                            }
                            itemVM.setItem()
                            itemVM.getInterestedUserForItem(it.itemId)
                            itemVM.loadedVM = true
                        }
                    })
                Log.d("interestedUsers","sto per prendere gli interested users")


                itemVM.status.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        "Available" -> radioGroupStatus.check(availableButton.id)
                        //"Sold" -> radioGroupStatus.check(soldButton.id)
                        "Unavailable" -> radioGroupStatus.check(blockButton.id)
                    }
                })



            }

            if(itemVM.temporaryStatus=="Sold"){

                buyerLayout.visibility = View.VISIBLE
            }

            availableButton.setOnClickListener {

                itemVM.temporaryStatus = "Available"

                buyerLayout.visibility = View.GONE
            }

            soldButton.setOnClickListener {

                itemVM.temporaryStatus = "Sold"

                buyerLayout.visibility = View.VISIBLE

            }

            blockButton.setOnClickListener {

                itemVM.temporaryStatus = "Unavailable"

                buyerLayout.visibility = View.GONE
            }

            itemVM.getInterestedUserForItem(arguments?.get("group-18-keyCardSelected").toString())
                .observe(viewLifecycleOwner, Observer {
                    if(it.isNotEmpty()){
                        soldButton.visibility = View.VISIBLE

                        for (i in it){
                            //itemVM.interestedUsersEmail.add(i.email)
                            itemVM.interestedUsers2[i.email] = i.userId
                            Log.d("interestedUsers",i.email)
                        }
                        buyerAdapter.clear()
                        buyerAdapter.addAll(itemVM.interestedUsers2.keys)
                        buyerAdapter.notifyDataSetChanged();
                    }else{
                        soldButton.visibility = View.INVISIBLE
                    }
                })

            if (itemVM.status.value.toString() == "Sold") {
                radioGroupStatus.visibility = View.GONE
                statusTitleField.visibility = View.GONE

                //layout_ItemEdit.removeView(statusTitleField)
                //layout_ItemEdit.removeViewInLayout(buttonsLayout)
            }

        } else {
            radioGroupStatus.visibility = View.GONE
            statusTitleField.visibility = View.GONE
            //layout_ItemEdit.removeView(statusTitleField)
            //layout_ItemEdit.removeViewInLayout(buttonsLayout)

        }

        (activity as MainActivity).fab.hide()

        //inizzializzazione spinner
        var categoryAdapter: ArrayAdapter<String?> =
            ArrayAdapter<String?>((activity as MainActivity), android.R.layout.simple_spinner_item)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoryAdapter.addAll(itemVM.categoryL.getCategories())

        categoryField.adapter = categoryAdapter


        categoryField.setOnTouchListener { v, event ->
            (activity as MainActivity).hideKeyboard()
            false
        }

        var subCategoryAdapter: ArrayAdapter<String?> =
            ArrayAdapter<String?>((activity as MainActivity), android.R.layout.simple_spinner_item)
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subCategoryField.adapter = subCategoryAdapter


        subCategoryField.setOnTouchListener { v, event ->
            (activity as MainActivity).hideKeyboard()
            false
        }

        if (categoryField != null) {

            categoryField.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    subCategoryAdapter.clear()
                    subCategoryAdapter.addAll(itemVM.categoryL.getSubCategories(categoryField.selectedItem!!.toString()))
                    if (itemVM.subCategory.value != "") {
                        val spinnerPosition =
                            subCategoryAdapter.getPosition(itemVM.subCategory.value)
                        if (spinnerPosition != -1) {
                            subCategoryField.setSelection(spinnerPosition)
                        } else {
                            subCategoryField.setSelection(0)
                        }

                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }
        itemVM.title.observe(viewLifecycleOwner, Observer { titleFieldText.setText(it) })
        itemVM.category.observe(viewLifecycleOwner, Observer {

            if (it != "") {
                val spinnerPosition = categoryAdapter.getPosition(it)
                categoryField.setSelection(spinnerPosition)

            }
        })

        itemVM.subCategory.observe(viewLifecycleOwner, Observer {
            if (it != "") {
                val spinnerPosition = subCategoryAdapter.getPosition(it)
                subCategoryField.setSelection(spinnerPosition)
            }
        })



        itemVM.price.observe(viewLifecycleOwner, Observer { priceFieldText.setText(it) })



        itemVM.location.observe(viewLifecycleOwner, Observer { locationFieldText.setText(it) })
        itemVM.description.observe(
            viewLifecycleOwner,
            Observer { descriptionFieldText.setText(it) })
        itemVM.expireDate.observe(viewLifecycleOwner, Observer { expireField.text = it })

        if (arguments?.get("group-18-keyCardSelected") != null) {
            if (!itemVM.loadedVM)
                itemVM.getImage(arguments?.get("group-18-keyCardSelected") as String)
        }

        itemVM.image.observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                //photo_annuncio.setImageBitmap(BitmapFactory.decodeByteArray(it,0,it.size))
                if(it.size > 2) {
                    profileByte = it
                    val arrayInputStream = ByteArrayInputStream(it)
                    val imageBitmap = BitmapFactory.decodeStream(arrayInputStream)
                    photo_annuncio.setImageBitmap(imageBitmap)
                    photo_annuncio.scaleType = ImageView.ScaleType.FIT_CENTER
                    progressBar3.visibility = View.GONE
                    photo_annuncio.visibility = View.VISIBLE
                }
                else
                {
                    if(it.size == 1)
                    {
                        //L'immagine non esiste
                        progressBar3.visibility = View.INVISIBLE
                        photo_annuncio.visibility = View.VISIBLE
                        photo_annuncio.setImageResource(R.drawable.photo_default)
                    }
                    else if (it.size == 2)
                    {
                        //l'immagine esiste ma non la riesce a scaricare'
                        profileByte = it
                        progressBar3.visibility = View.VISIBLE
                        photo_annuncio.visibility = View.INVISIBLE
                    }
                }

            } else {
                if (arguments?.get("group-18-direction") != "FAB") {
                    progressBar3.visibility = View.VISIBLE
                    photo_annuncio.visibility = View.INVISIBLE
                }
            }
        })

        expireLayout.setOnClickListener {
            val dpd = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { view, mYear, mMonth, mDay ->
                    itemVM.expireDate.value = ("$mDay/${mMonth + 1}/$mYear")
                    //expireDate.value = ("$mDay/${mMonth+1}/$mYear")
                },
                itemVM.year,
                itemVM.month,
                itemVM.day
            )
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }


        locationFieldEdit.setOnClickListener {
            showPlacePicker()
        }
        locationFieldText.setOnClickListener {
            showPlacePicker()
        }
    }

    override fun onStart() {
        super.onStart()
        //TODO loading of  the item if it is present
        photo_button = (activity as MainActivity).findViewById(R.id.photo_annuncio)
        registerForContextMenu(photo_button)
        photo_button.setOnClickListener {
            (activity as MainActivity).openContextMenu(it)
        }
        button_add = (activity as MainActivity).findViewById(R.id.addImageButton2)
        registerForContextMenu(button_add)
        button_add.setOnClickListener {
            (activity as MainActivity).openContextMenu(it)
        }

    }


    override fun onPause() {
        super.onPause()

        itemVM.title.value = titleFieldText.text.toString()
        itemVM.price.value = priceFieldText.text.toString()
        itemVM.category.value = categoryField.selectedItem.toString()
        itemVM.subCategory.value = subCategoryField.selectedItem.toString()
        itemVM.description.value = descriptionFieldText.text.toString()
        itemVM.expireDate.value = expireField.text.toString()
        itemVM.location.value = locationFieldText.text.toString()
        if (profileByte!!.isNotEmpty())
            itemVM.image.value = profileByte
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_add_edit_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menu.removeItem(R.id.action_settings)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_item ->{
                (activity as MainActivity).hideKeyboard()
                (activity as MainActivity).lockDeviceRotation(true)
                saveOnDB()
            }
            else -> {
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).fab.hide()
    }

    private fun saveOnDB() {

        val n = findNavController()
        if (validateForm())
        {

            scroll_ItemEdit.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE


            if (arguments?.get("group-18-direction") == "FAB") {//sto creando un nuovo item

                itemVM.setNewItem(
                    sharedVM.userId,
                    titleFieldText.text.toString(),
                    categoryField.selectedItem.toString(),
                    subCategoryField.selectedItem.toString(),
                    descriptionFieldText.text.toString(),
                    priceFieldText.text.toString(),
                    expireField.text.toString(),
                    locationFieldText.text.toString(),
                    "Available",
                    profileByte!!
                )

                FirebaseMessaging.getInstance().subscribeToTopic(itemVM.itemId.value!!+"_n")


                itemVM.loadImage.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                            var bundle = Bundle()
                            bundle.putString("group-18-keyCardSelected", itemVM.itemId.value)

                            Snackbar.make(
                                requireView(),
                                getString(R.string.adv_created),
                                Snackbar.LENGTH_LONG
                            ).setAction("Action", null).show()

                            if (it == false  )
                            {
                                Toast.makeText(
                                    activity as MainActivity,
                                    "Item Image not uploaded due to a server error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        n.navigate(R.id.action_itemEditFragment_to_myItemList, bundle)

                        (activity as MainActivity).lockDeviceRotation(false)
                    }
                })
            } else {//sto modificando un item


                itemVM.loadImage.value =
                    null //modifico il flag per poter attendere il caricamento dell'immagine prima di passare al fragment details
                val prevStatus = itemVM.status.value
                itemVM.updateItem(
                    arguments?.get("group-18-keyCardSelected").toString(),
                    sharedVM.userId,
                    titleFieldText.text.toString(),
                    categoryField.selectedItem.toString(),
                    subCategoryField.selectedItem.toString(),
                    descriptionFieldText.text.toString(),
                    priceFieldText.text.toString(),
                    expireField.text.toString(),
                    locationFieldText.text.toString(),
                    itemVM.status.value!!,
                    profileByte!!,
                    itemVM.imageChange
                )

                if (itemVM.temporaryStatus != "" && itemVM.temporaryStatus != prevStatus) {
                    if (itemVM.interestedUsers.value!!.isNotEmpty()) {
                        val info: String = when (itemVM.status.value) {
                            "Sold" -> "This item has been sold"
                            "Unavailable" -> "This item is temporarily unavailable"
                            "Available" -> "This item is available again"
                            else -> ""
                        }

                        if(itemVM.status.value=="Sold"){
                            Log.d("soldItem","Setto la vendita dell'oggetto nell'user")
                            itemVM.setItemBought(itemVM.interestedUsers2[buyerSpinner.selectedItem.toString()]!!,arguments?.get("group-18-keyCardSelected").toString())

                        }

                        if(itemVM.status.value == "Sold") {
                            val buyer = UserID(itemVM.interestedUsers2[buyerSpinner.selectedItem.toString()]!!, buyerSpinner.selectedItem.toString())
                            itemVM.sendNotifications(
                                sharedVM.userId,
                                info,
                                "Sold",
                                buyer
                            )

                            for(el in itemVM.interestedUsers.value!!)
                            {
                                if (el != buyer)
                                    (activity as MainActivity).sendMessage(el.userId, "Second-Hand Market", info)
                                else
                                    (activity as MainActivity).sendMessage(el.userId, "Second-Hand Market", "Congratulations. You bought an item, now you can review it")
                            }

                        }
                        else {
                            (activity as MainActivity).sendMessage(itemVM.itemId.value + "_changes", "Second-Hand Market", info)
                            itemVM.sendNotifications(sharedVM.userId, info)
                        }
                    }
                }

                if(itemVM.imageChange) {
                    itemVM.loadImage.observe(viewLifecycleOwner, Observer {
                        if (it != null) {
                            if (arguments?.get("group-18-direction") == "ItemDetails") {
                                var bundle = Bundle()
                                bundle.putString("group-18-keyCardSelected", itemVM.itemId.value)
                                bundle.putString("group-18-direction", "ItemEdit")
                                bundle.putString("group-18-itemUserId", sharedVM.userId)
                                n.navigate(
                                    R.id.action_itemEditFragment_to_itemDetailsFragment,
                                    bundle
                                )

                                Snackbar.make(
                                    requireView(),
                                    "Changes applied",
                                    Snackbar.LENGTH_LONG
                                ).setAction("Action", null).show()
                            } else {
                                n.navigate(R.id.action_itemEditFragment_to_myItemList)

                                Snackbar.make(
                                    requireView(),
                                    getString(R.string.changes_applied),
                                    Snackbar.LENGTH_LONG
                                ).setAction("Action", null).show()
                            }

                            if (it == false) {
                                Toast.makeText(
                                    activity as MainActivity,
                                    "Item Image not uploaded due to a server error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            (activity as MainActivity).lockDeviceRotation(false)
                        }
                    })
                }
                else
                {
                    if (arguments?.get("group-18-direction") == "ItemDetails") {
                        var bundle = Bundle()
                        bundle.putString("group-18-keyCardSelected", itemVM.itemId.value)
                        bundle.putString("group-18-direction", "ItemEdit")
                        bundle.putString("group-18-itemUserId", sharedVM.userId)
                        n.navigate(
                            R.id.action_itemEditFragment_to_itemDetailsFragment,
                            bundle
                        )

                        Snackbar.make(
                            requireView(),
                            "Changes applied",
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                    } else {
                        n.navigate(R.id.action_itemEditFragment_to_myItemList)

                        Snackbar.make(
                            requireView(),
                            getString(R.string.changes_applied),
                            Snackbar.LENGTH_LONG
                        ).setAction("Action", null).show()
                    }
                    (activity as MainActivity).lockDeviceRotation(false)
                }


            }

        } else {
            Toast.makeText(
                activity as MainActivity,
                "It is not allowed to save an item if at least one information is missing (picture included)",
                Toast.LENGTH_LONG
            ).show()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_GALLERY) {
            image_uri = data?.data
            launchImageCrop(image_uri)

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {
            launchImageCrop(image_uri)
        } else if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            itemVM.imageChange = true
            val imageStream: InputStream =
                (activity as MainActivity).contentResolver.openInputStream(result.uri!!)!!
            profileIm = BitmapFactory.decodeStream(imageStream)
            imageStream.close()
            photo_annuncio.setImageBitmap(profileIm)
            photo_annuncio.scaleType = ImageView.ScaleType.FIT_CENTER
            val stream1 = ByteArrayOutputStream()
            profileIm!!.compress(Bitmap.CompressFormat.PNG, 100, stream1)
            profileByte = stream1.toByteArray()
            itemVM.image.value = profileByte
            stream1.close()
        }else if( (requestCode == pingActivityRequestCode) && (resultCode == Activity.RESULT_OK)){
            val place: Place? = PingPlacePicker.getPlace(data!!)
            itemVM.itemPlace.value = place
            locationFieldText.setText(place!!.address.toString())
            itemVM.itemLatitude.value = place.latLng!!.latitude
            itemVM.itemLongitude.value = place.latLng!!.longitude
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE_GALLERY -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromGallery();
                } else {
                    Toast.makeText(
                        (activity as MainActivity),
                        getString(R.string.permission_denied),
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
            PERMISSION_CODE_CAMERA -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageFromCamera()
                } else {
                    Toast.makeText(
                        (activity as MainActivity),
                        getString(R.string.permission_denied),
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        }
    }

    private fun launchImageCrop(uri: Uri?) {

        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)// default is rectangle
            .start(activity as MainActivity, this)
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

    fun validateForm(): Boolean{
        var valid = true

        if (profileByte.isEmpty()) {

            valid = false
        }

        val title = titleFieldText.text.toString()
        if(TextUtils.isEmpty(title)){
            titleFieldText.error = getString(R.string.required)
            valid = false
        }else{
            titleFieldText.error = null
        }

        val category = categoryField.selectedItem.toString()
        if(TextUtils.isEmpty(category)){
            valid = false
        }else{}

        val subcategory = subCategoryField.selectedItem.toString()
        if(TextUtils.isEmpty(subcategory)){
            valid = false
        }else{}

        val description = descriptionFieldText.text.toString()
        if(TextUtils.isEmpty(description)){
            descriptionFieldText.error = getString(R.string.required)
            valid = false
        }else{
            descriptionFieldText.error = null
        }

        val price = priceFieldText.text.toString()
        if(TextUtils.isEmpty(price) || price=="."){
            priceFieldText.error = getString(R.string.required)
            valid = false
        }else{
            priceFieldText.error = null
        }

        val expireDate = expireField.text.toString()
        if(TextUtils.isEmpty(expireDate)){
            valid = false
        }else{}

        val location = locationFieldText.text.toString()
        if(TextUtils.isEmpty(location)){
            priceFieldText.error = getString(R.string.required)
            valid = false
        }else{
            priceFieldText.error = null
        }

        if(!valid){

        }

        if(valid == false)
            (activity as MainActivity).lockDeviceRotation(false)

        return valid
    }


}


