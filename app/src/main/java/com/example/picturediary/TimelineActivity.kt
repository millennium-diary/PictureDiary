package com.example.picturediary

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.picturediary.databinding.ActivityMainBinding
import com.example.picturediary.navigation.*
import com.example.picturediary.navigation.model.GroupDTO
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import kotlinx.android.synthetic.main.community.*


class TimelineActivity : AppCompatActivity(), OnItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.community)

        bottom_navigation.setOnItemSelectedListener(this)

        if (savedInstanceState == null) {
            val detailViewFragment = DetailViewFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content, detailViewFragment)
                .commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // 그룹 목록
            R.id.action_home -> {
                val detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, detailViewFragment)
                    .commit()
                return true
            }

            // 사용자 계정
            R.id.action_account -> {
                val userFragment = UserFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, userFragment)
                    .commit()
                return true
            }
        }
        return false
    }
}