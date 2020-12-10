package com.example.pomocnysasiad.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*


class LoginActivity : AppCompatActivity() {
    companion object {
        const val SIGN_IN = 500
    }

    private val userVM by viewModels<UserViewModel>()
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        Log.d("login", "onCreate")
        if (userVM.isLogoutUserOrNotVerified()) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder()
                    .setDefaultCountryIso("pl")
                    .build()
            )
            val intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.AppTheme)
                .build()
            startActivityForResult(intent, SIGN_IN)
        } else {
            startActivity(
                Intent(applicationContext, ChooseRoleActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {

                //success, get user and do some work
                if (response!!.isNewUser) {
                    Log.d("nowy user", ".")
                    userVM.createUser()
                }
                if (!userVM.isUserVerified()) {
                    userVM.sendEmailVerif()
                    waitUntilVerified()
                } else {
                    startActivity(
                        Intent(this, ChooseRoleActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
            } else {
                //fail, show some error message
            }
        }
    }

    private fun showDialog() {
        Log.d("Dialog", "enter")
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.login_loading)

        val gif = dialog.findViewById(R.id.custom_loading_imageView) as ImageView

        val imageViewTarget = GlideDrawableImageViewTarget(gif)

        Glide.with(applicationContext)
            .load(R.drawable.loading)
            .placeholder(R.drawable.loading)
            .crossFade()
            .into(imageViewTarget)
        Log.d("Dialog", "loaded gif and show")
        dialog.show()
    }

    private fun waitUntilVerified() {
        Log.d("waitUntilVerified", "enter")

        if (!userVM.isUserVerified()) {
            Log.d("waitUntilVerified", "not verified")
            showDialog()
            val job = CoroutineScope(Dispatchers.Main).launch {
                Log.d("Coroutine", "enetred")
                while (!userVM.isUserVerified()) {
                    delay(2000)
                }
                dialog.dismiss()
                this.cancel()
            }
            job.invokeOnCompletion {
                startActivity(
                    Intent(applicationContext, ChooseRoleActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
            }
        } else {
            startActivity(
                Intent(applicationContext, ChooseRoleActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}