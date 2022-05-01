package com.example.picturediary

import android.content.Context
import android.widget.Toast
import com.example.picturediary.navigation.dao.DBHelper
import com.example.picturediary.navigation.model.GroupDTO
import com.example.picturediary.navigation.model.UserDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Utils {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

//    suspend fun removeUser() = withContext(Dispatchers.IO) {
//            auth = Firebase.auth
//            firestore = FirebaseFirestore.getInstance()
//
//            val user = Firebase.auth.currentUser!!
//            val uid = user.uid
//
//            // users 컬렉션에서 사용자 삭제
//            firestore!!.collection("users")
//                .document(uid)
//                .delete()
//                .addOnSuccessListener {
//                    Toast.makeText(context, "계정을 성공적으로 삭제했습니다", Toast.LENGTH_SHORT).show()
//                    PrefApplication.prefs.setString("loggedInUser", "")
//                    context.startActivity(intent)
//                    println("삭제됨")
//                }
//                .addOnFailureListener {
//                    Toast.makeText(context, "계정을 삭제하는 중 문제가 생겼습니다", Toast.LENGTH_SHORT).show()
//                }
//
//            FirebaseAuth.getInstance().signOut()
//            user.delete()
//    }

    fun createDBHelper(context: Context): DBHelper {
        val dbName = "pictureDiary.db"
        return DBHelper(context, dbName, null, 2)
    }

    // 현재 그룹에 사용자가 있는지 확인
    suspend fun userExistsInGroup(groupId: String, username: String): Boolean {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        val returnGroup = firestore!!.collection("groups")
            .document(groupId)
            .get()
            .await()
            .toObject(GroupDTO::class.java)

        val members = returnGroup!!.shareWith
        return members!!.contains(username)
    }

    suspend fun userExists(username: String): Boolean {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        val returnGroup = firestore!!.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
            .toObjects(UserDTO::class.java)

        return returnGroup.isNotEmpty()
    }

    // groups 컬렉션의 shareWith 필드에 사용자 추가
    suspend fun addToShareWith(groupId: String, username: String) {
        withContext(Dispatchers.IO) {
            firestore = FirebaseFirestore.getInstance()
            firestore!!.collection("groups")
                .document(groupId)
                .update("shareWith", FieldValue.arrayUnion(username))
                .await()
        }
    }

    // users 컬렉션의 userGroups 필드에 사용자 추가
    suspend fun addToUserGroups(groupId: String, username: String) {
        withContext(Dispatchers.IO) {
            firestore = FirebaseFirestore.getInstance()
            val userDTO = firestore!!.collection("users")
                .whereEqualTo("username", username).get()
                .await()
                .toObjects(UserDTO::class.java)[0]
            val uid = userDTO.uid.toString()

            firestore!!.collection("users")
                .document(uid)
                .update("userGroups", FieldValue.arrayUnion(groupId))
                .await()
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