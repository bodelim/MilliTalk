package com.prjun.millitalk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_school_search.*
import kotlinx.android.synthetic.main.activity_user_sign_up.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.jsoup.Jsoup
import java.util.*


class SchoolSearch : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_search)

        var searchResult = arrayOf("학교검색후에 여기서 선택!")

        searchBar.addTextChangedListener(
                object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    private var timer: Timer = Timer()
                    private val DELAY: Long = 500 //Milliseconds
                    override fun afterTextChanged(s: Editable) {
                        timer.cancel()
                        timer = Timer()
                        timer.schedule(
                                object : TimerTask() {
                                    override fun run() {
                                        val url = "https://open.neis.go.kr/hub/schoolInfo?KEY=3fba6a4954ea4992a90d541cf103f654&Type=json&pIndex=1&pSize=5&SCHUL_NM=${searchBar.text}".trimIndent()
                                        //NEIS 학교검색 Api
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
                                                    searchResult += search_SchoolName
                                                    Log.d("spinnerArray",searchResult[i])
                                                    Log.d("rowlength",search_row.getJSONArray("row").length().toString())
                                                }
                                            }catch(e:Exception){
                                                searchResult = arrayOf("검색된 학교가 없습니다.")
                                                Log.d("parseError", e.toString())
                                                Log.d("parseErrorLine", e.printStackTrace().toString())
                                                Log.d("spinnerItems", searchResult[0])
                                            }
                                        })
                                        NEISParsing.start()

                                    }
                                },
                                DELAY
                        )
                    }
                }
        )

    }
}