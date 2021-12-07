package com.datn.thesocialnetwork.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.datn.thesocialnetwork.R
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.max
import kotlin.math.min

object SystemUtils {

    fun hasInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun hideKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun showKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun showMessage(context: Context?, message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showDialogNoInternetConnection(context: Context) {
        AlertDialog.Builder(context)
            .setTitle(R.string.str_error)
            .setMessage(R.string.str_error_socket_timeout)
            .setNegativeButton(context.getString(R.string.str_cancel)
            ) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun showDialogError(context: Context?, _message: String) {
        context?.let {
            var message: String = it.getString(R.string.str_error_default)
            if (_message.isNotBlank()) message = _message
            AlertDialog.Builder(it)
                .setTitle(R.string.str_error)
                .setMessage(message)
                .setNegativeButton(context.getString(R.string.str_cancel)
                ) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    fun requestPermission(
        context: Context?,
        listPermission: Array<String?>,
        permissionListener: PermissionListener?,
    ) {
        context?.let {
            TedPermission.with(context)
                .setPermissionListener(permissionListener)
                .setPermissions(*listPermission)
                .setDeniedTitle(context.getString(R.string.str_denied_title_permission))
                .setDeniedMessage(context.getString(R.string.str_denied_message_permission))
                .check()
        }
    }

    fun convertBitmapToJPG(imgBitmap: Bitmap): ByteArray {
        val bytes = ByteArrayOutputStream()
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytes)
        return bytes.toByteArray()
    }

    fun vectorToBitmap(
        context: Context,
        @DrawableRes id: Int,
        @ColorInt color: Int,
    ): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, null)
        if (vectorDrawable == null) {
            Log.e("BitmapHelper", "Resource not found")
            return BitmapDescriptorFactory.defaultMarker()
        }
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun signOut(mGoogleSignInClient: GoogleSignInClient, context: Context) {
        mGoogleSignInClient.signOut()
        Firebase.auth.signOut()
        GlobalValue.USER = null
        val prefs = context.getSharedPreferences(KEY.USER, AppCompatActivity.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove(KEY.USER)
        editor.apply()
    }

    fun getAdjustedRatio(ratio: String): String {
        val size = getSize(ratio)
        val floatRatio = size.width / size.height.toFloat()
        val adjustedRatio = min(max(floatRatio, 0.8f), 2f)
        val newHeight: Int
        val newWidth: Int
        if (size.width > size.height) {
            newHeight = size.height
            newWidth = (newHeight * adjustedRatio).toInt()
        } else {
            newWidth = size.width
            newHeight = (newWidth / adjustedRatio).toInt()
        }
        return "$newWidth:$newHeight"
    }

    //width , height
    fun getSize(ratio: String): Size {
        return ratio.split(":").let { Size(it[0].toInt(), it[1].toInt()) }
    }

    //callTime: seconds
    fun getVideoDurationFormat(duration: Int): String {
        var time = duration.toLong()
        val res = StringBuilder()

        if (time > 3600) {
            val hours = time / 3600
            res.append("${if (hours < 10) "0" else ""}$hours:")
            time -= hours * 3600
        }

        val mins = time / 60
        res.append("${if (mins < 10) "0" else ""}$mins:")
        time -= mins * 60

        val secs = time
        res.append("${if (secs < 10) "0" else ""}$secs")
        return res.toString()
    }

    private val uriCache = HashMap<String, Uri>()

    fun getUri(uriString: String): Uri {
        var res = uriCache[uriString]
        if (res == null) {
            res = Uri.parse(uriString)
            uriCache[uriString] = res
        }
        return res!!
    }

    fun String.normalize(): String = this.lowercase(Locale.ENGLISH).trim()

    fun String.formatQuery(): String = this.lowercase(Locale.getDefault()).trim()

    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private val onlyEmojiRegex = "[^\\p{L}\\p{N}\\p{P}\\p{Z}]".toRegex()
    val String.isOnlyEmoji
        get() = replace(onlyEmojiRegex, "") == ""

    fun Long.formatWithSpaces(): String {
        val sb = StringBuilder().append(this)

        for (i in sb.length - 3 downTo 1 step 3) {
            sb.insert(i, ' ')
        }

        return sb.toString()
    }

    fun Activity.getShareIntent(text: String): Intent {
        return ShareCompat.IntentBuilder.from(this)
            .setText(text)
            .setType("text/plain").intent
    }
}