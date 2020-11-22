package com.example.pomocnysasiad.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import com.example.pomocnysasiad.R
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.layout_login.*
import kotlinx.android.synthetic.main.layout_login.view.*
import kotlinx.android.synthetic.main.layout_register.*
import kotlinx.android.synthetic.main.layout_register.view.*
import kotlinx.coroutines.*
import java.util.*

class LoginActivity : AppCompatActivity() {
    private lateinit var loginView: View
    private lateinit var registerView: View
    private lateinit var parentView: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        parentView = findViewById(R.id.loginRegisterLayout)
        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode(Locale.getDefault().language)
        auth.currentUser?.let {
            if(it.isEmailVerified) {
                startActivity(Intent(applicationContext,ChooseRoleActivity::class.java))
                finish()
            } else {
                auth.signOut()
            }
        }
        loginView = layoutInflater.inflate(R.layout.layout_login, null)
        registerView = layoutInflater.inflate(R.layout.layout_register, null)
        parentView.addView(loginView)

        loginView.loginSignUP.setOnClickListener {
            parentView.removeAllViewsInLayout()
            parentView.addView(registerView)
        }
        registerView.loginSignIn.setOnClickListener {
            parentView.removeAllViewsInLayout()
            parentView.addView(loginView)
        }
        loginView.cirLoginButton.setOnClickListener {
            val login = loginView.loginEmail.text.toString()
            val password = loginView.loginPassword.text.toString()
            signIn(login, password)
        }

        registerView.registerButton.setOnClickListener {
            val name = registerView.registerDisplayName.text.toString()
            val login = registerView.registerEmail.text.toString()
            val password = registerView.registerPassword.text.toString()
            val rePassword = registerView.registerRePassword.text.toString()
            if(password == rePassword) {
                createUser(name, login, password)
            } else {
                registerValidateAuthTV.text =
                        resources.getString(R.string.password_repeat_correclty)
                registerValidateAuthTV.visibility = View.VISIBLE
            }

        }
    }

    private fun createUser(displayName: String, email: String, password: String) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            Log.d("create user", user.getIdToken(true).toString())
                            user.updateProfile(
                                    UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                            )
                            Log.d("email","send")
                            user.sendEmailVerification()
                            Log.d("email","sent")
                            waitUntilVerified()
                        }
                    } else {
                        Log.d("create exec", it.exception.toString())
                        when (it.exception) {
                            is FirebaseAuthUserCollisionException ->
                                registerValidateAuthTV.text =
                                        resources.getString(R.string.email_is_used)

                            is FirebaseAuthWeakPasswordException ->
                                registerValidateAuthTV.text =
                                        resources.getString(R.string.weak_password)

                            is FirebaseAuthInvalidCredentialsException ->
                                registerValidateAuthTV.text =
                                        resources.getString(R.string.email_is_bad_formated)

                            else -> registerValidateAuthTV.text = it.exception!!.message
                        }
                        registerValidateAuthTV.visibility = View.VISIBLE
                    }

                }
    }

    private fun signIn(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d("user", user.toString())
                        waitUntilVerified()
                    } else {
                        Log.d("login", it.exception.toString())
                        loginValidateAuthTV.visibility = View.VISIBLE
                        if (it.exception!!.message!!.contains("password")) {
                            loginValidateAuthTV.text = resources.getString(R.string.Wrong_password)
                        }
                        if (it.exception!!.message!!.contains("formatted")) {
                            loginValidateAuthTV.text =
                                    resources.getString(R.string.email_is_bad_formated)
                        }
                        if (it.exception!!.message!!.contains("no user record")) {
                            loginValidateAuthTV.text = resources.getString(R.string.No_user_record)
                        }
                        loginValidateAuthTV.visibility = View.VISIBLE
                    }
                }
    }

    private fun showDialog() {
        Log.d("Dialog","enter")
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
        Log.d("Dialog","loaded gif and show")
        dialog.show()
    }

    private fun waitUntilVerified() {
        Log.d("waitUntilVerified","enter")
        val verified = auth.currentUser!!.isEmailVerified
        if (!verified) {
            Log.d("waitUntilVerified","not verified")
            showDialog()
            val job =  CoroutineScope(Dispatchers.Main).launch {
                Log.d("Coroutine","enetred")
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