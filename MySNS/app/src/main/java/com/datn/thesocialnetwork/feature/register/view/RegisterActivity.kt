package com.datn.thesocialnetwork.feature.register.view

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.activity.viewModels
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.LoadingScreen
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.ActivityLoginBinding
import com.datn.thesocialnetwork.databinding.ActivityRegisterBinding
import com.datn.thesocialnetwork.feature.login.view.LoginActivity
import com.datn.thesocialnetwork.feature.login.viewmodel.LoginViewModel
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.editprofile.view.EditProfileFragment
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.datn.thesocialnetwork.feature.register.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var bd: ActivityRegisterBinding

    private val mRegisterViewModel: RegisterViewModel by viewModels()

    private var email: String = ""
    private var userName: String = ""
    private var firstName: String = ""
    private var lastName: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(bd.root)

        setInit()
        setObserveData()
        setEvent()
    }

    private fun setInit() {
        //not implement yet
    }

    private fun setObserveData() {
        mRegisterViewModel.liveDataRegisterUser.observe(this, { response ->
            observeRegisterUser(response)
        })

        mRegisterViewModel.liveDataInsertUser.observe(this, { response ->
            observeInsertUser(response)
        })
    }

    private fun observeInsertUser(response: Response<UserResponse>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(this)

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(this, response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                response.data?.let {
                    GlobalValue.USER = it
                    Log.d("TAG",it.uidUser + " " + it.userDetail.email)
                    sendToProfile()
                }
            }
        }
    }

    private fun setEvent() {
        bd.buttonRegister.setOnClickListener { clickRegister() }

        bd.buttonLoginNow.setOnClickListener {
            val mainIntent = Intent(this.applicationContext, RegisterActivity::class.java)
            startActivity(mainIntent)
        }
    }

    private fun clickRegister() {
        if (validateInput()) {
            mRegisterViewModel.registerUser(email, password)
        }

    }

    private fun observeRegisterUser(response: Response<FirebaseUser>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(this)

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(this, response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                response.data?.let {
                    mRegisterViewModel
                        .InsertUserToFirebase(
                            accFirebase = it,
                            password = password,
                            userName = userName,
                            firstName = firstName,
                            lastName = lastName,
                        )
                }
            }
        }
    }

    private fun validateInput() : Boolean {
        var isValid = false
        email = bd.edtEmail.text.toString().trim()
        userName = bd.edtUsername.text.toString().trim()
        firstName = bd.edtFirstName.text.toString().trim()
        lastName = bd.edtLastName.text.toString().trim()
        password = bd.edtPassword.text.toString().trim()
        confirmPassword = bd.edtConfirmPassword.text.toString().trim()
        //validate
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            bd.edtEmail.error = "Email không hợp lệ!"
            bd.edtEmail.isFocusable = true
        } else if(userName.isEmpty() && Regex(Const.REGEX_SPECIAL_CHAR).containsMatchIn(userName)) {
            bd.edtUsername.error = "userName không hợp lệ!"
            bd.edtUsername.isFocusable = true
        } else if (
            firstName.isEmpty() ||
            (Regex(Const.REGEX_SPECIAL_CHAR).containsMatchIn(firstName) ||
                    Regex(Const.REGEX_NUMBER).containsMatchIn(firstName))
        ) {
            bd.edtFirstName.error = "Tên không hợp lệ!"
            bd.edtFirstName.isFocusable = true
        } else if (
            lastName.isEmpty() ||
            (Regex(Const.REGEX_SPECIAL_CHAR).containsMatchIn(lastName)
            || Regex(Const.REGEX_NUMBER).containsMatchIn(lastName))
        ) {
            bd.edtFirstName.error = "Họ không hợp lệ!"
            bd.edtFirstName.isFocusable = true
        } else if (password.isEmpty() || password.length < 6) {
            bd.edtPassword.error = "Độ dài mật khẩu phải lớn hơn 6 kí tự!"
            bd.edtPassword.isFocusable = true
        } else if (confirmPassword.isEmpty()|| !confirmPassword.equals(password)) {
            bd.edtConfirmPassword.error = "Vui lòng nhập lại mật khẩu!"
            bd.edtConfirmPassword.isFocusable = true
        } else{
            isValid = true
        }
        return isValid
    }

    private fun sendToProfile() {
//        val fragEditProfile = EditProfileFragment()
//        val fragProfile = ProfileFragment()

//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragContainer, fragEditProfile)
//            .commit()
        EditProfileFragment.newInstance()
            .show(supportFragmentManager, "EditProfileFragmentFragment")
        finish()
    }
}