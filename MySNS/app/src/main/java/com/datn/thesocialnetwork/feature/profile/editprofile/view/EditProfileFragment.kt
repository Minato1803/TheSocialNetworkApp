package com.datn.thesocialnetwork.feature.profile.editprofile.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.LoadingScreen
import com.datn.thesocialnetwork.core.api.Response
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.datn.thesocialnetwork.core.util.SystemUtils
import com.datn.thesocialnetwork.data.datasource.remote.model.UserDetail
import com.datn.thesocialnetwork.databinding.FragmentEditProfileBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.profile.editprofile.viewmodel.EditProfileViewModel
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.gun0912.tedpermission.PermissionListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    companion object {
        fun newInstance() = EditProfileFragment()
    }

    @Inject
    lateinit var mGlide: RequestManager
    private var _bd: FragmentEditProfileBinding? = null
    lateinit var bd: FragmentEditProfileBinding
    lateinit var mMainActivity: MainActivity

    private val mEditProfileViewModel: EditProfileViewModel by viewModels()
    private lateinit var mEditProfileHelper: EditProfileHelper
    private val user = GlobalValue.USER!!

    var imgAvatarBitmap: Bitmap? = null
    lateinit var actionBarDrawerToggle : ActionBarDrawerToggle
    private var gender = ""
    private var birthday = ""
    private var firstName = ""
    private var lastName = ""
    private var userName = ""
    private var descrip = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainActivity = activity as MainActivity
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentEditProfileBinding.bind(view)
        bd = _bd!!

        setInit()
        setObserveData()
        setEvent()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_edit, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_save -> {
                clickUpdate()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clickUpdate() {
        setDataInput()
        setValidData()
        val imgAvatarByte: ByteArray? = imgAvatarBitmap?.let { SystemUtils.convertBitmapToJPG(it) }
        val userDetail = UserDetail(
            userName = userName,
            firstName = firstName,
            lastName = lastName,
            description = descrip,
            birthday = birthday,
            gender = gender,
            avatarUrl = user.userDetail.avatarUrl
        )
        mEditProfileViewModel.updateUserDetail(user.uidUser, userDetail, imgAvatarByte)
    }

    private fun setValidData() {
        //
    }

    private fun setDataInput() {
        gender = bd.tvGender.text.toString().trim()
        birthday = bd.tvBirthday.text.toString().trim()
        userName = bd.edtEditUsername.text.toString().trim()
        firstName = bd.edtFirstName.text.toString().trim()
        lastName = bd.edtLastName.text.toString().trim()
        descrip = bd.edtDescription.text.toString().trim()
    }

    private fun setInit() {
        mMainActivity.bd.toolbar.title = "Sửa thông tin cá nhân"
        mMainActivity.bd.toolbar.navigationIcon = resources.getDrawable(R.drawable.ic_arrow_back_24)
        mMainActivity.bd.bottomAppBar.visibility = View.GONE
        mMainActivity.bd.fabAdd.visibility = View.GONE
        mEditProfileHelper = EditProfileHelper(this)
        setDataOutput()
    }

    private fun setDataOutput() {
        mEditProfileHelper.setAvatarOutput(user.userDetail.avatarUrl)
        bd.tvBirthday.text = user.userDetail.birthday
        bd.tvGender.text = user.userDetail.gender

        bd.edtFirstName.setText(user.userDetail.firstName)
        bd.edtLastName.setText(user.userDetail.lastName)
        bd.edtEditUsername.setText(user.userDetail.userName)
        bd.edtDescription.setText(user.userDetail.description)
        bd.edtEmail.setText(user.userDetail.email)
    }

    private fun setObserveData() {
        mEditProfileViewModel.liveDataUpdateUserDetail.observe(viewLifecycleOwner, { response ->
            observeUpdateUserDetail(response)
        })
    }

    private fun observeUpdateUserDetail(response: Response<UserDetail>?) {
        when (response) {
            is Response.Loading -> LoadingScreen.show(context)

            is Response.Error -> {
                LoadingScreen.hide()
                SystemUtils.showDialogError(context, response.message)
            }

            is Response.Success -> {
                LoadingScreen.hide()
                response.data?.let { userDetail ->
                    GlobalValue.USER?.userDetail = userDetail
                    SystemUtils.showMessage(context, getString(R.string.str_message_update_success))
                }
            }
        }
    }

    private fun setEvent() {
        bd.imgAvatar.setOnClickListener { clickAvatar() }
        bd.tvGender.setOnClickListener { clickGender() }
        bd.tvBirthday.setOnClickListener { clickBirthDay() }
        mMainActivity.bd.toolbar.setOnClickListener { clickActionbar() }
        mMainActivity.bd.toolbar.setNavigationOnClickListener {
            mMainActivity.onBackPressed()
        }
    }

    private fun clickActionbar() {
        val profileFrag = ProfileFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, profileFrag,"sendToProfileFragment")
            .commit()
    }

    private fun clickBirthDay() {
        mEditProfileHelper.showDialogDatePicker()
    }

    private fun clickGender() {
        mEditProfileHelper.showBottomSheetGender()
    }

    private fun clickAvatar() {
        SystemUtils.requestPermission(context,
            GlobalValue.listPermissionSetAvatar,
            object : PermissionListener {
                override fun onPermissionGranted() {
                    mEditProfileHelper.showBottomSheetSetAvatar()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(context,
                        getString(R.string.str_deny_message),
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
}