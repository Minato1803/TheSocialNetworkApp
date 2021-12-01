package com.datn.thesocialnetwork.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.datn.thesocialnetwork.R
import com.datn.thesocialnetwork.core.api.status.GetStatus
import com.datn.thesocialnetwork.core.util.SystemUtils.showMessage
import com.datn.thesocialnetwork.databinding.StateRecyclerBinding
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

    fun Fragment.setActionBarTitle(title: String)
    {
        (requireActivity() as? AppCompatActivity)?.setActionBarTitle(title)
    }

    fun View.setViewAndChildrenEnabled(isEnabled: Boolean)
    {
        this.isEnabled = isEnabled

        (this as? ViewGroup)?.let { viewGroup ->
            viewGroup.children.forEach { view ->
                view.setViewAndChildrenEnabled(isEnabled)
            }
        }
    }
    public inline fun View.updatePadding(
        @Px left: Int = paddingLeft,
        @Px top: Int = paddingTop,
        @Px right: Int = paddingRight,
        @Px bottom: Int = paddingBottom
    ) {
        setPadding(left, top, right, bottom)
    }
    public inline fun View.setPadding(@Px size: Int) {
        setPadding(size, size, size, size)
    }

    fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
        FragmentViewBindingDelegate(this, viewBindingFactory)

    fun Context.tryOpenUrl(
        url: String,
        errorCallback: () -> Unit = {
            showMessage(this,getString(R.string.could_not_open_browser))
        }
    )
    {
        var webPage = Uri.parse(url)

        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            webPage = Uri.parse("https://$url")
        }

        val browserIntent =
            Intent(
                Intent.ACTION_VIEW,
                webPage
            )

        try
        {
            startActivity(browserIntent)
        }
        catch (ex: ActivityNotFoundException)
        {
            errorCallback()
        }
    }
}