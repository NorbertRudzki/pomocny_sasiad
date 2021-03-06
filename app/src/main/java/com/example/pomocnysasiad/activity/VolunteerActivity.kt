package com.example.pomocnysasiad.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.fragment.CreateRequestFragmentDirections
import com.example.pomocnysasiad.fragment.SearchRequestFragmentDirections
import com.example.pomocnysasiad.model.MyPreference
import kotlinx.android.synthetic.main.activity_volunteer.*

class VolunteerActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    override fun onResume() {
        super.onResume()
        val notificationId = intent.getIntExtra("notified",0)
        if(notificationId != 0){
            NotificationManagerCompat.from(applicationContext).cancel(notificationId)
        }
    }

    override fun onPause() {
        super.onPause()
        val preference = MyPreference(applicationContext)
        preference.setOpenChat(0L)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentVolunteer) as NavHostFragment
        navController = navHostFragment.navController
        if(savedInstanceState == null){
            navController.setGraph(R.navigation.volunteer_nav_graph)
            AppBarConfiguration(
                setOf(
                    R.id.acceptedRequestsFragment2,
                    R.id.searchRequestFragment2,
                    R.id.accountFragment2
                )
            )
        }

        volunteerBottomNavigationView.setupWithNavController(navController)

        val chatId = intent.getLongExtra("statusChanged", 0L)
        if (chatId != 0L) {
            navController.navigate(
                SearchRequestFragmentDirections.actionSearchRequestFragment2ToAcceptedRequestsFragment2().actionId,
                bundleOf("notification" to chatId)
            )
        }
    }
}