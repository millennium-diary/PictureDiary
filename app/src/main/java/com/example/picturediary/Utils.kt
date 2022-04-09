package com.example.picturediary

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.picturediary.navigation.model.GroupDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Utils {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    suspend fun removeUser(context: Context) = withContext(Dispatchers.IO) {
        val user = Firebase.auth.currentUser!!
        val uid = user.uid

        // users 컬렉션에서 사용자 삭제
        firestore!!.collection("users")
            .document(uid)
            .delete()
            .await()

        FirebaseAuth.getInstance().signOut()
        user.delete()
            .addOnSuccessListener {
                Toast.makeText(context, "계정을 성공적으로 삭제했습니다", Toast.LENGTH_SHORT).show()
                PrefApplication.prefs.setString("loggedInUser", "")
            }
            .addOnFailureListener {
                Toast.makeText(context, "계정을 삭제하는 중 문제가 생겼습니다", Toast.LENGTH_SHORT).show()
            }
    }

    // groups 컬렉션에 그룹 추가
    suspend fun addToGroup(grpname: String, activity: Context) {
        withContext(Dispatchers.IO) {
            auth = Firebase.auth
            firestore = FirebaseFirestore.getInstance()
            val uid = auth?.currentUser?.uid.toString()
            val username = auth?.currentUser?.displayName.toString()

            // 그룹 형식에 정보 입력
            val groupDTO = GroupDTO()
            groupDTO.grpid = "$grpname@$username"
            groupDTO.grpname = grpname
            groupDTO.leader = username
            groupDTO.timestamp = System.currentTimeMillis()
            groupDTO.shareWith = arrayListOf(groupDTO.leader.toString())

            firestore!!.collection("groups")
                .document("$grpname@$username")
                .set(groupDTO)
                .await()

            // 사용자 데이터베이스에 추가
            firestore!!.collection("users")
                .document(uid)
                .update("userGroups", FieldValue.arrayUnion("$grpname@$username"))
                .await()
        }
        withContext(Dispatchers.Main) {
            Toast.makeText(activity, "$grpname 그룹을 생성했습니다", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun groupExists(grpname: String): Boolean {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()

        val returnGroup = firestore!!.collection("groups")
            .document("$grpname@$username").get()
            .await()
            .toObject(GroupDTO::class.java)

        return returnGroup != null
    }

    // groups 컬렉션에서 그룹 삭제
    suspend fun deleteGroup(groupId: String) = withContext(Dispatchers.IO) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        firestore!!.collection("groups")
            .document(groupId)
            .delete()
            .await()
        println("test: deleteGroup $groupId ${Thread.currentThread().name}")
    }

    // users 컬렉션의 userGroups에서 그룹 삭제
    suspend fun deleteUserGroup(groupId : String) = withContext(Dispatchers.IO) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val uid = auth?.currentUser?.uid.toString()

        firestore!!.collection("users")
            .document(uid)
            .update("userGroups", FieldValue.arrayRemove(groupId))
            .await()
        println("test: deleteUserGroup $groupId ${Thread.currentThread().name}")
    }

    // groups 컬렉션의 shareWith에서 사용자 삭제
    suspend fun exitGroup(groupId: String) = withContext(Dispatchers.IO) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName

        firestore!!.collection("groups")
            .document(groupId)
            .update("shareWith", FieldValue.arrayRemove(username))
            .await()
        println("test: exitGroup $groupId ${Thread.currentThread().name}")
    }

    // groups 컬렉션에서 leader 새로 임명
    suspend fun giveLeader(groupId: String, groupName: String, username:String) = withContext(Dispatchers.IO) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // 그룹 멤버에서 나가기 (exitGroup)
        firestore!!.collection("groups")
            .document(groupId)
            .update("shareWith", FieldValue.arrayRemove(username))
            .await()

        // 리더 위임하기
        val groupDTO = firestore!!.collection("groups")
            .document(groupId).get()
            .await()
            .toObject(GroupDTO::class.java)

        val shareWith = groupDTO?.shareWith
        val timestamp = groupDTO?.timestamp
        val newLeader = shareWith!![0]
        val newGroupId = "$groupName@$newLeader"
        val newShareWith = arrayListOf<String>()
        for (index in 0 until shareWith.size) {
            newShareWith.add(shareWith[index])
        }

        val newGroupDTO = GroupDTO()
        newGroupDTO.grpid = newGroupId
        newGroupDTO.grpname = groupName
        newGroupDTO.leader = newLeader
        newGroupDTO.timestamp = timestamp
        newGroupDTO.shareWith = newShareWith

        firestore!!.collection("groups")
            .document(newGroupDTO.grpid.toString())
            .set(newGroupDTO)
            .await()

        // 본인이 리더였던 그룹 삭제 (deleteGroup)
        firestore!!.collection("groups")
            .document(groupId)
            .delete()
            .await()

        println("test: giveLeader $groupId ${Thread.currentThread().name}")
    }
}