package com.datn.thesocialnetwork.feature.register.view

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.LoadingScreen
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.ModelMapping
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserResponse
import com.datn.thesocialnetwork.databinding.FragmentRegisterBinding
import com.datn.thesocialnetwork.feature.login.view.LoginFragment
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.editprofile.view.EditProfileFragment
import com.datn.thesocialnetwork.feature.register.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var bd: FragmentRegisterBinding
    lateinit var mMainActivity: MainActivity

    private val mRegisterViewModel: RegisterViewModel by viewModels()


    private var email: String = ""
    private var userName: String = ""
    private var firstName: String = ""
    private var lastName: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        bd = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        setInit()
        setObserveData()
        setEvent()
        return bd.root
    }

    private fun setInit() {
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        mMainActivity.bd.toolbar.visibility = View.GONE
    }

    private fun setObserveData() {
        mRegisterViewModel.liveDataRegisterUser.observe(viewLifecycleOwner, { response ->
            observeRegisterUser(response)
        })

        mRegisterViewModel.liveDataInsertUser.observe(viewLifecycleOwner, { response ->
            observeInsertUser(response)
        })
    }

    private fun observeInsertUser(response: Response<UserResponse>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(requireContext())

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(requireContext(), response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                response.data?.let {
                    GlobalValue.USER = it
                    GlobalValue.USER_DETAIL = ModelMapping.mapToUserModel(GlobalValue.USER!!)
                    Log.d("TAG",it.uidUser + " " + it.userDetail.email)
                    sendToProfile()
                }
            }
        }
    }

    private fun setEvent() {
        bd.buttonRegister.setOnClickListener { clickRegister() }

        bd.buttonLoginNow.setOnClickListener {
            val fragLogin = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(id, fragLogin, "sendToDetailAddress")
                .commit()
        }
    }

    private fun clickRegister() {
        if (validateInput()) {
            mRegisterViewModel.registerUser(email, password)
        }

    }

    private fun observeRegisterUser(response: Response<FirebaseUser>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(requireContext())

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(requireContext(), response.message)
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
            bd.textInputEmail.error = "Email không hợp lệ!"
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

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, EditProfileFragment.newInstance(), "tag")
            .commit()
    }
}