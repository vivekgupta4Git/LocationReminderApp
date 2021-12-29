package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)


        findViewById<Button>(R.id.loginButton).setOnClickListener {
            launchSignIn()
        }

        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }



    private fun launchSignIn(){
        val providers = arrayListOf(
    AuthUI.IdpConfig.EmailBuilder().build(),AuthUI.IdpConfig.GoogleBuilder().build()
        ,AuthUI.IdpConfig.AnonymousBuilder().build())

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            0)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0)
        {
            val response = IdpResponse.fromResultIntent(data)
            if(resultCode == Activity.RESULT_OK)
            {
                findViewById<Button>(R.id.loginButton).visibility = View.GONE
                findViewById<TextView>(R.id.welcomeTextview).text = "Loading.."

                val intent = Intent(this,RemindersActivity::class.java)
                startActivity(intent)
                finish()



            }else
            {
                Log.e("myTag","Response Error(+ ${response?.error?.errorCode} +) : ${response?.error} ")
            }
        }
    }


}
