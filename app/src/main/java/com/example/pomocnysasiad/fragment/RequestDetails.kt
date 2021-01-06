package com.example.pomocnysasiad.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.model.Request
import kotlinx.android.synthetic.main.fragment_request_details.*


class RequestDetails : Fragment() {

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
        return inflater.inflate(R.layout.fragment_request_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.get("title")
        val auth = arguments?.get("auth")
        val desc = arguments?.get("desc")

        detailsTitle.text = "tytul: ${title}"
        detailsAuth.text = "Osoba potrzebujaca: ${auth}"
        detailsDesc.text = "Opis: ${desc}"


    }
}