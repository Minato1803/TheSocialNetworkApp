package com.datn.thesocialnetwork.feature.notification

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

class Notification constructor(base: Context?) : ContextWrapper(base) {

    val ID = "some_id"
    val NAME = "FirebaseAPP"

    var notificationManager: NotificationManager? = null
    fun create() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createChannel() {
        val notificationChannel =
            NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        getManager()?.createNotificationChannel(notificationChannel)
    }

    fun getManager(): NotificationManager? {
        if (notificationManager == null) {
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        }
        return notificationManager
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getONotification(
        title: String?,
        body: String?,
//        pIntent: PendingIntent?,
        soundUri: Uri?,
        icon: String,
    ): Notification.Builder? {
        return Notification.Builder(applicationContext, ID)
//            .setContentIntent(pIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setSmallIcon(icon.toInt())
    }
}