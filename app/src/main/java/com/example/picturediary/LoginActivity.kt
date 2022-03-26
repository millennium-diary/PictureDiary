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
import com.google.android.material.snackbar.Snackbar
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
            when {
                username_edittext.text.isEmpty() -> Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                password_edittext.text.isEmpty() -> Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                else -> createAndLogin(username_edittext.text.toString(), password_edittext.text.toString())
            }
        }
    }

    // 사용자 등록 안 되어 있으면 추가, 등록되어 있으면 로그인
    private fun createAndLogin(username : String, password : String) {
        progress_bar.visibility = View.VISIBLE
        auth?.createUserWithEmailAndPassword("$username@fake.com", password)
            ?.addOnCompleteListener { task ->
                // 회원가입
                when {
                    task.isSuccessful -> {
                        addUser(username)
                        Toast.makeText(this, "회원가입을 성공적으로 했습니다", Toast.LENGTH_SHORT).show()
                        moveMainPage(auth?.currentUser)
                    }
                    // 로그인
                    task.exception?.message?.contains("already in use") == true -> signinUserID(username, password)
                    // 에러 메시지
                    else -> Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
        progress_bar.visibility = View.GONE
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
                    when {
                        error?.startsWith("The password is invalid") == true ->
                            Toast.makeText(this, "비밀번호가 틀렸거나 이미 존재하는 사용자입니다", Toast.LENGTH_SHORT).show()
                        error?.startsWith("The given password is invalid") == true ->
                            Toast.makeText(this, "비밀번호의 형식이 올바르지 않습니다", Toast.LENGTH_SHORT).show()
                        else ->
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // 사용자 추가 함수
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