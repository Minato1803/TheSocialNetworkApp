package com.datn.thesocialnetwork.feature.profile.editprofile.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.databinding.FragmentEditProfileBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileFragment : DialogFragment(R.layout.fragment_edit_profile) {

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    @Inject
    lateinit var mGlide: RequestManager
    private var _bd: FragmentEditProfileBinding? = null
    lateinit var bd: FragmentEditProfileBinding

    private val user = GlobalValue.USER!!

    var imgAvatarBitmap: Bitmap? = null
    private var gender = ""
    private var birthday = ""
    private var firstName = ""
    private var lastName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentEditProfileBinding.bind(view)
        bd = _bd!!

        setInit()
        setObserveData()
        setEvent()
    }

    private fun setInit() {
        //not implement yet
    }

    private fun setObserveData() {
        //not implement yet
    }

    private fun setEvent() {
        //not implement yet
    }
}