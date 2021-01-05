package com.example.pomocnysasiad.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.MyPreference
import kotlinx.android.synthetic.main.activity_choose_role.*

class ChooseRoleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        volunteerRoleBT.setOnClickListener {
            val myPreference = MyPreference(this)
            myPreference.setRole(1)
            startActivity(
                Intent(applicationContext, VolunteerActivity::class.java)
            )
            finish()
        }

        inNeedRoleBT.setOnClickListener {
            val myPreference = MyPreference(this)
            myPreference.setRole(2)
            startActivity(
                Intent(applicationContext, InNeedActivity::class.java)
            )
            finish()
        }
    }
}