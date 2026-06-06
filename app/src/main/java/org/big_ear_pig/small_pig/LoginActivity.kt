package org.big_ear_pig.small_pig

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.big_ear_pig.small_pig.module.AppDatabase
import org.big_ear_pig.small_pig.module.log.MyLog
import org.big_ear_pig.small_pig.module.user.MyUser

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var pbLoading: ProgressBar
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
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

        // 初始化 Room 数据库
        db = AppDatabase.getInstance(applicationContext)

        // 绑定控件
        etUsername = findViewById(R.id.username)
        etPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.login)
        pbLoading = findViewById(R.id.loading)

        // 输入监听：用户名/密码不为空时，启用登录按钮
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                btnLogin.isEnabled = etUsername.text.isNotEmpty() && etPassword.text.isNotEmpty()
            }
        }
        etUsername.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)

        // 登录按钮点击事件
        btnLogin.setOnClickListener {
            btnLogin.visibility = View.GONE
            pbLoading.visibility = View.VISIBLE

            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            login(username, password)
        }
    }

    private fun login(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    val userList = db.myUserDao().getAllUsers()
                    if(userList.isEmpty()){
                        // 用户不存在：先创建新用户（IO线程）
                        launch(Dispatchers.IO) {
                            val newUser = MyUser().apply {
                                this.username = username
                                this.password = password
                            }
                            db.myUserDao().insertUser(newUser)
                            // 新用户创建成功后记录日志
                            insertLoginLog(username)
                        }
                        // UI 反馈和跳转
                        Toast.makeText(this@LoginActivity, "新用户已创建并登录", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }else{
                        val existingUser = userList.find { it.username == username }
                        if (existingUser != null) {
                            if (existingUser.password == password) {
                                // 记录登录日志
                                insertLoginLog(username)
                                Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                                navigateToMainActivity()
                            } else {
                                // 密码错误，不记录日志
                                Toast.makeText(this@LoginActivity, "密码错误", Toast.LENGTH_SHORT).show()
                                pbLoading.visibility = View.GONE
                                btnLogin.visibility = View.VISIBLE
                            }
                        }else{
                            // 密码错误，不记录日志
                            Toast.makeText(this@LoginActivity, "用户不存在", Toast.LENGTH_SHORT).show()
                            pbLoading.visibility = View.GONE
                            btnLogin.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "登录失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    pbLoading.visibility = View.GONE
                    btnLogin.visibility = View.VISIBLE
                }
            }
        }
    }

    // 辅助方法：在 IO 线程插入日志
    private suspend fun insertLoginLog(username: String) {
        withContext(Dispatchers.IO) {
            val log = MyLog().apply {
                message = "用户 $username 登录成功"
                timestamp = System.currentTimeMillis()
            }
            db.myLogDao().insertLog(log)
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}