package com.example.pomocnysasiad.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.pomocnysasiad.R
import kotlinx.android.synthetic.main.activity_volunteer.*

class VolunteerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)
        val navController: NavController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentVolunteer) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.volunteer_nav_graph)
        AppBarConfiguration(
            setOf(
                R.id.acceptedRequestsFragment2,
                R.id.searchRequestFragment2,
                R.id.accountFragment2
            )
        )
        volunteerBottomNavigationView.setupWithNavController(navController)
    }
}