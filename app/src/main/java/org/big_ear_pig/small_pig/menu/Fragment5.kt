package org.big_ear_pig.small_pig.menu

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import org.big_ear_pig.small_pig.R
import org.big_ear_pig.small_pig.fragment.TableConfigFragment
import org.big_ear_pig.small_pig.module.AppDatabase
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Fragment5 - 用户中心
 * 功能：显示用户信息，头像拍照/相册，导出数据库为JSON（自动保存到Download/FreeData/），导入JSON（使用GET_CONTENT兼容所有文件管理器）
 */
class Fragment5 : Fragment() {

    companion object {
        private const val ARG_USERNAME = "username"
        private const val ARG_PASSWORD = "password"
        private const val TAG = "Fragment5"
        private const val EXPORT_FILE_NAME = "free_data"

        fun newInstance(username: String, password: String): Fragment5 {
            return Fragment5().apply {
                arguments = Bundle().apply {
                    putString(ARG_USERNAME, username)
                    putString(ARG_PASSWORD, password)
                }
            }
        }
    }

    // UI组件
    private lateinit var ivAvatar: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvPassword: TextView
    private lateinit var btnConfigTable: Button
    private lateinit var cameraContainer: View
    private lateinit var viewFinder: PreviewView
    private lateinit var btnTakePhoto: Button
    private lateinit var btnCloseCamera: Button
    private lateinit var btnExport: Button
    private lateinit var btnImport: Button

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    // Launcher
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    // 导出使用 ACTION_CREATE_DOCUMENT (可设置默认文件名)
    private lateinit var exportFileLauncher: ActivityResultLauncher<Intent>
    // 导入使用 ACTION_GET_CONTENT (兼容旧设备，显示所有文件)
    private lateinit var importFileLauncher: ActivityResultLauncher<String>

