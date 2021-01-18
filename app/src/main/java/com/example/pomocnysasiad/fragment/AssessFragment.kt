package com.example.pomocnysasiad.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.FirebaseRepository
import com.example.pomocnysasiad.model.MyPreference
import kotlinx.android.synthetic.main.fragment_assess.*


class AssessFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assess, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val firebaseRepository = FirebaseRepository()
        val preference = MyPreference(requireContext())

        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("userId")
        val userName = arguments?.getString("userName")
        assessQuestion.text = "${resources.getString(R.string.assess_question)} $userName"
        var userRate = 0.0F
        assessRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            userRate = rating
        }
        assessSubmit.setOnClickListener {
            val opinion = assessOpinionET.text.toString()
            if (opinion.isNotBlank()) {
                firebaseRepository.assessUser(userId!!, opinion, userRate)
                if (preference.getRole() == 1) {
                    findNavController().navigate(R.id.action_assessFragment2_to_acceptedRequestsFragment2)
                } else {
                    findNavController().navigate(R.id.action_assessFragment_to_myRequestsFragment)
                }
            }

        }
    }
}