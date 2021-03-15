package com.prjun.millitalk

import android.widget.EditText

data class userData(
        var email: String? = null,
        var nick: String? = null,
        var age: String? = null,
        var birth_month: String? = null,
        var birth_day: String? = null,
        var schoolName: String? = null,
        var schoolGrade: Int? = null,
        var profileImg: String? = null,
        var profileMsg: String? = null,
        var friendsAmount: Int? = null,
        var followerAmount: Int? = null,
        var followingAmount: Int? = null
)