package com.datn.thesocialnetwork.core.validate

import android.util.Patterns

object Validation {
    fun isValidEmail(s: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(s).matches()
    }
}