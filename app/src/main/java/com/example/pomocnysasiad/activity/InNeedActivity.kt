package com.example.pomocnysasiad.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.fragment.CreateRequestFragmentDirections
import com.example.pomocnysasiad.fragment.MyRequestsFragment
import kotlinx.android.synthetic.main.activity_in_need.*

class InNeedActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onResume() {
        super.onResume()
        if(intent.getBooleanExtra("searchClick", false)){
            navController.navigate(CreateRequestFragmentDirections.actionCreateRequestFragmentToMyRequestsFragment())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_need)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentInNeed) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.inneed_nav_graph)
        AppBarConfiguration(
            setOf(
                R.id.myRequestsFragment,
                R.id.createRequestFragment,
                R.id.accountFragment3
            )
        )
        inNeedBottomNavigationView.setupWithNavController(navController)
    }
}
