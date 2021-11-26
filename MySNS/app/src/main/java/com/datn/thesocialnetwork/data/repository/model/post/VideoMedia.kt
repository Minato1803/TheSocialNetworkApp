package com.datn.thesocialnetwork.data.repository.model.post

import android.os.Parcel

data class VideoMedia(
    override var uri: String?=null,
    override var ratio: String?=null,
    override var description: String="",
    override var reactCount: Int=0,
    override var commentCount: Int=0,
    override var shareCount: Int=0,
    var duration: Int = 0
) : Media(uri, ratio, description, reactCount, commentCount, shareCount) {

    override val toHashMap: HashMap<String, Any?>
        get() = hashMapOf(
            FIELD_DURATION to duration
        )

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(TYPE_VIDEO)
        super.writeToParcel(parcel, flags)
        parcel.writeInt(duration)
    }

    companion object{
        const val FIELD_DURATION = "duration"
    }
}