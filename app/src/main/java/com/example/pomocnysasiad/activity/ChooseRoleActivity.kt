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
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        inNeedRoleBT.setOnClickListener {
            startActivity(
                Intent(applicationContext, InNeedActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }
    }
}