package org.big_ear_pig.small_pig

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.big_ear_pig.small_pig.menu.Fragment1
import org.big_ear_pig.small_pig.menu.Fragment2
import org.big_ear_pig.small_pig.menu.Fragment3
import org.big_ear_pig.small_pig.menu.Fragment4
import org.big_ear_pig.small_pig.menu.Fragment5

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 启用边缘到边缘布局（基础）
        enableEdgeToEdge()

        // 2. 设置全屏标志：隐藏状态栏、导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController = window.insetsController
            windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            windowInsetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        WindowCompat.getInsetsController(window, window.decorView).hide(WindowInsetsCompat.Type.statusBars())

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item1 -> {
                    loadFragment(Fragment1())
                    true
                }
                R.id.item2 -> {
                    loadFragment(Fragment2())
                    true
                }
                R.id.item3 -> {
                    loadFragment(Fragment3())
                    true
                }
                R.id.item4 -> {
                    loadFragment(Fragment4())
                    true
                }
                R.id.item5 -> {
                    loadFragment(Fragment5())
                    true
                }
                else -> false
            }
        }

        // 默认加载第一个页面
        loadFragment(Fragment1())

        // 3. 移除系统栏内边距（因为要全屏）
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            // 全屏模式下不需要设置内边距，直接返回insets
            Log.d("MainActivity",""+v.id)
            insets
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commitNow()
    }
}