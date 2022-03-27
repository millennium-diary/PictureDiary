package com.example.picturediary

import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.user_group_item.view.*
import java.lang.Float.*

// 참고자료 : https://velog.io/@trycatch98/Android-RecyclerView-Swipe-Menu
class GroupSwipeHelperCallback(var context: Context, var recyclerView: RecyclerView) : ItemTouchHelper.Callback() {
    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private var currentPosition: Int? = null
    private var previousPosition: Int? = null
    private var currentDx = 0f
    private var clamp = 0f

    // 활성화된 이동 방향을 정의하는 플래그를 반환하는 메소드
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT or RIGHT)
    }

    // 드래그된 객체를 새로운 위치로 옮길때 호출
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) { }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        currentDx = 0f
        previousPosition = viewHolder.absoluteAdapterPosition
        getDefaultUIUtil().clearView(getView(viewHolder))
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            currentPosition = viewHolder.absoluteAdapterPosition
            getDefaultUIUtil().onSelected(getView(it))
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {
            val view = getView(viewHolder)
            val isClamped = getTag(viewHolder)
            val x =  clampViewPositionHorizontal(view, dX, isClamped, isCurrentlyActive)

            currentDx = x
            getDefaultUIUtil().onDraw(
                c,
                recyclerView,
                view,
                x,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        val isClamped = getTag(viewHolder)
        // 현재 View가 고정되어 있지 않고 사용자가 -clamp 이상 swipe시 isClamped true로 변경 아닐시 false로 변경
        setTag(viewHolder, !isClamped && currentDx <= -clamp)

        if (!isClamped && currentDx <= -clamp) {
            auth = Firebase.auth
            firestore = FirebaseFirestore.getInstance()
            val groupId = viewHolder.itemView.groupId.text.toString()
            val groupName = viewHolder.itemView.groupName.text.toString()
            val leader = viewHolder.itemView.leader.text.toString()

            firestore!!.collection("groups")
                .document(groupId)
                .get()
                .addOnCompleteListener { task ->
                    val document = task.result
                    val shareWith = document["shareWith"] as ArrayList<String>?

                    if (shareWith!!.size <= 1)
                        deleteGroup(groupId, groupName)
//                    else if ()
                    else
                        exitGroup(groupId, groupName)
                }
        }

        return 2f
    }

    // groups 컬렉션에서 그룹 삭제
    private fun deleteGroup(groupId : String, groupName : String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName

        val dlg = AlertDialog.Builder(context)
        dlg.setTitle("$groupName 그룹에 남은 마지막 멤버입니다")
        dlg.setMessage("$username 님이 나가면 해당 그룹은 아예 삭제됩니다")

        // 그룹 삭제
        dlg.setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
            firestore!!.collection("groups")
                .document(groupId)
                .delete()
            deleteUserGroup(groupId, groupName)
            removePreviousClamp(recyclerView)
        })
        // 그룹 삭제 취소
        dlg.setNegativeButton("취소", DialogInterface.OnClickListener { _, _ ->
            removePreviousClamp(recyclerView)
        })

        dlg.show()
    }

    // groups 컬렉션의 shareWith에서 사용자 삭제
    private fun exitGroup(groupId : String, groupName : String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName

        val dlg = AlertDialog.Builder(context)
        dlg.setTitle("$groupName 그룹을 나가시겠습니까?")

        // 그룹 삭제
        dlg.setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
            Toast.makeText(context, "groupId $groupId", Toast.LENGTH_SHORT).show()
            firestore!!.collection("groups")
                .document(groupId)
                .update("shareWith", FieldValue.arrayRemove(username))
            deleteUserGroup(groupId, groupName)
            removePreviousClamp(recyclerView)
        })
        // 그룹 삭제 취소
        dlg.setNegativeButton("취소", DialogInterface.OnClickListener { _, _ ->
            removePreviousClamp(recyclerView)
        })
        dlg.show()
    }

    // users 컬렉션의 userGroups에서 그룹 삭제
    private fun deleteUserGroup(groupId : String, groupName : String) {
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        val username = auth?.currentUser?.displayName.toString()

        firestore!!.collection("users")
            .document(username)
            .update("userGroups", FieldValue.arrayRemove(groupId))

        Toast.makeText(context, "$groupName 그룹을 나갔습니다", Toast.LENGTH_SHORT).show()
    }

    private fun getView(viewHolder: RecyclerView.ViewHolder): View {
        return (viewHolder as GroupListAdapter.ViewHolder).itemView.swipe_view
    }

    private fun clampViewPositionHorizontal(
        view: View,
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ) : Float {
        // View의 가로 길이의 절반까지만 swipe 되도록
        val min: Float = -view.width.toFloat()/2
        // RIGHT 방향으로 swipe 막기
        val max: Float = 0f

        // View가 고정되었을 때 swipe되는 영역 제한
        val x = if (isClamped)
            if (isCurrentlyActive) dX - clamp else -clamp
        else dX

        return min(max(min, x), max)
    }

    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean) {
        viewHolder.itemView.tag = isClamped
    }

    private fun getTag(viewHolder: RecyclerView.ViewHolder) : Boolean {
        return viewHolder.itemView.tag as? Boolean ?: false
    }

    fun setClamp(clamp: Float) {
        this.clamp = clamp
    }

    // 다른 View가 swipe 되거나 터치되면 고정 해제
    fun removePreviousClamp(recyclerView: RecyclerView) {
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).animate().x(0f).setDuration(100L).start()
            setTag(viewHolder, false)
            previousPosition = null
        }
    }
}