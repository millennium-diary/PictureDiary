package com.example.picturediary

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.picturediary.navigation.*
import com.google.android.material.navigation.NavigationBarView.*
import kotlinx.android.synthetic.main.community.*

class ShareActivity : AppCompatActivity(), OnItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_home -> {
                val detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, detailViewFragment)
                    .commit()
                return true
            }
            R.id.action_search -> {
                val gridFragment = GridFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, gridFragment)
                    .commit()
                return true
            }
            R.id.action_add_video -> {
                return true
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.community)
        bottom_navigation.setOnItemSelectedListener(this)
    }
}