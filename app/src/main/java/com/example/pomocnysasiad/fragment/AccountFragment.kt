package com.example.pomocnysasiad.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.SearchRequestService
import com.example.pomocnysasiad.activity.ChooseRoleActivity
import com.example.pomocnysasiad.activity.LoginActivity
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.*


class AccountFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val userVM by viewModels<UserViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountUserName.text = userVM.getUserName()
        auth = FirebaseAuth.getInstance()
        accountSignOutBT.setOnClickListener {
            if (SearchRequestService.isSearching) {
                val intentService = Intent(requireContext(), SearchRequestService::class.java)
                requireContext().stopService(intentService)
            }
            auth.signOut()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            requireActivity().finish()
        }
        accountChooseRoleBT.setOnClickListener {
            if (SearchRequestService.isSearching) {
                val intentService = Intent(requireContext(), SearchRequestService::class.java)
                requireContext().stopService(intentService)
            }
            startActivity(
                    Intent(this.context, ChooseRoleActivity::class.java)
            )
        }
    }
}