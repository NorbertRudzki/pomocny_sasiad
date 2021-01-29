package com.example.pomocnysasiad.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.view.OpinionAdapter
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.firebase.ui.auth.data.model.User
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_account_reputation.*

class AccountReputationFragment : Fragment() {
    private val userVM by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_reputation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reputationOpinionRecycler.layoutManager = LinearLayoutManager(requireContext())
        val userId = arguments?.getString("userId")
        val userJson = arguments?.getString("user")
        if(!userJson.isNullOrBlank()){
            val user = Gson().fromJson(userJson, com.example.pomocnysasiad.model.User::class.java)
            display(user)
        } else {
            val userLiveData = userVM.getUser(userId!!)
            userLiveData.observe(viewLifecycleOwner){
                if(it != null){
                    display(it)
                    userLiveData.removeObservers(viewLifecycleOwner)
                }
            }
        }
    }

    private fun display(user: com.example.pomocnysasiad.model.User){
        reputationUser.text = user.name
        reputationHelpCounter.text = "${user.helpCounter}"
        reputationRatingBar.rating = user.score
        if(user.opinionList.isEmpty()){
            emptyImage.visibility = View.VISIBLE
            reputationOpinionRecycler.visibility = View.GONE
        } else {
            emptyImage.visibility = View.GONE
            reputationOpinionRecycler.visibility = View.VISIBLE
            reputationOpinionRecycler.adapter = OpinionAdapter(user.opinionList)
        }

    }
}