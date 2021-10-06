package com.datn.thesocialnetwork.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.datn.thesocialnetwork.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.ByteArrayOutputStream

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
        @ColorInt color: Int
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
}