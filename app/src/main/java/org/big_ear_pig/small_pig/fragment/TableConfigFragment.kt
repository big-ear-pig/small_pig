// TableConfigFragment.kt
package org.big_ear_pig.small_pig.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.big_ear_pig.small_pig.R
import org.big_ear_pig.small_pig.module.AppDatabase
import org.big_ear_pig.small_pig.module.table.MyTableMeta

class TableConfigFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var rvTableList: RecyclerView
    private lateinit var adapter: TableListAdapter
    private val tableList = mutableListOf<MyTableMeta>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page_table_config_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getInstance(requireContext().applicationContext)

        rvTableList = view.findViewById(R.id.rvTableList)
        rvTableList.layoutManager = LinearLayoutManager(requireContext())
        adapter = TableListAdapter(tableList) { table ->
            // 点击表项，进入字段配置 Fragment
            val columnFragment = ColumnConfigFragment.newInstance(table.id, table.displayName ?: "未命名")
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, columnFragment)
                .commit()
        }
        rvTableList.adapter = adapter

        view.findViewById<Button>(R.id.btnAddTable).setOnClickListener {
            showAddTableDialog()
        }

        loadTables()
    }

    override fun onResume() {
        super.onResume()
        loadTables()
    }

    private fun loadTables() {
        lifecycleScope.launch {
            val tables = db.myTableMetaDao().getAll()
            tableList.clear()
            tableList.addAll(tables)
            adapter.notifyDataSetChanged()
        }
    }

    private fun showAddTableDialog() {
        val editText = EditText(requireContext())
        editText.hint = "请输入表显示名称，例如：通讯录"
        AlertDialog.Builder(requireContext())
            .setTitle("添加新表")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    lifecycleScope.launch {
                        val table = MyTableMeta(displayName = name)
                        db.myTableMetaDao().insert(table)
                        loadTables()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    inner class TableListAdapter(
        private val items: List<MyTableMeta>,
        private val onItemClick: (MyTableMeta) -> Unit
    ) : RecyclerView.Adapter<TableListAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val table = items[position]
            holder.textView.text = table.displayName ?: "未命名表"
            holder.itemView.setOnClickListener { onItemClick(table) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(android.R.id.text1)
        }
    }

    companion object {
        fun newInstance(): TableConfigFragment = TableConfigFragment()
    }
}