package com.example.picturediary

import android.app.ProgressDialog
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.picturediary.navigation.dao.DBHelper
import com.example.picturediary.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 데이터베이스 생성
        val dbName = "pictureDiary.db"
        val dbHelper = DBHelper(this, dbName, null, 1)
        dbHelper.readableDatabase

        // Firebase 로그인 통합 관리하는 객체
        auth = FirebaseAuth.getInstance()
//        PrefApplication.prefs.setString("loggedInUser", "")
        val loggedInUser = PrefApplication.prefs.getString("loggedInUser", "")

        if (loggedInUser.isBlank()) {
            setContentView(R.layout.activity_login)

            signup_button.setOnClickListener { signUpButton() }
            login_button.setOnClickListener { loginButton() }
        }
        else {
            val userInfo = loggedInUser.split("★")
            val username = userInfo[0]
            val password = userInfo[1]
            println("사용자 $username $password")

            auth?.signInWithEmailAndPassword(username, password)
                ?.addOnCompleteListener { moveMainPage(auth?.currentUser) }
        }
    }

    // 이메일로 회원가입
    private fun signUpButton() {
        val wifi = getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifi.isWifiEnabled) {
            Toast.makeText(this, "와이파이 연결을 확인해 주세요", Toast.LENGTH_SHORT).show()
        }
        else {
            when {
                username_edittext.text.isEmpty() -> Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                password_edittext.text.isEmpty() -> Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                else -> createAndLogin(username_edittext.text.toString(), password_edittext.text.toString())
            }
        }
    }

    // 이메일로 로그인
    private fun loginButton() {
        val wifi = getSystemService(WIFI_SERVICE) as WifiManager
        if (!wifi.isWifiEnabled) {
            Toast.makeText(this, "와이파이 연결을 확인해 주세요", Toast.LENGTH_SHORT).show()
        }
        else {
            when {
                username_edittext.text.isEmpty() -> Toast.makeText(this, "아이디를 입력해주세요", Toast.LENGTH_SHORT).show()
                password_edittext.text.isEmpty() -> Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                else -> signinUserID(username_edittext.text.toString(), password_edittext.text.toString())
            }
        }
    }

    // 사용자 등록 & 로그인
    private fun createAndLogin(username : String, password : String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("처리 중...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        auth?.createUserWithEmailAndPassword("$username@fake.com", password)
            ?.addOnCompleteListener { task ->
                // 회원가입
                when {
                    task.isSuccessful -> {
                        addUser(username)
                        setNewUsername(username)

                        Toast.makeText(this, "회원가입을 성공적으로 했습니다", Toast.LENGTH_SHORT).show()
                        PrefApplication.prefs.setString("loggedInUser", "$username★$password")
                        if (progressDialog.isShowing) progressDialog.dismiss()
                        moveMainPage(auth?.currentUser)
                    }
                    // 이미 존재하는 아이디
                    task.exception?.message?.contains("already in use") == true -> {
                        Toast.makeText(this, "이미 존재하는 아이디입니다", Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }
                    // 이미 존재하는 아이디
                    task.exception?.message?.contains("badly formatted") == true -> {
                        Toast.makeText(this, "사용자 아이디에는 문자, 숫자, 밑줄 및 마침표만 사용할 수 있습니다", Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }
                    // 비밀번호 형식 에러
                    task.exception?.message?.startsWith("The given password is invalid") == true -> {
                        Toast.makeText(this, "비밀번호의 형식이 올바르지 않습니다\n(최소 6글자로 설정해야 합니다)", Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }
                    // 에러 메시지
                    else -> {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        if (progressDialog.isShowing) progressDialog.dismiss()
                    }
                }
            }
    }

    // 로그인 함수
    private fun signinUserID(username : String, password: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("처리 중...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        auth?.signInWithEmailAndPassword("$username@fake.com", password)
            ?.addOnCompleteListener { task ->
                // 로그인
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인을 성공적으로 했습니다", Toast.LENGTH_SHORT).show()
                    PrefApplication.prefs.setString("loggedInUser", "$username★$password")
                    if (progressDialog.isShowing) progressDialog.dismiss()
                    moveMainPage(auth?.currentUser)
                }
                // 에러 메시지
                else {
                    val error = task.exception?.message
                    when {
                        error?.contains("no user record") == true -> {
                            Toast.makeText(this, "존재하는 사용자가 없습니다", Toast.LENGTH_SHORT).show()
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        }
                        error?.startsWith("The password is invalid") == true -> {
                            Toast.makeText(this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show()
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        }
                        error?.startsWith("The given password is invalid") == true -> {
                            Toast.makeText(this, "비밀번호의 형식이 올바르지 않습니다\n(최소 6글자로 설정해야 합니다)", Toast.LENGTH_SHORT).show()
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        }
                        else -> {
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                            if (progressDialog.isShowing) progressDialog.dismiss()
                        }
                    }
                }
        }
    }

    private fun setNewUsername(username: String) {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = username
        }

        user!!.updateProfile(profileUpdates)
    }

    // 사용자 추가 함수
    private fun addUser(username: String) {
        val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()

        val userInfo = UserDTO()
        userInfo.uid = auth?.uid.toString()
        userInfo.username = username
        userInfo.imageUrl = ""
        userInfo.message = ""

        val collection = firestore.collection("users").document(userInfo.uid!!)
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
}