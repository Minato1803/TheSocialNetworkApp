package com.datn.thesocialnetwork.feature.post.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.bumptech.glide.RequestManager
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.FirebaseStatus
import com.datn.thesocialnetwork.core.listener.BaseOnEventListener
import com.datn.thesocialnetwork.core.util.SystemUtils.hideKeyboard
import com.datn.thesocialnetwork.core.util.ViewUtils.setViewAndChildrenEnabled
import com.datn.thesocialnetwork.core.util.ViewUtils.showSnackbarGravity
import com.datn.thesocialnetwork.databinding.FragmentCreatePostBinding
import com.datn.thesocialnetwork.feature.main.view.MainActivity
import com.datn.thesocialnetwork.feature.post.adapter.PostFeedAdapter
import com.datn.thesocialnetwork.feature.post.viewmodel.CreatePostViewModel
import com.datn.thesocialnetwork.feature.profile.view.ProfileFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CreatePostFragment : DialogFragment(R.layout.fragment_create_post), BaseOnEventListener {

    companion object {
        fun newInstance(): CreatePostFragment {
            return CreatePostFragment()
        }
    }

    @Inject
    lateinit var  postFeedAdapter: PostFeedAdapter

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var glide: RequestManager

    private var _bd: FragmentCreatePostBinding? = null
    lateinit var binding: FragmentCreatePostBinding
    lateinit var mMainActivity: MainActivity
    private val viewModel: CreatePostViewModel by activityViewModels()

    private lateinit var uri: Uri
    private lateinit var uriPhoto: Uri
    private var uriPhotoList = arrayListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogTheme)
        mMainActivity = activity as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _bd = FragmentCreatePostBinding.bind(view)
        binding = _bd!!

        setInit()
        checkReadStoragePermAndLoadImages()
        setObserveData()
        setEvent()
    }

    private fun setObserveData() {
        lifecycleScope.launchWhenStarted {
            _uploadStatus.collectLatest {
                when (it) {
                    FirebaseStatus.Sleep -> Unit// do nothing
                    FirebaseStatus.Loading -> {
                        setLoadingState(true)
                    }
                    is FirebaseStatus.Failed -> {
                        setLoadingState(false)
                        binding.host.showSnackbarGravity(
                            message = it.message.getFormattedMessage(requireContext()),
                            length = Snackbar.LENGTH_SHORT,
                            buttonText = getString(R.string.ok)
                        )
                        hideKeyboard(requireContext())
                    }
                    is FirebaseStatus.Success -> {
                        setLoadingState(false)
                        binding.host.showSnackbarGravity(
                            message = it.message.getFormattedMessage(requireContext()),
                            length = Snackbar.LENGTH_SHORT,
                            buttonText = getString(R.string.see),
                            action = {
                                val fragProfile = ProfileFragment()
                                navigateFragment(fragProfile, "ProfileFragment")
                                this@CreatePostFragment.dismiss()
                            }
                        )

                        viewModel.unSelectImage()
                        binding.edTxtDesc.setText("")
                        hideKeyboard(requireContext())
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.allImagesFromGallery.collectLatest {
                if (it == null)
                {
                    binding.txtEmptyResult.isVisible = false
                }
                else
                {
                    if (it.isNotEmpty())
                    {
                        setStoragePermissionStatus(true)
                        binding.txtEmptyResult.isVisible = false
                        getAllPhotos()
                    }
                    else // show empty state
                    {
                        binding.txtEmptyResult.isVisible = true
                    }
                }
            }
        }
    }

    private fun setEvent() {
        binding.postUpload.setOnClickListener {
            if (postFeedAdapter.multipleArray.size <= 1) {
                val uriList: ArrayList<Uri> = ArrayList()
                if (postFeedAdapter.multipleArray.size == 1) {
                    uriList.add(postFeedAdapter.multipleArray[0])
                } else {
                    postFeedAdapter.defaultUrl?.let { it1 -> uriList.add(it1) }
                }
                uploadPost(uriList)
            } else {
                uploadPost(postFeedAdapter.multipleArray)
            }
            setObserveData()
        }

        binding.postFeedCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takeImageFromCamera()
            } else {
                requestCameraPermissions.launch(Manifest.permission.CAMERA)
            }
        }

        binding.postFeedMultiplePhotos.setOnClickListener {
            if (!postFeedAdapter.multiple) {
                binding.postFeedMultiplePhotos.setBackgroundResource(R.drawable.feed_selected_button_background)
                postFeedAdapter.multiple = true
            } else {
                binding.postFeedMultiplePhotos.setBackgroundResource(R.drawable.feed_unselected_button_background)
                postFeedAdapter.multiple = false
            }
            postFeedAdapter.notifyDataSetChanged()
        }

        binding.btnCancel.setOnClickListener {
            this.dismiss()
        }
    }

    private fun setInit() {
//        mMainActivity.bd.appBarLayout.visibility = View.GONE
//        mMainActivity.bd.bottomAppBar.isVisible = false
//        mMainActivity.bd.fabAdd.isVisible = false
    }

    private fun getAllPhotos() {
        val uriArr = viewModel._allImagesFromGallery.value
        postFeedAdapter.uriArr = uriArr
        postFeedAdapter.preview = binding.postFeedImageView
        binding.postFeedGalleryRecyclerView.adapter = postFeedAdapter

        val request = ImageRequest.Builder(requireContext())
            .data(uriArr?.get(0))
            .target { drawable ->
                binding.postFeedImageView.setImageDrawable(drawable)
            }
            .build()

        imageLoader.enqueue(request)
//
//        glide
//            .load(uriArr[0])
//            .into(binding.postFeedImageView)

    }

    private val _uploadStatus: MutableStateFlow<FirebaseStatus> = MutableStateFlow(
        FirebaseStatus.Sleep
    )

    private fun uploadPost(uriList: ArrayList<Uri>) {
        /**
         * If status is equal to [FirebaseStatus.Loading]
         * do not upload new post
         */
        if (_uploadStatus.value != FirebaseStatus.Loading) {
            lifecycleScope.launch {
                viewModel.postImage(
                    listUri = uriList,
                    desc = binding.edTxtDesc.text.toString(),
                    hashtags = binding.edTxtDesc.hashtags,
                    mentions = binding.edTxtDesc.mentions
                ).collectLatest { uploadStatus ->
                    _uploadStatus.value = uploadStatus
                }
            }
        } else {
            // skip
        }
    }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
            if (isSaved) {
                viewModel.captureImage(uri)
                val request = ImageRequest.Builder(requireContext())
                    .data(viewModel._capturedImage.value)
                    .target { drawable ->
                        binding.postFeedImageView.setImageDrawable(drawable)
                    }
                    .build()

                imageLoader.enqueue(request)
            }
        }

    private fun takeImageFromCamera() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val photoFile = File.createTempFile(
            "IMG_${timeStamp}_",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        uriPhotoList.add(uri)

        takePicture.launch(uri)
    }

    /**permission*/

    private fun setStoragePermissionStatus(isPermissionGranted: Boolean) {
        binding.postFeedGalleryRecyclerView.isVisible = isPermissionGranted
        binding.txtAStoragePermission.isVisible = !isPermissionGranted
    }

    private fun checkReadStoragePermAndLoadImages() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setStoragePermissionStatus(true)
            viewModel.loadAllImagesFromGallery()
        } else {
            setStoragePermissionStatus(false)
        }
    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takeImageFromCamera()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.CAMERA
                    )
                ) {
                    //never ask again
                    (requireActivity() as MainActivity).showSnackbar(
                        message = getString(R.string.message_camera_never_ask),
                        buttonText = getString(R.string.settings),
                        action = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts(
                                        "package",
                                        requireContext().packageName,
                                        null
                                    )
                                }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

    private val requestStoragePermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.loadAllImagesFromGallery()
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    //never ask again
                    (requireActivity() as MainActivity).showSnackbar(
                        message = getString(R.string.message_storage_never_ask),
                        buttonText = getString(R.string.settings),
                        action = {
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts(
                                        "package",
                                        requireContext().packageName,
                                        null
                                    )
                                }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

    private fun setLoadingState(isLoading: Boolean) {
        with(binding)
        {
            progressBarUpload.isVisible = isLoading
            mainBody.alpha = if (isLoading) 0.5f else 1f
            mainBody.setViewAndChildrenEnabled(!isLoading)
        }
    }

    fun navigateFragment(fragment: Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(id, fragment, "tag")
            .commit()
    }
}