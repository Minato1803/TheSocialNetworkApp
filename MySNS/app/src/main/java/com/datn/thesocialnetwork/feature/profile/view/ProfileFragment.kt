package com.datn.thesocialnetwork.feature.profile.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.databinding.FragmentHomeBinding
import com.datn.thesocialnetwork.databinding.FragmentProfileBinding
import com.datn.thesocialnetwork.feature.profile.editprofile.view.EditProfileFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        setEvent()
        return binding!!.root
    }

    private fun setEvent() {
//        binding!!.txtFullName.setOnClickListener {
            EditProfileFragment.newInstance()
                .show(childFragmentManager,"editProfileFragment")
//        }
    }

}