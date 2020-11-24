package com.example.pomocnysasiad.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.pomocnysasiad.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.*
import kotlinx.coroutines.*


class LoginActivity : AppCompatActivity() {
    companion object {
        const val SIGN_IN = 500
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("pl")
        Log.d("login","onCreate")
        val user = auth.currentUser
        if (user == null || (!user.isEmailVerified && user.email != null)) {
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

    override fun onStart() {
        super.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            Log.d("response", response!!.toString())
            if (resultCode == Activity.RESULT_OK) {
                //success, get user and do some work
                val user = FirebaseAuth.getInstance().currentUser
                if (!user!!.isEmailVerified && user.email != null) {
                    user.sendEmailVerification()
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
        val verified = auth.currentUser!!.isEmailVerified
        if (!verified) {
            Log.d("waitUntilVerified", "not verified")
            showDialog()
            val job = CoroutineScope(Dispatchers.Main).launch {
                Log.d("Coroutine", "enetred")
                while (!auth.currentUser!!.isEmailVerified) {
                    delay(2000)
                    auth.currentUser!!.reload()
                    Log.d("user", auth.currentUser!!.toString())
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