package com.datn.thesocialnetwork.feature.login.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.datn.thesocialnetwork.core.api.LoadingScreen
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.FragmentLoginBinding
import com.datn.thesocialnetwork.feature.login.viewmodel.LoginViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.register.view.RegisterFragment
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var bd: FragmentLoginBinding

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val mLoginViewModel: LoginViewModel by viewModels()
    private val fragRegister = RegisterFragment()

    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        bd = FragmentLoginBinding.inflate(layoutInflater, container, false)
        setInit()
        setObserveData()
        setEvent()
        return bd.root
    }

    private fun setInit() {
    }

    private fun setObserveData() {
        mLoginViewModel.liveDataLogin.observe(viewLifecycleOwner, { response ->
            observeDataLogin(response)
        })

        mLoginViewModel.liveLoginUser.observe(viewLifecycleOwner, { response ->
            observeLoginUser(response)
        })
    }

    private fun observeLoginUser(response: Response<FirebaseUser>) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(requireContext())

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(requireContext(), response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                response.data?.let {
                    mLoginViewModel.getInfoUser(it)
                }
            }
        }
    }

    private fun sendToMainActivity() {
        startActivity(Intent(context, MainActivity::class.java))
    }

    private fun observeDataLogin(response: Response<UserResponse>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(requireContext())

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(requireContext(), response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                response.data.let {
                    GlobalValue.USER = it
                    sendToMainActivity()
                }
            }
        }
    }

    private fun setEvent() {
        bd.btnLogin.setOnClickListener { clickLogin() }

        bd.btnRegisterNow.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(id, fragRegister, "sendToRegister")
                .commit()
        }
    }

    private fun clickLogin() {
        if(validInput()) {
            mLoginViewModel.LoginWithEmailPassword(email, password)
        }
    }

    private fun validInput(): Boolean {
        var isValid = false
        email = bd.edtEmail.text.toString().trim()
        password = bd.edtPassword.text.toString().trim()

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            bd.edtEmail.error = "Email không hợp lệ!"
            bd.edtEmail.isFocusable = true
        } else {
            isValid = true
        }

        return isValid
    }
}