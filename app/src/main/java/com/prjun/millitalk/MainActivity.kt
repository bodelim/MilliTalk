package com.prjun.millitalk

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_user_sign_up.*


class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage) {
        @SuppressLint("LongLogTAG")//이건 뭐지?
        if(p0?.notification != null){
            sendNotification(p0.notification?.title, p0.notification!!.body!!)
        }
    }

    private fun sendNotification(title: String?, body: String){
        //어떤 모양으로 알림을 할지 설정한 다음 실제 폰 상단에 표시하도록 한다.
        //pendingIntent를 이용 알림을 클릭하면 열 앱의 액티비티를 설정해 준다.
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}

class MainActivity : AppCompatActivity() {
    /*
    companion object {
        var mactivity: Activity? = null
        }
    //정적변수
     */
    //var LA = loginActivity.lactivity as loginActivity


    // Google Login result
    private val RC_SIGN_IN = 9001

    // Google Api Client
    private var googleSigninClient: GoogleSignInClient? = null

    // Firebase Auth
    private var firebaseAuth: FirebaseAuth? = null

    val db = Firebase.firestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //다크모드 미적용

        //mactivity = this@MainActivity
        //mactivity정의(다른액티비티에서 조종하기위함)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSigninClient = GoogleSignIn.getClient(this, gso)

        firebaseAuth = FirebaseAuth.getInstance()


        val user = Firebase.auth.currentUser
        //var user = FirebaseAuth.getInstance().currentUser
        Log.d("loginCheck","photoUrl: ${user?.photoUrl}")


        user?.let {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl = user.photoUrl
            // Check if user's email is verified
            val emailVerified = user.isEmailVerified
            val uid = user.uid
        }

        var FbDoc = db.collection("UserInform").document(user?.email.toString())



        nickProfile.setText(user?.displayName+"님의 프로필")

        /*
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }
         */
        if(user == null){
            Toast.makeText(this, "구글 로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
            finish()
        }
        if(user != null){
            FbDoc.get().addOnSuccessListener { documentSnapshot ->
                val document = documentSnapshot
                if (document!!.exists()) {
                    Log.d("Firestore", "Document exists!")
                } else {
                    Log.d("Firestore", "Document does not exist!")
                    Toast.makeText(this, "밀리톡 첫 사용을 환영합니다!\n회원가입을 진행합니다.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, userSignUp::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        //https://open.neis.go.kr/hub/schoolInfo?KEY=3fba6a4954ea4992a90d541cf103f654&Type=json&pIndex=1&pSize=10


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Google 로그인 인텐트 응답
        if (requestCode === RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {

            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) {

                // 성공여부
                if (it.isSuccessful) {

                    val user = firebaseAuth?.currentUser
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()

                } else {

                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    }



