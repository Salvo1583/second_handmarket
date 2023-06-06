package it.polito.mad.group18.second_handmarket.auth

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.group18.second_handmarket.MainActivity
import it.polito.mad.group18.second_handmarket.R
import it.polito.mad.group18.second_handmarket.viewModels.SharedVM
import it.polito.mad.group18.second_handmarket.viewModels.UserVM
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.authentication.*
import java.io.ByteArrayInputStream


class Authentication : Fragment()
{


    private lateinit var auth: FirebaseAuth


    private val sharedVM: SharedVM by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        auth = FirebaseAuth.getInstance()
        (activity as MainActivity).lockDeviceRotation(true, true)
        (activity as MainActivity).fab.hide()
        (activity as MainActivity).setDrawerLocked(true)
        return inflater.inflate(R.layout.authentication, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        register.paint.isUnderlineText = true
        (activity as MainActivity).supportActionBar!!.hide()

        if (auth.currentUser != null) {
            Log.d("HLLLL", "1")

            FirebaseMessaging.getInstance().unsubscribeFromTopic(sharedVM.userId)

            if(sharedVM.myItems.value!!.isNotEmpty()) {
                for (elements in sharedVM.myItems.value!!) {
                    Log.d("topicUnsubscribe", "Topic to be going unsubscribed: $elements")
                    FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(elements.itemId + "_n")
                }
            }

            if(sharedVM.interestedItems.value!!.isNotEmpty()) {
                for (elements in sharedVM.interestedItems.value!!) {
                    Log.d("topicUnsubscribe", "Topic to be going unsubscribed: $elements")
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(elements + "_changes")
                }
            }

            signOut()

        }

        signInWithGoogle.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("GoogleSignIn", "True")

            findNavController().navigate(R.id.action_authentication_to_editProfileFragment, bundle)


        }


        signIn.setOnClickListener {
            (activity as MainActivity).hideKeyboard()
            signIn(usernameField.text.toString(), passwordField.text.toString())
        }
        register.setOnClickListener {
            val bundle = Bundle()
            bundle.putBoolean("Registration", true)

            findNavController().navigate(R.id.action_authentication_to_editProfileFragment, bundle)
        }

    }

    private fun updateUI() {

        findNavController().navigate(R.id.action_authentication_to_nav_home)

    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = usernameField.text.toString()
        if (TextUtils.isEmpty(email)) {
            usernameField.error = "Required."
            valid = false
        } else {
            usernameField.error = null
        }

        val password = passwordField.text.toString()
        if (TextUtils.isEmpty(password)) {
            passwordField.error = "Required."
            valid = false
        } else {
            passwordField.error = null
        }
        return valid
    }

    private fun signIn(email: String, password: String) {
        if (!validateForm()) {
            return
        }

        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity as MainActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    sharedVM.userId = user!!.uid
                    sharedVM.userEmail = user!!.email!!
                    updateUI()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        activity as MainActivity, "Login failed: ${task.exception!!.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        // [END sign_in_with_email]
    }

    private fun signOut() {
        auth.signOut()
        sharedVM.userId = ""
        sharedVM.userEmail = ""
        findNavController().popBackStack()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).lockDeviceRotation(false)
    }



}