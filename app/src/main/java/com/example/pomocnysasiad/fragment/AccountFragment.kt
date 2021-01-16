package com.example.pomocnysasiad.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.pomocnysasiad.R
import com.example.pomocnysasiad.VolunteerRequestService
import com.example.pomocnysasiad.activity.ChooseRoleActivity
import com.example.pomocnysasiad.activity.LoginActivity
import com.example.pomocnysasiad.model.Code
import com.example.pomocnysasiad.model.User
import com.example.pomocnysasiad.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.*
import kotlin.random.Random


class AccountFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val userVM by viewModels<UserViewModel>()
    private var currentUser: User? = null
    private var tokenCounter = 1
    private lateinit var myCode: LiveData<Code>

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
        userVM.user.observe(viewLifecycleOwner) {
            if (it != null) {
                currentUser = it
                accountUserTokens.text = "zetony: ${it.tokens.toString()}"
                checkMyCode()
            }
        }

        accountSignOutBT.setOnClickListener {
            if (VolunteerRequestService.isSearching) {
                val intentService = Intent(requireContext(), VolunteerRequestService::class.java)
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
            startActivity(
                    Intent(this.context, ChooseRoleActivity::class.java)
            )
        }
        accountActivateCodeBtn.setOnClickListener {
            try {
                val data = userVM.getCode(accountCodeText.text.toString().toInt())
                data.observe(viewLifecycleOwner, object: Observer<Code?> {
                    override fun onChanged(code: Code?) {
                        if (code != null) {
                            userVM.deleteCode(code.codeID)
                            userVM.increaseToken(code.tokenNum.toLong())
                        } else {
                            Toast.makeText(context, "Podany kod nie istnieje", Toast.LENGTH_SHORT).show()
                        }
                        data.removeObserver(this)
                    }
                })
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "Kod musi byc liczbą", Toast.LENGTH_SHORT).show()
            }
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
                    var codeID = Random(System.nanoTime()).nextInt(899999) + 100000
                    //TODO: jakies zabezpieczenie zeby sie 2 razy taki sam kod nie wygenerowal
                    //while (userVM.getCode(codeID)?.value != null) {
                    //    codeID = Random(System.nanoTime()).nextInt(899999) + 100000
                    //}
                    userVM.createCode(Code(codeID, it.id, tokenCounter))
                    userVM.decreaseToken(tokenCounter.toLong())
                } else {
                    Toast.makeText(context, "Nie masz wystarczająco tokenów", Toast.LENGTH_SHORT).show()
                }
            }
        }
        accountDeleteCodeBtn.setOnClickListener {
            val data = userVM.getCode(accountCodeDisplay.text.toString().toInt())
            data.observe(viewLifecycleOwner, object: Observer<Code?> {
                override fun onChanged(code: Code?) {
                    if (code != null) {
                        userVM.deleteCode(code.codeID)
                        userVM.increaseToken(code.tokenNum.toLong())
                    }
                    data.removeObserver(this)
                }
            })
        }
    }

    private fun checkMyCode() {
        if (currentUser != null) {
            val data = userVM.getCode(currentUser!!.id)
            data.observe(viewLifecycleOwner, object: Observer<Code?> {
                override fun onChanged(code: Code?) {
                    if (code != null) {
                        accountReduceTockenBtn.visibility = View.GONE
                        accountAddTockenBtn.visibility = View.GONE
                        accountTokenCountDisplay.visibility = View.GONE
                        accountCreateCodeBtn.visibility = View.GONE
                        accountCodeDisplay.visibility = View.VISIBLE
                        accountDeleteCodeBtn.visibility = View.VISIBLE
                        accountCodeDisplay.text = code.codeID.toString()
                    } else {
                        accountReduceTockenBtn.visibility = View.VISIBLE
                        accountAddTockenBtn.visibility = View.VISIBLE
                        accountTokenCountDisplay.visibility = View.VISIBLE
                        accountCreateCodeBtn.visibility = View.VISIBLE
                        accountCodeDisplay.visibility = View.GONE
                        accountDeleteCodeBtn.visibility = View.GONE
                    }
                    data.removeObserver(this)
                }
            })
        }
    }
}