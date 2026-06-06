package org.big_ear_pig.small_pig.menu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import org.big_ear_pig.small_pig.databinding.Fragment3Binding
import org.big_ear_pig.small_pig.module.file.MyFile
import java.io.File

/**
 * 文件列表展示 Fragment
 * 从 Room 数据库读取文件信息，根据后缀显示操作按钮，并响应点击（播放视频、听歌、图片预览等）
 */
class Fragment3 : Fragment() {

    private var _binding: Fragment3Binding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = Fragment3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }



    /**
     * 处理操作按钮点击，根据文件后缀打开对应的应用
     * @param file 文件实体
     */
    private fun handleActionClick(file: MyFile) {
        val extension = file.fileExtension?.lowercase() ?: ""
        val fileObj = File(file.filePath)
        if (!fileObj.exists()) {
            Toast.makeText(requireContext(), "文件不存在: ${file.filePath}", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = getFileUri(fileObj)
        when (extension) {
            "mp4" -> playVideo(uri)
            "mp3" -> playAudio(uri)
            "jpg", "jpeg" -> previewImage(uri)
            else -> openUnknownFile(uri, file.mimeType)
        }
    }

    /**
     * 将 File 对象转换为 Uri，适配 Android 7.0+ 的 FileProvider
     * @param file 文件对象
     * @return 可分享的 Uri
     */
    private fun getFileUri(file: File): Uri {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    /**
     * 播放视频（调用系统视频播放器）
     */
    private fun playVideo(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "video/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivitySafely(intent)
    }

    /**
     * 播放音频（调用系统音乐播放器）
     */
    private fun playAudio(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "audio/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivitySafely(intent)
    }

    /**
     * 预览图片（调用系统图片查看器）
     */
    private fun previewImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivitySafely(intent)
    }

    /**
     * 打开未知类型文件，使用 MIME 类型或通配符
     */
    private fun openUnknownFile(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val type = mimeType ?: "*/*"
            setDataAndType(uri, type)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivitySafely(intent)
    }

    /**
     * 安全启动 Intent，捕获 ActivityNotFoundException
     */
    private fun startActivitySafely(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "没有找到可用的应用打开该文件", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}