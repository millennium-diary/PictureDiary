package com.example.picturediary

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        // 이메일로 로그인/회원가입
        email_login_button.setOnClickListener {
            signinAndSignup()
        }

        // 구글로 로그인
       google_sign_in_button.setOnClickListener {
            googleLogin()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestIdToken("920736295527-hc69kt36ikvjf57j10d04s8j9q8930c0.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    // 참고자료 : https://www.inflearn.com/course/%EC%9D%B8%EC%8A%A4%ED%83%80%EA%B7%B8%EB%9E%A8%EB%A7%8C%EB%93%A4%EA%B8%B0-%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C/lecture/27389?tab=community&volume=1.00&mm=null&q=101455 커뮤니티
    private fun googleLogin() {
        progress_bar.visibility = View.VISIBLE
        val signInIntent = googleSignInClient!!.signInIntent
        startForResult.launch(signInIntent)
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->

            if (result.resultCode == RESULT_OK) {
                val intent: Intent = result.data!!
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(intent)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(ContentValues.TAG, "firebaseAuthWithGoogle: " + account.id)
                    firebaseAuthWithGoogle(account)
                } catch (e: ApiException) {
                    Log.w(ContentValues.TAG, "Google Sign in failed")
                }
            }
        }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                when {
                    // 회원가입
                    task.isSuccessful -> moveMainPage(task.result?.user)
//                    // 에러 메시지
//                    task.exception?.message.isNullOrEmpty() -> Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
//                    // 로그인
//                    else -> signinEmail()
                }
            }
    }

    private fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )?.addOnCompleteListener { task ->
            when {
                // 회원가입
                task.isSuccessful -> moveMainPage(task.result?.user)
                // 에러 메시지
                task.exception?.message.isNullOrEmpty() -> Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                // 로그인
                else -> signinEmail()
            }
        }
    }

    private fun signinEmail() {
        auth?.createUserWithEmailAndPassword(
            email_edittext.text.toString(),
            password_edittext.text.toString()
        )?.addOnCompleteListener { task ->
            progress_bar.visibility = View.GONE
            // 로그인
            if (task.isSuccessful) moveMainPage(task.result?.user)
            // 에러 메시지
            else Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}