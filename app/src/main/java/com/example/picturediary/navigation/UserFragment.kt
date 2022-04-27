package com.example.picturediary.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.ImageDecoder.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.picturediary.LoginActivity
import com.example.picturediary.PrefApplication
import com.example.picturediary.R
import com.example.picturediary.Utils
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UserFragment: Fragment() {
    private val utils = Utils()
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var photoUrl: Uri? = null
    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && it.data != null) {
            photoUrl = it.data!!.data!!
            info_profile_pic.setImageURI(photoUrl)

            val source: Source = createSource(requireContext().contentResolver, photoUrl!!)
            val bitmap = decodeBitmap(source)
            val bitmapDrawable = BitmapDrawable(requireContext().resources, bitmap)
            info_profile_pic.background = bitmapDrawable
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        val uid = auth?.currentUser?.uid.toString()
        val username = auth?.currentUser?.displayName.toString()

        val view: View = inflater.inflate(R.layout.fragment_user, container, false)

        view.info_username_text.text = username
        firestore!!.collection("users")
            .document(uid)
            .get()
            .addOnCompleteListener { task ->
                val document = task.result
                val imageUrl = document["imageUrl"].toString()
                val message = document["message"].toString()

                // imageUrl 유무에 따른 프로필 사진 설정
                if (imageUrl.isBlank()) view.info_profile_pic.setImageResource(R.drawable.user)
                else Glide.with(requireContext()).load(Uri.parse(imageUrl)).into(view.info_profile_pic)

                // message 유무에 따른 상태 메시지 설정
                if (message != "") {
                    view.info_message_text.text = message
                    view.info_message_edit.setText(message)
                }
                else {
                    view.speech_bubble.visibility = View.GONE
                    view.info_message_edit.hint = "상태 메시지를 입력하세요"
                }
            }


        // 정보 수정 버튼
        view.info_update.setOnClickListener {
            firestore!!.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener { task ->
                    val document = task.result
                    val message = document["message"].toString()

                    if (message != "") view.info_message_edit.setText(message)
                    else view.info_message_edit.hint = "상태 메시지를 입력하세요"
                }

            // textview --> GONE, edittext --> VISIBLE
            view.info_update.visibility = View.GONE
            view.info_message_text.visibility = View.GONE
            view.info_user_out.visibility = View.GONE

            view.speech_bubble.visibility = View.VISIBLE
            view.change_pic.visibility = View.VISIBLE
            view.info_update_complete.visibility = View.VISIBLE
            view.info_message_edit.visibility = View.VISIBLE

            // 프로필 사진 눌러 사진 변경
            view.info_profile_pic.setOnClickListener {
                val choose_pic = arrayOf("기본 이미지로 변경", "앨범에서 사진 선택")
                val dlg = AlertDialog.Builder(requireContext())
                val picListener = DialogInterface.OnClickListener { _, which ->
                    if (which == 0) view.info_profile_pic.setImageResource(R.drawable.user)
                    else selectImage()
                }
                dlg.setItems(choose_pic, picListener)
                dlg.create()
                dlg.show()
            }
        }

        // 카메라 버튼 사진 눌러 사진 변경
        view.change_pic.setOnClickListener {
            val choose_pic = arrayOf("기본 이미지로 변경", "앨범에서 사진 선택")
            val dlg = AlertDialog.Builder(requireContext())
            val picListener = DialogInterface.OnClickListener { _, which ->
                if (which == 0) view.info_profile_pic.setImageResource(R.drawable.user)
                else selectImage()
            }
            dlg.setItems(choose_pic, picListener)
            dlg.create()
            dlg.show()
        }

        // 수정 완료 버튼
        view.info_update_complete.setOnClickListener {
            val newMessage = view.info_message_edit.text.toString()
            changeMessage(newMessage)
            view.info_message_text.text = newMessage

            if (newMessage.isEmpty()) view.speech_bubble.visibility = View.GONE
            if (view.info_profile_pic.drawable.constantState == resources.getDrawable(R.drawable.user).constantState) {
                firestore!!.collection("users")
                    .document(uid)
                    .update("imageUrl", "")
            }
            else if (view.info_profile_pic.drawable != null)
                uploadImage()

            // textview --> VISIBLE, edittext --> GONE
            view.info_update.visibility = View.VISIBLE
            view.info_username_text.visibility = View.VISIBLE
            view.info_message_text.visibility = View.VISIBLE
            view.info_user_out.visibility = View.VISIBLE

            view.change_pic.visibility = View.GONE
            view.info_update_complete.visibility = View.GONE
            view.info_username_edit.visibility = View.GONE
            view.info_message_edit.visibility = View.GONE
        }

        // 계정 삭제
        view.delete_account.setOnClickListener {
            // groups 컬렉션에서 사용자 삭제
            GlobalScope.launch(Dispatchers.IO) {
                val groupDTOs = firestore!!.collection("groups")
                    .whereArrayContains("shareWith", username).get()
                    .await()
                    .toObjects(GroupDTO::class.java)

                for (groupDTO in groupDTOs) {
                    val groupId = groupDTO.grpid.toString()
                    val groupName = groupDTO.grpname.toString()
                    val leader = groupDTO.leader.toString()
                    val shareWith = groupDTO.shareWith

                    when {
                        shareWith!!.size <= 1 -> utils.deleteGroup(groupId)
                        username == leader -> utils.giveLeader(groupId, groupName, username)
                        else -> utils.exitGroup(groupId)
                    }
                }

                utils.removeUser(requireContext())
                val intent = Intent(context, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                requireContext().startActivity(intent)
            }
        }

        // 비밀번호 변경
        view.change_password.setOnClickListener {
            val user = Firebase.auth.currentUser
            var password: String? = null

            // 팝업 설정
            val dlg = AlertDialog.Builder(requireActivity())
            val input = EditText(requireActivity())
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            dlg.setTitle("새로운 비밀번호를 입력하세요")
            dlg.setMessage("비밀번호는 최소 6글자로 설정해야 합니다")
            dlg.setView(input)
            dlg.setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                password = input.text.toString()
                if (password!!.isNotBlank()) {
                    user!!.updatePassword(password!!)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "비밀번호를 성공적으로 변경했습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "비밀번호를 다시 확인해 주세요", Toast.LENGTH_SHORT).show()
                        }
                }
            })
            dlg.show()
        }

       // 로그아웃
        view.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            PrefApplication.prefs.setString("loggedInUser", "")
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        return view
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        getResult.launch(intent)
    }

    private fun uploadImage() {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val uid = auth?.currentUser?.uid.toString()
        val username = auth?.currentUser?.displayName.toString()
        val filename = "profile_$username.png"
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("처리 중...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        if (photoUrl != null) {
            ref.putFile(photoUrl!!)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "처리 완료", Toast.LENGTH_SHORT).show()
                    if (progressDialog.isShowing) progressDialog.dismiss()

                    ref.downloadUrl.addOnSuccessListener {
                        firestore!!.collection("users")
                            .document(uid)
                            .update("imageUrl", it.toString())
                    }
                }
                .addOnFailureListener {
                    if (progressDialog.isShowing) progressDialog.dismiss()
                    Toast.makeText(requireContext(), "처리하는 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }            
        }
        else {
            if (progressDialog.isShowing)
                progressDialog.dismiss()
        }

    }

    private fun changeMessage(message: String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val uid = auth?.currentUser?.uid.toString()

        firestore!!.collection("users")
            .document(uid)
            .update("message", message)
    }
}