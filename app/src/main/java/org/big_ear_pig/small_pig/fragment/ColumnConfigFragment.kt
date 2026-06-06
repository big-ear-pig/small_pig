package org.big_ear_pig.small_pig.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.big_ear_pig.small_pig.R
import org.big_ear_pig.small_pig.module.AppDatabase
import org.big_ear_pig.small_pig.module.table.DataType
import org.big_ear_pig.small_pig.module.table.MyColumnMeta
import kotlinx.coroutines.launch

class ColumnConfigFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var rvColumnList: RecyclerView
    private lateinit var adapter: ColumnListAdapter
    private val columnList = mutableListOf<MyColumnMeta>()
    private var tableId: Long = 0
    private var tableDisplayName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page_table_config_column, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext().applicationContext)

        tableId = arguments?.getLong(ARG_TABLE_ID) ?: 0
        tableDisplayName = arguments?.getString(ARG_TABLE_NAME) ?: "未知表"

        view.findViewById<TextView>(R.id.tvTableName).text = "表：$tableDisplayName"

        rvColumnList = view.findViewById(R.id.rvColumnList)
        rvColumnList.layoutManager = LinearLayoutManager(requireContext())
        adapter = ColumnListAdapter(columnList) { column ->
            showDeleteConfirmDialog(column)
        }
        rvColumnList.adapter = adapter

        view.findViewById<Button>(R.id.btnAddColumn).setOnClickListener {
            showAddColumnDialog()
        }

        loadColumns()
    }

    override fun onResume() {
        super.onResume()
        loadColumns()
    }

    private fun loadColumns() {
        lifecycleScope.launch {
            val columns = db.myColumnMetaDao().getColumnsByTableMetaId(tableId)
            columnList.clear()
            columnList.addAll(columns)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showAddColumnDialog() {
        val dialogView = layoutInflater.inflate(R.layout.page_table_config_column_add, null)
        val etColName = dialogView.findViewById<EditText>(R.id.etColName)
        val spDataType = dialogView.findViewById<Spinner>(R.id.spDataType)
        val etCommonWidth = dialogView.findViewById<EditText>(R.id.etCommonWidth)
        val etHeaderHeight = dialogView.findViewById<EditText>(R.id.etHeaderHeight)
        val etDataHeight = dialogView.findViewById<EditText>(R.id.etDataHeight)

        // 设置 Spinner 选项：使用枚举的显示名称
        val dataTypeDisplayNames = DataType.values().map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dataTypeDisplayNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDataType.adapter = adapter

        // 预设默认值
        spDataType.setSelection(0)  // 默认选中第一个（文本）
        etCommonWidth.setText("100")
        etHeaderHeight.setText("48")
        etDataHeight.setText("56")

        AlertDialog.Builder(requireContext())
            .setTitle("添加字段")
            .setView(dialogView)
            .setPositiveButton("确定") { _, _ ->
                val colName = etColName.text.toString().trim()
                val selectedDisplayName = spDataType.selectedItem.toString()
                val selectedDataType = DataType.values().find { it.displayName == selectedDisplayName }
                val dataTypeCode = selectedDataType?.code ?: DataType.TEXT.code
                val commonWidth = etCommonWidth.text.toString().toFloatOrNull() ?: 100f
                val headerHeight = etHeaderHeight.text.toString().toFloatOrNull() ?: 48f
                val dataHeight = etDataHeight.text.toString().toFloatOrNull() ?: 56f

                if (colName.isEmpty()) {
                    Toast.makeText(requireContext(), "字段名称不能为空", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    val existingColumns = db.myColumnMetaDao().getColumnsByTableMetaId(tableId)
                    val nextOrder = (existingColumns.maxOfOrNull { it.orderIndex } ?: 0) + 1
                    val column = MyColumnMeta(
                        tableMetaId = tableId,
                        colName = colName,
                        dataType = dataTypeCode,  // 存储枚举的 code
                        defaultValue = "",
                        orderIndex = nextOrder,
                        commonWidth = commonWidth,
                        headerHeight = headerHeight,
                        dataHeight = dataHeight
                    )
                    db.myColumnMetaDao().insert(column)
                    loadColumns()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteConfirmDialog(column: MyColumnMeta) {
        AlertDialog.Builder(requireContext())
            .setTitle("删除字段")
            .setMessage("确定要删除字段“${column.colName}”吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    db.myColumnMetaDao().delete(column)
                    loadColumns()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    inner class ColumnListAdapter(
        private val items: List<MyColumnMeta>,
        private val onLongClick: (MyColumnMeta) -> Unit
    ) : RecyclerView.Adapter<ColumnListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val column = items[position]
            // 将存储的 code 转换为显示名称（处理可能的旧数据兼容）
            val dataTypeCode = column.dataType ?: ""
            val displayDataType = DataType.fromCode(dataTypeCode)?.displayName ?: dataTypeCode.ifEmpty { "未知" }
            holder.textView.text = "${column.colName} ($displayDataType)  宽:${column.commonWidth}dp  头高:${column.headerHeight}dp  行高:${column.dataHeight}dp"
            holder.itemView.setOnLongClickListener {
                onLongClick(column)
                true
            }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(android.R.id.text1)
        }
    }

    companion object {
        private const val ARG_TABLE_ID = "table_id"
        private const val ARG_TABLE_NAME = "table_name"

        fun newInstance(tableId: Long, tableName: String): ColumnConfigFragment {
            return ColumnConfigFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_TABLE_ID, tableId)
                    putString(ARG_TABLE_NAME, tableName)
                }
            }
        }
    }
}