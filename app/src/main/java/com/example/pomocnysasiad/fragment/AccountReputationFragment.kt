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
        val userId = arguments?.getString("userId")!!
        val userLiveData = userVM.getUser(userId)
        userLiveData.observe(viewLifecycleOwner){
            if(it != null){
                display(it)
                userLiveData.removeObservers(viewLifecycleOwner)
            }
        }

    }

    private fun display(user: com.example.pomocnysasiad.model.User){
        reputationUser.text = user.name
        reputationHelpCounter.text = "${user.helpCounter} ocen"
        reputationRatingBar.rating = user.score
        reputationOpinionRecycler.adapter = OpinionAdapter(user.opinionList)
    }
}