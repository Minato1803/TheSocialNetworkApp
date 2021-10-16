package com.datn.thesocialnetwork.feature.profile.editprofile.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.Const
import com.datn.thesocialnetwork.databinding.BottomSheetDialogAvatarBinding
import com.datn.thesocialnetwork.databinding.BottomSheetDialogGenderBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class EditProfileHelper(
    private val parent: EditProfileFragment
) {
    @SuppressLint("InflateParams")
    fun showBottomSheetGender() {
        val bottomSheetDialogGender = BottomSheetDialog(parent.requireContext())
        val bdBtsDialogGender =
            BottomSheetDialogGenderBinding.bind(parent.mMainActivity.layoutInflater.inflate(R.layout.bottom_sheet_dialog_gender,
                null))
        bottomSheetDialogGender.setContentView(bdBtsDialogGender.root)
        bottomSheetDialogGender.show()

        bdBtsDialogGender.tvMale.setOnClickListener {
            parent.bd.tvGender.text = bdBtsDialogGender.tvMale.text
            bottomSheetDialogGender.dismiss()
        }
        bdBtsDialogGender.tvFemale.setOnClickListener {
            parent.bd.tvGender.text = bdBtsDialogGender.tvFemale.text
            bottomSheetDialogGender.dismiss()
        }
        bdBtsDialogGender.tvCancel.setOnClickListener {
            bottomSheetDialogGender.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    fun showBottomSheetSetAvatar() {
        val bottomSheetDialogAvatar = BottomSheetDialog(parent.requireContext())
        val bdBtsDialogAvatar =
            BottomSheetDialogAvatarBinding.bind(parent.mMainActivity.layoutInflater.inflate(R.layout.bottom_sheet_dialog_avatar,
                null))
        bottomSheetDialogAvatar.setContentView(bdBtsDialogAvatar.root)
        bottomSheetDialogAvatar.show()

        bdBtsDialogAvatar.tvTakePhoto.setOnClickListener {
            bottomSheetDialogAvatar.dismiss()
            clickTakePhoto()
        }
        bdBtsDialogAvatar.tvChooseImage.setOnClickListener {
            bottomSheetDialogAvatar.dismiss()
            clickChooseImage()
        }
        bdBtsDialogAvatar.tvCancel.setOnClickListener {
            bottomSheetDialogAvatar.dismiss()
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun showDialogDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat(Const.DATE_FORMAT)
        val datePickerDialog = DatePickerDialog(parent.requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                parent.bd.tvBirthday.text = (dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun setAvatarOutput(bitmap: Bitmap?) {
        parent.mGlide
            .load(bitmap)
            .placeholder(R.drawable.ic_edit_avatar)
            .error(R.drawable.ic_edit_avatar)
            .fitCenter()
            .into(parent.bd.imgAvatar)
    }

    fun setAvatarOutput(url: String) {
        parent.mGlide
            .load(url)
            .placeholder(R.drawable.ic_edit_avatar)
            .error(R.drawable.ic_edit_avatar)
            .fitCenter()
            .into(parent.bd.imgAvatar)
    }

    private fun clickChooseImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_PICK
        }
        resultActivityPickImage.launch(intent)
    }

    private fun clickTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultActivityTakePhoto.launch(intent)
    }

    private val resultActivityPickImage =
        parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { dataNN ->
                    parent.imgAvatarBitmap =
                        BitmapFactory.decodeStream(parent.context?.contentResolver?.openInputStream(dataNN))
                    setAvatarOutput(parent.imgAvatarBitmap)
                }
            }
        }

    private val resultActivityTakePhoto =
        parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { dataNN ->
                    parent.imgAvatarBitmap = dataNN.extras?.get("data") as Bitmap
                    setAvatarOutput(parent.imgAvatarBitmap)
                }
            }
        }
}