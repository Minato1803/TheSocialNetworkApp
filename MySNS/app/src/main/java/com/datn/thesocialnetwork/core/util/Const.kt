package com.datn.thesocialnetwork.core.util

object Const {

    // Retrofit config.
    const val READ_TIMEOUT = 5_000L
    const val WRITE_TIMEOUT = 5_000L
    const val CONNECT_TIMEOUT = 10_000L

    // google SignIn config
    const val WEB_CLIENT_ID =
        "325160175322-ok0cgdcnnd5o2vtls8nkatk9pjsm63bs.apps.googleusercontent.com"

    const val avatarDefaultUrl =
        "https://firebasestorage.googleapis.com/v0/b/mysns-3ef38.appspot.com/o/MySNS%2Favatar%2Favatar_default.png?alt=media&token=c422d047-dba9-4069-aafe-94d67f44026c"


}

object FirebaseNode {
    const val user = "user"

    const val uidUser = "uidUser"
    const val uidGoogle = "uidGoogle"
    const val firstName = "firstName"
    const val lastName = "lastName"
    const val birthday = "birthday"
    const val gender = "gender"
    const val email = "email"
    const val password = "password"
    const val avatarUrl = "avatarUrl"
}