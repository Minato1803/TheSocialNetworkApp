package com.datn.thesocialnetwork.data.datasource.local.sharedprefs

import android.content.SharedPreferences
import com.datn.thesocialnetwork.core.util.KEY
import javax.inject.Inject
import javax.inject.Named

class LoginSharedPrefs @Inject constructor(
    @Named(KEY.LOGIN) private val mPrefsLogin: SharedPreferences,
) {
    private val editor = mPrefsLogin.edit()

    fun getUserId() = mPrefsLogin.getString(KEY.USER_ID, "") ?: ""

    fun saveIdLogin(userId: String?) {
        editor.putString(KEY.USER_ID, userId)
        editor.apply()
    }
}