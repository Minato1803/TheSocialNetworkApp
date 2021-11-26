package com.datn.thesocialnetwork.data.repository.model.post

import android.os.Parcel
import android.os.Parcelable
import com.datn.thesocialnetwork.data.repository.model.chat_room.Room

abstract class Media(
    open var uri: String?,
    open var ratio: String?,
    open var description: String,
    open var reactCount: Int,
    open var commentCount: Int,
    open var shareCount: Int
) : Parcelable {
    open val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_URL to uri,
            FIELD_RATIO to ratio,
            FIELD_TYPE to if (this@Media is ImageMedia) TYPE_IMAGE else TYPE_VIDEO,
            FIELD_RATIO to ratio,
            FIELD_DESCRIPTION to description,
            FIELD_REACT_COUNT to reactCount,
            FIELD_COMMENT_COUNT to commentCount,
            FIELD_SHARE_COUNT to shareCount,
        )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri)
        parcel.writeString(ratio)
        parcel.writeString(description)
        parcel.writeInt(reactCount)
        parcel.writeInt(commentCount)
        parcel.writeInt(shareCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Media> {
        const val PAYLOAD_DISPLAY_MORE = 0
        const val PAYLOAD_RATIO = 2
        const val PAYLOAD_PREVIEW = 1

        const val FIELD_TYPE = "type"
        const val FIELD_URL = "url"
        const val FIELD_RATIO = "ratio"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_REACT_COUNT = "reactCount"
        const val FIELD_COMMENT_COUNT = "commentCount"
        const val FIELD_SHARE_COUNT = "shareCount"

        const val TYPE_IMAGE = 0
        const val TYPE_VIDEO = 1

        override fun createFromParcel(parcel: Parcel): Media {
            return when (parcel.readInt()) {
                TYPE_IMAGE -> ImageMedia(parcel)
                else -> VideoMedia(parcel)
            }
        }

        override fun newArray(size: Int): Array<ImageMedia?> {
            return arrayOfNulls(size)
        }
    }
}