package com.prjun.millitalk

import android.content.Intent
import android.os.Bundle
import android.text.util.Linkify
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.HtmlCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user_sign_up.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.lang.reflect.Field
import java.util.regex.Matcher
import java.util.regex.Pattern


class userSignUp : AppCompatActivity() {

    private var firebaseAuth: FirebaseAuth? = null

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_sign_up)

        firebaseAuth = FirebaseAuth.getInstance()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //다크모드 미적용

        val spinnerBirth_MM = arrayOf("월",1,2,3,4,5,6,7,8,9,10,11,12)
        val spinnerBirth_DD = arrayOf("일",1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31)

        val BirthMM_spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerBirth_MM)
        val BirthDD_spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerBirth_DD)

        Birth_MM.adapter = BirthMM_spinnerAdapter
        Birth_DD.adapter = BirthDD_spinnerAdapter

        try {
            val popup: Field = Spinner::class.java.getDeclaredField("mPopup")
            popup.setAccessible(true)
            val window: ListPopupWindow = popup.get(Birth_MM) as ListPopupWindow
            window.setHeight(300) //pixel
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        try {
            val popup: Field = Spinner::class.java.getDeclaredField("mPopup")
            popup.setAccessible(true)
            val window: ListPopupWindow = popup.get(Birth_DD) as ListPopupWindow
            window.setHeight(300) //pixel
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        val user = Firebase.auth.currentUser

        val scope = CoroutineScope(Dispatchers.Main)

        var spinnerItems = arrayOf("학교검색후에 여기서 선택!")

        var myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems)

        schoolSpinner.adapter = myAdapter

        var spinnerSchool: String = schoolSpinner.getSelectedItem().toString()
        var spinnerText_BirthMM: String = Birth_MM.getSelectedItem().toString()
        var spinnerText_BirthDD: String = Birth_DD.getSelectedItem().toString()
        //선택한 스피너의 값

        signupSchoolNameText.setOnClickListener{
            val intent = Intent(this, SchoolSearch::class.java)
            startActivity(intent)
        }


        schoolSearchBtn.setOnClickListener {
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
                        scope.launch{
                            myAdapter = ArrayAdapter(
                                this@userSignUp,
                                android.R.layout.simple_spinner_dropdown_item,
                                spinnerItems
                            )
                            schoolSpinner.adapter = myAdapter
                        }
                    }
                })
                NEISParsing.start()


            Log.d("spinner선택값",spinnerSchool)

        }

        SignUpBtn.setOnClickListener{
            spinnerSchool = schoolSpinner.getSelectedItem().toString()
            spinnerText_BirthMM = Birth_MM.getSelectedItem().toString()
            spinnerText_BirthDD = Birth_DD.getSelectedItem().toString()
            val SignUp_info = userData(
                    user?.email.toString(),
                    signupNickText.getText().toString(),
                    signupBirth_YY.getText().toString(),
                    spinnerText_BirthMM,
                    spinnerText_BirthDD,
                    spinnerSchool, //학교명
                    null,
                    user?.photoUrl.toString(),
                    null,
                    0,
                    0,
                    0
            )
            db.collection("UserInform").document(user?.email.toString()).set(SignUp_info)

            Toast.makeText(this, "회원가입이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
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