package com.prjun.millitalk

import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.HtmlCompat
import com.google.common.io.Files.append
import kotlinx.android.synthetic.main.activity_user_sign_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.regex.Matcher
import java.util.regex.Pattern


class userSignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_sign_up)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //다크모드 미적용

        val scope = CoroutineScope(Dispatchers.Main)

        var spinnerItems = arrayOf("아이템0")

        var myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems)

        schoolSpinner.adapter = myAdapter


        schoolSearchBtn.setOnClickListener {
            var spinnerText: String = schoolSpinner.getSelectedItem().toString()
            val url = "https://open.neis.go.kr/hub/schoolInfo?KEY=3fba6a4954ea4992a90d541cf103f654&Type=json&pIndex=1&pSize=5&SCHUL_NM=${signupSchoolNameText.text}".trimIndent()
            //NEIS 학교검색 Api
            spinnerItems = arrayOf()
                var NEISParsing = Thread(Runnable {
                    try {
                    var jObject =
                        Jsoup.connect(url).ignoreContentType(true).get().select("body").text()
                    var result = JSONObject(jObject).getJSONArray("schoolInfo")
                        Log.d("Zone1", result.toString())
                    var search_head = result.getJSONObject(0)
                        Log.d("Zone2", search_head.toString())
                    //head
                    var search_SchoolAmount = search_head.getJSONArray("head")
                        .getJSONObject(0).getString("list_total_count")
                        Log.d("Zone3", search_SchoolAmount.toString())
                    //검색된 학교수(최소 1곳이상)
                    var search_row = result.getJSONObject(1)
                        Log.d("Zone4", search_row.toString())
                        //row
                    for (i in 0 until search_row.getJSONArray("row").length()) {
                        var search_SchoolName =
                            search_row.getJSONArray("row").getJSONObject(i).getString("SCHUL_NM")
                        //학교이름
                        Log.d("forCount",i.toString())
                        Log.d("SchoolName", search_SchoolName)
                        spinnerItems += search_SchoolName
                        Log.d("spinnerArray",spinnerItems[i])
                        Log.d("rowlength",search_row.getJSONArray("row").length().toString())
                        scope.launch{
                            myAdapter = ArrayAdapter(
                                this@userSignUp,
                                android.R.layout.simple_spinner_dropdown_item,
                                spinnerItems
                            )
                            schoolSpinner.adapter = myAdapter
                        }
                    }
                    }catch(e:Exception){
                        spinnerItems = arrayOf("검색된 학교가 없습니다.")
                        Log.d("parseError", e.toString())
                        Log.d("parseErrorLine", e.printStackTrace().toString())
                        Log.d("spinnerItems", spinnerItems[0])
                        scope.launch {
                            myAdapter = ArrayAdapter(
                                this@userSignUp,
                                android.R.layout.simple_spinner_dropdown_item,
                                spinnerItems
                            )
                        }
                    }
                })
                NEISParsing.start()


            Log.d("spinner선택값",spinnerText)

        }


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