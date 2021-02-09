package com.example.pomocnysasiad.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.service.VolunteerRequestService
import com.example.pomocnysasiad.activity.ChooseRoleActivity
import com.example.pomocnysasiad.activity.LoginActivity
import com.example.pomocnysasiad.model.Code
import com.example.pomocnysasiad.model.MyPreference
import com.example.pomocnysasiad.model.User
import com.example.pomocnysasiad.service.InNeedRequestService
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_account.*
import kotlin.random.Random


class AccountFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val userVM by viewModels<UserViewModel>()
    private var currentUser: User? = null
    private var tokenCounter = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        userVM.user.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
                if(it.name.isNullOrBlank()){
                    setNameLayout.visibility = View.VISIBLE
                    accountUserName.visibility = View.GONE
                    setNameButton.setOnClickListener {
                        if(setNameEditText.text.toString().isNotBlank()){
                            userVM.setDisplayName(setNameEditText.text.toString())
                        }
                    }
                } else {
                    setNameLayout.visibility = View.GONE
                    accountUserName.visibility = View.VISIBLE
                    accountUserName.text = it.name.toString()
                }
                Log.d("tokens",it.tokens.toString())
                accountUserTokens.text = it.tokens.toString()
                accountRatingBar.rating = it.score
                accountUserOpinions.text = it.helpCounter.toString()
                checkMyCode()
            }
        }

        accountSignOutBT.setOnClickListener {
            if (VolunteerRequestService.isSearching) {
                val intentService = Intent(requireContext(), VolunteerRequestService::class.java)
                requireContext().stopService(intentService)
            }
            if(InNeedRequestService.isSearching) {
                val intentService = Intent(requireContext(), InNeedRequestService::class.java)
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
            if (VolunteerRequestService.isSearching) {
                val intentService = Intent(requireContext(), VolunteerRequestService::class.java)
                requireContext().stopService(intentService)
            }
            if(InNeedRequestService.isSearching) {
                val intentService = Intent(requireContext(), InNeedRequestService::class.java)
                requireContext().stopService(intentService)
            }
            startActivity(
                Intent(this.context, ChooseRoleActivity::class.java)
            )
        }

        accountActivateCodeBtn.setOnClickListener {

                val data = userVM.getCode(accountCodeText.text.toString().toInt())
                data.observe(viewLifecycleOwner, object : Observer<Code?> {
                    override fun onChanged(code: Code?) {
                        if (code != null) {
                            userVM.deleteCode(code.codeID)
                        } else {
                            Toast.makeText(context, "Podany kod nie istnieje", Toast.LENGTH_SHORT)
                                .show()
                        }
                        data.removeObserver(this)
                    }
                })
        }
        accountAddTockenBtn.setOnClickListener {
            tokenCounter += 1
            accountTokenCountDisplay.text = tokenCounter.toString()
        }
        accountReduceTockenBtn.setOnClickListener {
            if (tokenCounter > 1) {
                tokenCounter -= 1
                accountTokenCountDisplay.text = tokenCounter.toString()
            }
        }
        accountCreateCodeBtn.setOnClickListener {
            currentUser?.let {
                if (it.tokens >= tokenCounter) {
                    val codeID = Random(System.nanoTime()).nextInt(899999) + 100000
                    userVM.createCode(Code(codeID, it.id, tokenCounter))
                    userVM.decreaseToken(tokenCounter.toLong())
                } else {
                    Toast.makeText(context, "Nie masz wystarczająco tokenów", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
        accountDeleteCodeBtn.setOnClickListener {
            Log.d("accountDeleteCodeBtn","click")
            userVM.deleteCode(accountCodeDisplay.text.toString().toInt())
            createCodeLayout.visibility = View.VISIBLE
            codeDisplayDeleteLayout.visibility = View.GONE
        }
        accountOpinionsBtn.setOnClickListener {
            val preferences = MyPreference(requireContext())
            if (preferences.getRole() == 1) {
                findNavController().navigate(
                    R.id.action_accountFragment2_to_accountReputationFragment,
                    bundleOf("user" to Gson().toJson(currentUser!!))
                )
            } else {
                findNavController().navigate(
                    R.id.action_accountFragment3_to_accountReputationFragment2,
                    bundleOf("user" to Gson().toJson(currentUser!!))
                )
            }

        }

    }

    private fun checkMyCode() {
        if (currentUser != null) {
            val data = userVM.getCode(currentUser!!.id)
            data.observe(viewLifecycleOwner,
                { code ->
                    if (code != null) {
                        createCodeLayout.visibility = View.GONE
                        codeDisplayDeleteLayout.visibility = View.VISIBLE
                        accountCodeDisplay.text = code.codeID.toString()
                    } else {
                        createCodeLayout.visibility = View.VISIBLE
                        codeDisplayDeleteLayout.visibility = View.GONE
                    }
                })
        }
    }
}