    private var progressDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupLaunchers()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_5, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivAvatar = view.findViewById(R.id.ivAvatar)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvPassword = view.findViewById(R.id.tvPassword)
        btnConfigTable = view.findViewById(R.id.btnConfigTable)
        cameraContainer = view.findViewById(R.id.cameraContainer)
        viewFinder = view.findViewById(R.id.viewFinder)
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto)
        btnCloseCamera = view.findViewById(R.id.btnCloseCamera)
        btnExport = view.findViewById(R.id.btnExport)
        btnImport = view.findViewById(R.id.btnImport)

        cameraContainer.visibility = View.GONE

        val username = arguments?.getString(ARG_USERNAME) ?: "未登录"
        val password = arguments?.getString(ARG_PASSWORD) ?: ""
        tvUsername.text = "用户名：$username"
        val maskedPassword = if (password.isNotEmpty()) "*".repeat(password.length) else "未设置"
        tvPassword.text = "密码：$maskedPassword"

        btnConfigTable.setOnClickListener {
            val tableConfigFragment = TableConfigFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, tableConfigFragment)
                .commit()
        }

        ivAvatar.setOnClickListener { showImagePickDialog() }
        btnTakePhoto.setOnClickListener { capturePhoto() }
        btnCloseCamera.setOnClickListener { closeCamera() }

        // 导出：使用 ACTION_CREATE_DOCUMENT，默认文件名 free_data.json
        btnExport.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, EXPORT_FILE_NAME)
            }
            exportFileLauncher.launch(intent)
        }

        // 导入：使用 ACTION_GET_CONTENT，MIME 类型设为 "*/*" 显示所有文件，兼容红米8等旧设备
        btnImport.setOnClickListener {
            importFileLauncher.launch("*/*")
        }
    }

    private fun setupLaunchers() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { displayImage(it) } ?: Toast.makeText(requireContext(), "未选择图片", Toast.LENGTH_SHORT).show()
        }

        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCameraX() else Toast.makeText(requireContext(), "相机权限被拒绝", Toast.LENGTH_SHORT).show()
        }

        // 导出：CreateDocument
        exportFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    performExportToUri(uri)
                } ?: Toast.makeText(requireContext(), "未选择保存位置，导出已取消", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "导出已取消", Toast.LENGTH_SHORT).show()
            }
        }

        // 导入：使用 GetContent，兼容性更好
        importFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { performImportFromUri(it) }
                ?: Toast.makeText(requireContext(), "未选择文件，导入已取消", Toast.LENGTH_SHORT).show()
        }
    }

    // ======================= 导出数据库 =======================

    private fun performExportToUri(uri: Uri) {
        showProgressDialog("正在导出数据...")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getInstance(requireContext()).openHelper.writableDatabase
                val jsonString = exportAllTables(db)

                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(StandardCharsets.UTF_8))
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导出成功！文件已保存至：${uri.lastPathSegment}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导出失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) { dismissProgressDialog() }
            }
        }
    }

    // ======================= 导入数据库 =======================

    private fun performImportFromUri(uri: Uri) {
        showProgressDialog("正在导入数据...")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val jsonString = requireContext().contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    ?: throw IOException("无法读取文件内容")

                val root = JSONObject(jsonString)
                val tablesArray = root.getJSONArray("tables")
                val db = AppDatabase.getInstance(requireContext()).openHelper.writableDatabase

                db.beginTransaction()
                try {
                    for (i in 0 until tablesArray.length()) {
                        importTable(db, tablesArray.getJSONObject(i))
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导入完成", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "导入失败：${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) { dismissProgressDialog() }
            }
        }
    }

    // ======================= 数据库 JSON 序列化 / 反序列化 =======================

    private fun exportAllTables(db: SupportSQLiteDatabase): String {
        val root = JSONObject()
        val tables = JSONArray()
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name != 'my_user'",
            emptyArray()
        )
        try {
            while (cursor.moveToNext()) {
                val tableName = cursor.getString(0)
                tables.put(exportTable(db, tableName))
            }
        } finally {
            cursor.close()
        }
        root.put("tables", tables)
        return root.toString()
    }

    private fun exportTable(db: SupportSQLiteDatabase, tableName: String): JSONObject {
        val tableObj = JSONObject()
        tableObj.put("name", tableName)

        val columns = JSONArray()
        val colCursor = db.query("PRAGMA table_info($tableName)", emptyArray())
        try {
            while (colCursor.moveToNext()) {
                val col = JSONObject()
                col.put("name", colCursor.getString(1))
                col.put("type", colCursor.getString(2))
                col.put("notnull", colCursor.getInt(3))
                col.put("pk", colCursor.getInt(5))
                columns.put(col)
            }
        } finally {
            colCursor.close()
        }
        tableObj.put("columns", columns)

        val rows = JSONArray()
        val dataCursor = db.query("SELECT * FROM $tableName", emptyArray())
        try {
            while (dataCursor.moveToNext()) {
                val row = JSONObject()
                for (i in 0 until dataCursor.columnCount) {
                    val colName = dataCursor.getColumnName(i)
                    val value = when (dataCursor.getType(i)) {
                        Cursor.FIELD_TYPE_INTEGER -> dataCursor.getLong(i)
                        Cursor.FIELD_TYPE_FLOAT -> dataCursor.getDouble(i)
                        Cursor.FIELD_TYPE_STRING -> dataCursor.getString(i)
                        Cursor.FIELD_TYPE_BLOB -> android.util.Base64.encodeToString(dataCursor.getBlob(i), android.util.Base64.NO_WRAP)
                        else -> null
                    }
                    row.put(colName, value)
                }
                rows.put(row)
            }
        } finally {
            dataCursor.close()
        }
        tableObj.put("rows", rows)
        return tableObj
    }

    private fun importTable(db: SupportSQLiteDatabase, tableObj: JSONObject) {
        val tableName = tableObj.getString("name")
        val columnsArray = tableObj.getJSONArray("columns")
        val rowsArray = tableObj.getJSONArray("rows")

        if (!checkTableExists(db, tableName)) {
            createTable(db, tableName, columnsArray)
        } else {
            alterTableAddMissingColumns(db, tableName, columnsArray)
        }

        val columnNames = (0 until columnsArray.length()).map { columnsArray.getJSONObject(it).getString("name") }

        for (i in 0 until rowsArray.length()) {
            val row = rowsArray.getJSONObject(i)
            val cv = ContentValues()
            for (col in columnNames) {
                if (!row.has(col)) continue
                val value = row.get(col)
                when (value) {
                    null -> cv.putNull(col)
                    is Int -> cv.put(col, value)
                    is Long -> cv.put(col, value)
                    is Double -> cv.put(col, value)
                    is Boolean -> cv.put(col, value)
                    is String -> cv.put(col, value)
                    else -> cv.put(col, value.toString())
                }
            }
            db.insert(tableName, SQLiteDatabase.CONFLICT_REPLACE, cv)
        }
    }

    private fun checkTableExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
        val cursor = db.query("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?", arrayOf(tableName))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    private fun createTable(db: SupportSQLiteDatabase, tableName: String, columnsArray: JSONArray) {
        val sb = StringBuilder("CREATE TABLE $tableName (")
        for (i in 0 until columnsArray.length()) {
            val col = columnsArray.getJSONObject(i)
            val colName = col.getString("name")
            val colType = col.getString("type")
            val notNull = col.getBoolean("notnull")
            val pk = col.getBoolean("pk")
            sb.append("$colName $colType")
            if (notNull) sb.append(" NOT NULL")
            if (pk) sb.append(" PRIMARY KEY")
            if (i < columnsArray.length() - 1) sb.append(", ")
        }
        sb.append(")")
        db.execSQL(sb.toString())
    }

    private fun alterTableAddMissingColumns(db: SupportSQLiteDatabase, tableName: String, columnsArray: JSONArray) {
        val existing = getExistingColumnNames(db, tableName)
        for (i in 0 until columnsArray.length()) {
            val col = columnsArray.getJSONObject(i)
            val colName = col.getString("name")
            if (!existing.contains(colName)) {
                val colType = col.getString("type")
                db.execSQL("ALTER TABLE $tableName ADD COLUMN $colName $colType")
            }
        }
    }

    private fun getExistingColumnNames(db: SupportSQLiteDatabase, tableName: String): Set<String> {
        val set = mutableSetOf<String>()
        val cursor = db.query("PRAGMA table_info($tableName)", emptyArray())
        try {
            while (cursor.moveToNext()) {
                set.add(cursor.getString(1))
            }
        } finally {
            cursor.close()
        }
        return set
    }

    // ======================= 自定义进度对话框 =======================

    private fun showProgressDialog(message: String) {
        dismissProgressDialog()
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_progress, null)
        val tvMessage = view.findViewById<TextView>(R.id.progress_message)
        tvMessage.text = message

        progressDialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()
        progressDialog?.show()
    }

    private fun dismissProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    // ======================= 头像拍照 / 相册功能 =======================

    private fun showImagePickDialog() {
        val options = arrayOf("拍照", "从相册选择")
        AlertDialog.Builder(requireContext())
            .setTitle("选择头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndStart()
                    1 -> pickImageLauncher.launch("image/*")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun checkCameraPermissionAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCameraX()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            startCameraX()
        }
    }

    private fun startCameraX() {
        cameraContainer.visibility = View.VISIBLE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "相机绑定失败", e)
                Toast.makeText(requireContext(), "启动相机失败", Toast.LENGTH_SHORT).show()
                closeCamera()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createTempImageFile() ?: run {
            Toast.makeText(requireContext(), "创建临时文件失败", Toast.LENGTH_SHORT).show()
            return
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    displayImage(Uri.fromFile(photoFile))
                    closeCamera()
                }
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "拍照失败: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "拍照失败: ${exception.message}", Toast.LENGTH_SHORT).show()
                    closeCamera()
                }
            })
    }

    private fun createTempImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", requireContext().cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun closeCamera() {
        cameraContainer.visibility = View.GONE
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProviderFuture.get().unbindAll()
        }, ContextCompat.getMainExecutor(requireContext()))
        imageCapture = null
    }

    private fun displayImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .placeholder(R.drawable.ic_default_avatar)
            .error(R.drawable.ic_default_avatar)
            .into(ivAvatar)
    }

    override fun onStop() {
        super.onStop()
        if (cameraContainer.visibility == View.VISIBLE) closeCamera()
        dismissProgressDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        dismissProgressDialog()
    }
}