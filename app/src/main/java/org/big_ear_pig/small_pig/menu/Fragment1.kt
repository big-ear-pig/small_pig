package org.big_ear_pig.small_pig.menu

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.big_ear_pig.small_pig.R
import org.big_ear_pig.small_pig.module.AppDatabase
import org.big_ear_pig.small_pig.module.table.MyCellValue
import org.big_ear_pig.small_pig.module.table.MyColumnMeta
import org.big_ear_pig.small_pig.module.table.MyTableMeta
import org.big_ear_pig.small_pig.module.table.MyTableRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Fragment1 : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var tableNameText: TextView
    private lateinit var switchTableButton: Button
    private lateinit var addButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var headerScroll: HorizontalScrollView
    private lateinit var dataScroll: HorizontalScrollView
    private lateinit var headerLayout: LinearLayout

    private var currentTableMeta: MyTableMeta? = null
    private var currentColumns: List<MyColumnMeta> = emptyList()
    private var fullDataList: List<Map<Long, String>> = emptyList()
    private var currentDataList: List<Map<Long, String>> = emptyList()
    private var tableDataAdapter: TableDataAdapter? = null

    companion object {
        private const val TAG = "Fragment1"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_1, container, false)
        tableNameText = view.findViewById(R.id.table_name_text)
        switchTableButton = view.findViewById(R.id.switch_table_button)
        addButton = view.findViewById(R.id.addButton)
        searchEditText = view.findViewById(R.id.search_edit_text)
        searchButton = view.findViewById(R.id.search_button)
        recyclerView = view.findViewById(R.id.table_data_list)
        headerScroll = view.findViewById(R.id.header_scroll)
        dataScroll = view.findViewById(R.id.data_scroll)
        headerLayout = view.findViewById(R.id.header_layout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.isNestedScrollingEnabled = false

        headerScroll.setOnScrollChangeListener { view, scrollX, scrollY, oldX, oldY ->
            if(scrollX == oldX){

            }else{
                dataScroll.scrollTo(scrollX, 0)
            }

        }
        dataScroll.setOnScrollChangeListener { view, scrollX, scrollY, oldX, oldY->
            if(scrollX == oldX){

            }else{
                headerScroll.scrollTo(scrollX, 0)
            }

        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext().applicationContext)
        loadTableData()

        switchTableButton.setOnClickListener {
            showSwitchTableDialog()
        }

        addButton.setOnClickListener {
            if (currentTableMeta == null) {
                Toast.makeText(requireContext(), "请先选择一个表", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showAddRowDialog()
        }

        searchButton.setOnClickListener {
            performSearch()
        }
    }

    private fun performSearch() {
        val keyword = searchEditText.text.toString().trim()
        if (keyword.isEmpty()) {
            // 清空搜索，显示全部数据
            if (fullDataList != currentDataList) {
                currentDataList = fullDataList.toList()
                tableDataAdapter?.updateData(currentDataList)
            }
            return
        }

        // 在所有行、所有列的值中进行匹配（不区分大小写）
        val filtered = fullDataList.filter { rowData ->
            rowData.values.any { cellValue ->
                cellValue.contains(keyword, ignoreCase = true)
            }
        }
        currentDataList = filtered
        tableDataAdapter?.updateData(currentDataList)

        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "未找到包含“$keyword”的内容", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadTableData(tableMeta: MyTableMeta? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val targetTable = tableMeta ?: db.myTableMetaDao().getFirstTableMeta()
                if (targetTable == null) {
                    withContext(Dispatchers.Main) {
                        tableNameText.text = "无表数据"
                        Toast.makeText(requireContext(), "没有找到任何表", Toast.LENGTH_SHORT).show()
                        headerLayout.visibility = View.GONE
                        searchEditText.setText("")
                    }
                    return@launch
                }
                currentTableMeta = targetTable
                val tableId = targetTable.id

                val columns = db.myColumnMetaDao().getColumnsByTableMetaId(tableId)
                if (columns.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        tableNameText.text = targetTable.displayName ?: "未命名表"
                        Toast.makeText(requireContext(), "该表没有定义列", Toast.LENGTH_SHORT).show()
                        currentColumns = emptyList()
                        fullDataList = emptyList()
                        currentDataList = emptyList()
                        recyclerView.adapter = TableDataAdapter(emptyList(), emptyList())
                        headerLayout.removeAllViews()
                        headerLayout.visibility = View.GONE
                        searchEditText.setText("")
                    }
                    return@launch
                }
                currentColumns = columns

                val rows = db.myTableRowDao().getRowsByTableId(tableId)
                val cellValuesMap = mutableMapOf<Long, MutableMap<Long, String>>()
                for (row in rows) {
                    val cells = db.myCellValueDao().getByRowId(row.id)
                    cells.forEach { cell ->
                        cellValuesMap.getOrPut(row.id) { mutableMapOf() }[cell.colMetaId] = cell.valueText ?: ""
                    }
                }
                val dataList = rows.map { row -> cellValuesMap[row.id] ?: emptyMap() }
                fullDataList = dataList
                currentDataList = dataList.toList()

                val totalWidthPx = columns.sumOf { dpToPx(it.commonWidth) }
                val screenWidth = resources.displayMetrics.widthPixels
                Log.d(TAG, "totalWidthPx = $totalWidthPx, screenWidth = $screenWidth")
                Log.d(TAG, "是否可滚动: ${totalWidthPx > screenWidth}")

                withContext(Dispatchers.Main) {
                    // 创建或更新适配器
                    if (tableDataAdapter == null) {
                        tableDataAdapter = TableDataAdapter(currentDataList, columns)
                        recyclerView.adapter = tableDataAdapter
                    } else {
                        tableDataAdapter?.updateData(currentDataList)
                        tableDataAdapter?.updateColumns(columns)
                    }

                    // 刷新表头
                    headerLayout.removeAllViews()
                    val headerView = tableDataAdapter!!.getHeaderView(headerLayout)
                    headerLayout.addView(headerView)
                    headerLayout.layoutParams =
                        FrameLayout.LayoutParams(totalWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
                    headerLayout.visibility = View.VISIBLE

                    recyclerView.layoutParams =
                        FrameLayout.LayoutParams(totalWidthPx, ViewGroup.LayoutParams.MATCH_PARENT)
                    tableNameText.text = targetTable.displayName ?: "未命名表"

                    // 重置滚动位置并清空搜索框
                    headerScroll.scrollTo(0, 0)
                    dataScroll.scrollTo(0, 0)
                    searchEditText.setText("")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "加载数据失败: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showSwitchTableDialog() {
        lifecycleScope.launch(Dispatchers.IO) {
            val allTables = db.myTableMetaDao().getAll()
            if (allTables.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "没有其他表", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val tableNames = allTables.map { it.displayName ?: "未命名表" }
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(requireContext())
                    .setTitle("选择表")
                    .setItems(tableNames.toTypedArray()) { _, which ->
                        loadTableData(allTables[which])
                    }
                    .show()
            }
        }
    }

    private fun showAddRowDialog() {
        val context = requireContext()
        val columns = currentColumns
        if (columns.isEmpty()) {
            Toast.makeText(context, "当前表没有列，无法添加数据", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 20)

        val editTexts = mutableListOf<EditText>()
        for (col in columns) {
            val label = TextView(context)
            label.text = col.colName ?: "列${col.id}"
            label.textSize = 16f
            layout.addView(label)

            val editText = EditText(context)
            editText.hint = "请输入${col.colName ?: "值"}"
            if (col.defaultValue != null) {
                editText.setText(col.defaultValue)
            }
            layout.addView(editText)
            editTexts.add(editText)
        }

        AlertDialog.Builder(context)
            .setTitle("新增行数据")
            .setView(layout)
            .setPositiveButton("保存") { _, _ ->
                val values = editTexts.map { it.text.toString() }
                saveNewRow(values)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveNewRow(values: List<String>) {
        val tableMeta = currentTableMeta ?: return
        val columns = currentColumns
        if (values.size != columns.size) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val newRow = MyTableRow(tableId = tableMeta.id)
                val rowId = db.myTableRowDao().insert(newRow)
                for (i in columns.indices) {
                    val cellValue = MyCellValue(
                        rowId = rowId,
                        colMetaId = columns[i].id,
                        valueText = values[i].takeIf { it.isNotEmpty() }
                    )
                    db.myCellValueDao().insert(cellValue)
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "新增成功", Toast.LENGTH_SHORT).show()
                    loadTableData(currentTableMeta)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "新增失败: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun dpToPx(dp: Float): Int = (dp * resources.displayMetrics.density).toInt()

    // ================== 内部适配器 ==================
    private inner class TableDataAdapter(
        private var dataList: List<Map<Long, String>>,
        private var columns: List<MyColumnMeta>
    ) : RecyclerView.Adapter<TableDataAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = LayoutInflater.from(this@Fragment1.requireContext())
                .inflate(R.layout.item_table_row, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(dataList[position], columns)
        }

        override fun getItemCount(): Int = dataList.size

        fun updateData(newDataList: List<Map<Long, String>>) {
            this.dataList = newDataList
            notifyDataSetChanged()
        }

        fun updateColumns(newColumns: List<MyColumnMeta>) {
            this.columns = newColumns
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val rowContainer: LinearLayout = itemView.findViewById(R.id.row_container)

            fun bind(rowData: Map<Long, String>, columns: List<MyColumnMeta>) {
                rowContainer.removeAllViews()
                for (col in columns) {
                    val value = rowData[col.id] ?: ""
                    val textView = createTextView(value, col.commonWidth)
                    rowContainer.addView(textView)
                }
            }
        }

        private fun createTextView(text: String, widthDp: Float): TextView {
            return TextView(this@Fragment1.requireContext()).apply {
                this.text = text
                setPadding(16, 12, 16, 12)
                layoutParams = LinearLayout.LayoutParams(
                    this@Fragment1.dpToPx(widthDp),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setBackgroundResource(android.R.drawable.editbox_background)
                gravity = Gravity.CENTER_VERTICAL
            }
        }

        fun getHeaderView(parent: ViewGroup): View {
            val headerView = LayoutInflater.from(this@Fragment1.requireContext())
                .inflate(R.layout.item_table_header, parent, false)
            val headerContainer = headerView.findViewById<LinearLayout>(R.id.header_container)
            headerContainer.removeAllViews()
            for (col in columns) {
                val tv = TextView(this@Fragment1.requireContext()).apply {
                    text = col.colName ?: "未命名"
                    setPadding(16, 12, 16, 12)
                    layoutParams = LinearLayout.LayoutParams(
                        this@Fragment1.dpToPx(col.commonWidth),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setBackgroundColor(Color.parseColor("#E0E0E0"))
                    gravity = Gravity.CENTER_VERTICAL
                    textSize = 14f
                }
                headerContainer.addView(tv)
            }
            return headerView
        }
    }
}