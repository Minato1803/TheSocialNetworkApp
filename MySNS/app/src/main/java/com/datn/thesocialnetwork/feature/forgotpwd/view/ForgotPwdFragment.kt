package com.datn.thesocialnetwork.feature.forgotpwd.view

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.databinding.FragmentCreatePostBinding
import com.datn.thesocialnetwork.databinding.FragmentForgotPwdBinding
import com.datn.thesocialnetwork.databinding.FragmentLoginBinding
import com.datn.thesocialnetwork.feature.login.view.LoginFragment
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.view.CreatePostFragment
import com.datn.thesocialnetwork.feature.register.view.RegisterFragment
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ForgotPwdFragment : DialogFragment(R.layout.fragment_forgot_pwd) {

    companion object {
        fun newInstance(): ForgotPwdFragment {
            return ForgotPwdFragment()
        }
    }

    private var _bd: FragmentForgotPwdBinding? = null
    lateinit var binding: FragmentForgotPwdBinding
    lateinit var mMainActivity: MainActivity

    private var email: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        mMainActivity = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentForgotPwdBinding.bind(view)
        binding = _bd!!

        setInit()
        setEvent()
    }

    private fun setEvent() {
        binding.btnEnter.setOnClickListener { clickLogin() }
    }

    private fun clickLogin() {
        if(validInput()) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(requireContext(),"Kiểm tra hòm thư của bạn để khởi tạo lại password", Toast.LENGTH_LONG).show()
                    this.dismiss()
                }
            }
        }
    }

    private fun gotoLoginFrag() {
        val fragLogin = LoginFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragLogin, "sendToLogin")
            .commit()
    }

    private fun validInput(): Boolean {
        var isValid = false
        email = binding.edtEmail.text.toString().trim()

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Email không hợp lệ!"
            binding.edtEmail.isFocusable = true
        } else {
            isValid = true
        }

        return isValid
    }


    private fun setInit() {
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        mMainActivity.bd.toolbar.visibility = View.GONE
    }
}