package com.datn.thesocialnetwork.feature.login.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.LoadingScreen
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.FirebaseNode
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.ActivityLoginBinding
import com.datn.thesocialnetwork.feature.login.viewmodel.LoginViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.register.view.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var bd: ActivityLoginBinding

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val mLoginViewModel: LoginViewModel by viewModels()

    private var email: String = ""
    private var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bd.root)

        setInit()
        setObserveData()
        setEvent()
    }

    private fun setInit() {
    }

    private fun setObserveData() {
        mLoginViewModel.liveDataLogin.observe(this, { response ->
            observeDataLogin(response)
        })

        mLoginViewModel.liveLoginUser.observe(this, { response ->
            observeLoginUser(response)
        })
    }

    private fun observeLoginUser(response: Response<FirebaseUser>) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(this)

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(this, response.message)
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
        val mainIntent = Intent(this.applicationContext, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun observeDataLogin(response: Response<UserResponse>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(this)

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(this, response.message)
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
            val mainIntent = Intent(this.applicationContext, RegisterActivity::class.java)
            startActivity(mainIntent)
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