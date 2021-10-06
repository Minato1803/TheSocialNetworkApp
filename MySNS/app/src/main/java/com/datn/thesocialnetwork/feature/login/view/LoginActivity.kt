package com.datn.thesocialnetwork.feature.login.view

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.LoadingScreen
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.ActivityLoginBinding
import com.datn.thesocialnetwork.feature.login.viewmodel.LoginViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var bd: ActivityLoginBinding

    @Inject
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val mLoginViewModel: LoginViewModel by viewModels()


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
            observeLogin(response)
        })

        mLoginViewModel.liveDataVerifyLogin.observe(this, { response ->
            observeVerifyLogin(response)
        })
    }

    private fun observeVerifyLogin(response: Response<String>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(this)

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(this, response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                sendToMainActivity()
            }
        }
    }

    private fun sendToMainActivity() {
        val mainIntent = Intent(this.applicationContext, MainActivity::class.java)
        startActivity(mainIntent)
        finish()
    }

    private fun observeLogin(response: Response<UserResponse>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(this)

            is Response.Error -> {
                LoadingScreen.hide()
                if (response.message == getString(R.string.err_no_exist)) {
                    //register
                } else {
                    SystemUtils.showDialogError(this, response.message)
                }
            }

            is Response.Success -> {
                LoadingScreen.hide()
                sendToMainActivity()
            }
        }
    }

    private fun setEvent() {
        bd.btnLoginWithGoogle.setOnClickListener { clickLoginWithGG() }

        bd.btnLogin.setOnClickListener { clickLogin() }
    }

    private fun clickLogin() {
        val email = bd.tvEmail.text.toString().trim()
        val password = bd.tvPassword.text.toString().trim()
        //validate
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            bd.tvEmail.error = "sai Email!"
            bd.tvEmail.isFocusable = true
        } else if (password.length < 6) {
            bd.tvPassword.error = "Độ dài mật khẩu phải lớn hơn 6 kí tự!"
            bd.tvPassword.isFocusable = true
        } else {
            mLoginViewModel.verifyLogin(email, password, this)
        }
    }

    private fun clickLoginWithGG() {
        getGoogleAccount()
    }

    private fun getGoogleAccount() {
        resultActivityPickAccGG.launch(mGoogleSignInClient.signInIntent)
    }

    private val resultActivityPickAccGG =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val accGG = GoogleSignIn.getSignedInAccountFromIntent(result.data).result
                accGG?.let {
                    mLoginViewModel.checkUserExist(accGG)
                }
            }
        }
}