package com.datn.thesocialnetwork.feature.post.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.datn.thesocialnetwork.data.repository.PostRepository
import com.datn.thesocialnetwork.feature.post.adapter.Image
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val repository: PostRepository,
    application: Application
) : AndroidViewModel(application)
{
    val _allImagesFromGallery: MutableStateFlow<List<Uri>> = MutableStateFlow(listOf())

    private val _selectedImage: MutableStateFlow<List<Uri>?> = MutableStateFlow(null)
    val _capturedImage: MutableStateFlow<Uri?> = MutableStateFlow(null)

    private var wasImagesQueried = false

    val allImagesFromGallery: Flow<List<Image>?> = _allImagesFromGallery.combine(
        _selectedImage
    ) { all, selected ->
        if (!wasImagesQueried)
        {
            null
        }
        else
        {
            if (selected != null)
            {
                all.map {
                    Image(it, it == selected)
                }
            }
            else
            {
                all.map {
                    Image(it, false)
                }
            }
        }
    }



    fun selectImageFromGallery(uriList: List<Uri>)
    {
        _capturedImage.value = null
        _selectedImage.value = uriList
    }

    fun captureImage(uri: Uri)
    {
        _capturedImage.value = uri
    }

    fun unSelectImage()
    {
        _selectedImage.value = null
        _capturedImage.value = null
    }

    fun getAllImages(): List<Uri>
    {
        wasImagesQueried = true

        val allImages = mutableListOf<Uri>()

        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID
        )

        val imageSortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = getApplication<Application>().applicationContext.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            imageProjection,
            null,
            null,
            imageSortOrder
        )

        cursor.use {

            if (cursor != null)
            {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext())
                {
                    allImages.add(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(idColumn)
                        )
                    )
                }
            }
        }
        return allImages
    }

    fun loadAllImagesFromGallery()
    {
        viewModelScope.launch {
            _allImagesFromGallery.value = withContext(Dispatchers.IO) {
                getAllImages()
            }
        }
    }

    @ExperimentalCoroutinesApi
    fun postImage(
        listUri: ArrayList<Uri>,
        desc: String,
        hashtags: List<String>,
        mentions: List<String>,
    ) = repository.uploadPost(
        listUri = listUri,
        desc = desc,
        hashtags = hashtags,
        mentions = mentions,
    )


}