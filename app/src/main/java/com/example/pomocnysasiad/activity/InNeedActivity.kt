package com.example.pomocnysasiad.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.fragment.CreateRequestFragmentDirections
import com.example.pomocnysasiad.fragment.MyRequestsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.pomocnysasiad.model.MyPreference
import kotlinx.android.synthetic.main.activity_in_need.*

class InNeedActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onResume() {
        super.onResume()
        if (intent.getBooleanExtra("searchClick", false)) {
            navController.navigate(CreateRequestFragmentDirections.actionCreateRequestFragmentToMyRequestsFragment())
        }
    }

    override fun onPause() {
        super.onPause()
       val preference = MyPreference(applicationContext)
        preference.setOpenChat(0L)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_need)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentInNeed) as NavHostFragment
        navController = navHostFragment.navController
        if(savedInstanceState == null){
            navController.setGraph(R.navigation.inneed_nav_graph)
            AppBarConfiguration(
                setOf(
                    R.id.myRequestsFragment,
                    R.id.createRequestFragment,
                    R.id.accountFragment3
                )
            )
        }
        inNeedBottomNavigationView.setupWithNavController(navController)

        val chatId = intent.getLongExtra("statusChanged", 0L)
        if (chatId != 0L) {
            navController.navigate(
                CreateRequestFragmentDirections.actionCreateRequestFragmentToMyRequestsFragment().actionId,
                bundleOf("notification" to chatId)
            )
        }
    }

}
