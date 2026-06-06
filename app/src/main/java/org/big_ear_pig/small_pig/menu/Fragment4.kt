package org.big_ear_pig.small_pig.menu

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.big_ear_pig.small_pig.R

class Fragment4 : Fragment() {

    private lateinit var btnEnableWifi: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_4, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnEnableWifi = view.findViewById(R.id.btn_enable_wifi)
        btnEnableWifi.setOnClickListener {
            // 跳转到系统WiFi设置页面
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        // 页面加载完成后检查WiFi连接状态
        checkWifiConnection()
    }

    /**
     * 检查WiFi连接状态，并控制按钮显示
     */
    private fun checkWifiConnection() {
        val isWifiConnected = isWifiAvailable()

        if (!isWifiConnected) {
            Toast.makeText(requireContext(), "未连接到WiFi，请检查网络设置", Toast.LENGTH_LONG).show()
            // 未连接WiFi时显示按钮
            btnEnableWifi.visibility = View.VISIBLE
        } else {
            Toast.makeText(requireContext(), "已连接到WiFi", Toast.LENGTH_SHORT).show()
            // 已连接WiFi时隐藏按钮
            btnEnableWifi.visibility = View.GONE
        }
    }

    /**
     * 判断WiFi是否可用
     */
    private fun isWifiAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Android 6.0 (API 23) 及以上版本
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
        // 低版本兼容
        else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }
}