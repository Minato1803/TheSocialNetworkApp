package com.datn.thesocialnetwork.core.util

import android.view.Gravity
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

object ViewUtils {
    fun CoordinatorLayout.showSnackbarGravity(
        message: String,
        buttonText: String? = null,
        action: () -> Unit = {},
        length: Int = Snackbar.LENGTH_LONG,
        gravity: Int = Gravity.TOP
    )
    {
        val s = Snackbar
            .make(this, message, length)

        buttonText?.let {
            s.setAction(it) {
                action()
            }
        }

        val params = s.view.layoutParams as CoordinatorLayout.LayoutParams
        params.gravity = gravity
        s.view.layoutParams = params
        s.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE

        s.show()
    }
    fun AppCompatActivity.setActionBarTitle(title: String)
    {
        supportActionBar?.title = title
    }

    fun Fragment.setActionBarTitle(@StringRes title: Int)
    {
        (requireActivity() as? AppCompatActivity)?.setActionBarTitle(getString(title))
    }
}