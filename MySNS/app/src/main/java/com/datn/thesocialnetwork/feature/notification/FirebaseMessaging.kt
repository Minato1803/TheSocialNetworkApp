package com.datn.thesocialnetwork.feature.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.util.GlobalValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

internal class FirebaseMessaging : FirebaseMessagingService() {

    override  fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("TAG", "Message Notification Body: " + remoteMessage.notification!!.body)
        val notificationType: String? = remoteMessage.data["notificationType"]
        if (notificationType == "PostNotification") {
            val sender: String? = remoteMessage.data["sender"]
            val pId: String? = remoteMessage.data["pId"]
            val pTitle: String? = remoteMessage.data["pTitle"]
            val pDescription: String? = remoteMessage.data["pDescription"]
            if (sender != GlobalValue.USER?.uidUser) {
                showPostNotification("" + pId, "" + pTitle, "" + pDescription)
            }
        } else if (notificationType == "ChatNotification") {
            val sent: String? = remoteMessage.data["sent"]
            val user: String? = remoteMessage.data["user"]
            val fUser = FirebaseAuth.getInstance().currentUser
            if (fUser != null && sent == fUser.uid) {
                if (GlobalValue.USER!!.uidUser != user) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        sendOreoNotification(remoteMessage)
                    } else {
                        sendNormalNotification(remoteMessage)
                    }
                }
            }
        }
    }

    private fun showPostNotification(pId: String, pTitle: String, pDescription: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupPostNotificationChannel(notificationManager)
        }
        //go to detail post
//        intent.putExtra("postId", pId)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_image_temp)
        val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, "" + ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_image_temp)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setContentText(pDescription)
                .setSound(notificationSoundUri)
//                .setContentIntent(pendingIntent)
        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPostNotificationChannel(notificationManager: NotificationManager?) {
        val channelName: CharSequence = "New Notification"
        val channelDescription = "Device to device post notification"
        val adminChannel =
            NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = channelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

    private fun sendNormalNotification(remoteMessage: RemoteMessage) {
        val user: String? = remoteMessage.data["user"]
        val icon: String? = remoteMessage.data["icon"]
        val title: String? = remoteMessage.data["title"]
        val body: String? = remoteMessage.data["body"]
        val notification: RemoteMessage.Notification? = remoteMessage.notification
        val i = user?.replace("[\\D]".toRegex(), "")?.toInt()
        // goto message
        val bundle = Bundle()
        bundle.putString("hisUid", user)
//        val pIntent = i?.let {
//            PendingIntent.getActivity(this,
//                it, intent, PendingIntent.FLAG_ONE_SHOT)
//        }
        val defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = icon?.let {
            NotificationCompat.Builder(this)
                .setSmallIcon(it.toInt())
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
        }
//            .setContentIntent(pIntent)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var j = 0
        if (i != null) {
            if (i > 0) {
                j = i
            }
        }
        if (builder != null) {
            notificationManager.notify(j, builder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user: String? = remoteMessage.data["user"]
        val icon: String? = remoteMessage.data["icon"]
        val title: String? = remoteMessage.data["title"]
        val body: String? = remoteMessage.data["body"]
        val notification: RemoteMessage.Notification? = remoteMessage.notification
        val i = user?.replace("[\\D]".toRegex(), "")?.toInt()
        val bundle = Bundle()
        bundle.putString("hisUid", user)
        val defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification1 = Notification(this)
        notification1.create()
        val builder: Notification.Builder? =
            icon?.let { notification1.getONotification(title, body, defSoundUri, it) }
        var j = 0
        if (i != null) {
            if (i > 0) {
                j = i
            }
        }
        if (builder != null) {
            notification1.getManager()?.notify(j, builder.build())
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateToken(s)
        }
    }

    private fun updateToken(tokenRefresh: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(tokenRefresh)
        ref.child(user!!.uid).setValue(token)
    }

    companion object {
        private const val ADMIN_CHANNEL_ID = "admin_channel"
    }
}