package com.example.picturediary

import android.content.*
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.picturediary.navigation.model.UserDTO
import com.google.firebase.auth.*
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase 로그인 통합 관리하는 객체
        auth = FirebaseAuth.getInstance()

        // 이메일로 로그인/회원가입
        signup_login_button.setOnClickListener {
            // 아이디가 모두 알파벳으로 구성되었는지 확인
//            if (!checkUsername(username_edittext.text.toString()))
//                Toast.makeText(this, "아이디는 ", Toast.LENGTH_SHORT).show()
            when {
                username_edittext.text.isEmpty() -> Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                password_edittext.text.isEmpty() -> Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                else -> createAndLogin(username_edittext.text.toString(), password_edittext.text.toString())
            }
        }
    }

//    private fun checkUsername(string: String): Boolean {
//        var cleanName = false
//
//        if (string.all { it.isLetter() }) cleanName = true
//        if (string.contains())
//    }

    // 사용자 등록 안 되어 있으면 추가, 등록되어 있으면 로그인
    private fun createAndLogin(username : String, password : String) {
        auth?.createUserWithEmailAndPassword("$username@fake.com", password)
            ?.addOnCompleteListener { task ->
                // 회원가입
                if (task.isSuccessful) {
                    addUser(username)
                    Toast.makeText(this, "회원가입을 성공적으로 했습니다", Toast.LENGTH_SHORT).show()
                    moveMainPage(auth?.currentUser)
                }
                // 로그인
                else if (task.exception?.message?.contains("already in use") == true)
                    signinUserID(username, password)
                // 에러 메시지
                else
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
            }
    }


    // 로그인 함수
    private fun signinUserID(username : String, password: String) {
        auth?.signInWithEmailAndPassword("$username@fake.com", password)
            ?.addOnCompleteListener { task ->
                progress_bar.visibility = View.GONE
                // 로그인
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인을 성공적으로 했습니다", Toast.LENGTH_SHORT).show()
                    moveMainPage(auth?.currentUser)
                }
                // 에러 메시지
                else {
                    val error = task.exception?.message
                    if (error?.startsWith("The password is invalid") == true)
                        Toast.makeText(this, "비밀번호가 틀렸거나 이미 존재하는 사용자입니다", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addUser(username: String) {
        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()

        val userInfo = UserDTO()
        userInfo.uid = auth?.uid
        userInfo.username = username

        val collection = firestore.collection("users").document(username)
        collection.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    if (!document.exists()) collection.set(userInfo)
                }
            }
        }
    }

    // 메인 페이지로 이동
    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}