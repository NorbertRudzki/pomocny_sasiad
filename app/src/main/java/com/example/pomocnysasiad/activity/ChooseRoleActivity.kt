package com.example.pomocnysasiad.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pomocnysasiad.R
import kotlinx.android.synthetic.main.activity_choose_role.*

class ChooseRoleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)

        volunteerRoleBT.setOnClickListener {
            startActivity(
                Intent(applicationContext, VolunteerActivity::class.java)
            )
            finish()
        }

        inNeedRoleBT.setOnClickListener {
            startActivity(
                Intent(applicationContext, InNeedActivity::class.java)
            )
            finish()
        }
    }
}