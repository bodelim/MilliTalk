package com.prjun.millitalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.util.Linkify
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.activity_user_sign_up.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class userSignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_sign_up)

        //HTML 적용
        acceptText.text = HtmlCompat.fromHtml(getString(R.string.a), HtmlCompat.FROM_HTML_MODE_COMPACT)

//Transform 정의
        val transform =
            Linkify.TransformFilter(object : Linkify.TransformFilter, (Matcher, String) -> String {
                override fun transformUrl(p0: Matcher?, p1: String?): String {
                    return ""
                }

                override fun invoke(p1: Matcher, p2: String): String {
                    return ""
                }

            })

        //링크달 패턴 정의
        val pattern = Pattern.compile("개인정보처리방침")
        Linkify.addLinks(
            acceptText,
            pattern,
            "http://millitalkpolicy.kro.kr/",
            null,
            transform
        )

    }
}