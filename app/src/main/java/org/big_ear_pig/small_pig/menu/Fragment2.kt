package org.big_ear_pig.small_pig.menu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.big_ear_pig.small_pig.R
import org.big_ear_pig.small_pig.module.AppDatabase
import org.big_ear_pig.small_pig.module.article.MyArticle
import kotlinx.coroutines.launch

class Fragment2 : Fragment() {

    private lateinit var searchEditText: androidx.appcompat.widget.AppCompatEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArticleAdapter

    // 分页参数
    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private var hasMore = true
    private var currentKeyword = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        val addButton = view.findViewById<android.widget.Button>(R.id.addButton)
        recyclerView = view.findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ArticleAdapter(emptyList()) { article ->
            navigateToDetail(article)
        }
        recyclerView.adapter = adapter

        // 滚动到底部加载更多
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!isLoading && hasMore && lastVisibleItem >= totalItemCount - 2) {
                    loadMoreArticles()
                }
            }
        })

        // 搜索框监听
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentKeyword = s.toString()
                resetAndLoadFirstPage()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        addButton.setOnClickListener {
            navigateToAdd()
        }

        // 初始加载第一页
        resetAndLoadFirstPage()
    }

    override fun onResume() {
        super.onResume()
        // 从新增/详情页返回时刷新列表（回到第一页）
        resetAndLoadFirstPage()
    }

    private fun resetAndLoadFirstPage() {
        currentPage = 0
        hasMore = true
        isLoading = false
        adapter.updateList(emptyList())
        loadMoreArticles()
    }

    private fun loadMoreArticles() {
        if (isLoading || !hasMore) return
        isLoading = true

        lifecycleScope.launch {
            try {
                val dao = AppDatabase.getInstance(requireContext()).myArticleDao()
                val offset = currentPage * pageSize
                val newArticles: List<MyArticle> = if (currentKeyword.isEmpty()) {
                    dao.getArticlesPaged(limit = pageSize, offset = offset)
                } else {
                    dao.searchArticlesPaged(
                        keyword = currentKeyword,
                        limit = pageSize,
                        offset = offset
                    )
                }

                // 判断是否还有更多数据
                if (newArticles.size < pageSize) {
                    hasMore = false
                }

                val currentList = adapter.currentList.toMutableList()
                if (currentPage == 0) {
                    adapter.updateList(newArticles)
                } else {
                    currentList.addAll(newArticles)
                    adapter.updateList(currentList)
                }

                currentPage++
            } finally {
                isLoading = false
            }
        }
    }

    private fun navigateToDetail(article: MyArticle) {
        // TODO: 跳转详情页
        // 例如：startActivity(Intent(requireContext(), ArticleDetailActivity::class.java).apply {
        //     putExtra("article_id", article.id)
        // })
    }

    private fun navigateToAdd() {
        // TODO: 跳转新增页
        // 例如：startActivity(Intent(requireContext(), AddArticleActivity::class.java))
    }

    // ========== 内部适配器（使用 MyArticle） ==========
    private class ArticleAdapter(
        private var articles: List<MyArticle>,
        private val onItemClick: (MyArticle) -> Unit
    ) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

        // 供外部获取当前数据列表（用于分页追加）
        val currentList: List<MyArticle>
            get() = articles

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_article, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val article = articles[position]
            holder.bind(article)
            holder.itemView.setOnClickListener { onItemClick(article) }
        }

        override fun getItemCount(): Int = articles.size

        fun updateList(newList: List<MyArticle>) {
            articles = newList
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val imageView = itemView.findViewById<android.widget.ImageView>(R.id.articleImage)
            private val titleTextView = itemView.findViewById<android.widget.TextView>(R.id.articleTitle)
            private val summaryTextView = itemView.findViewById<android.widget.TextView>(R.id.articleSummary)

            fun bind(article: MyArticle) {
                Glide.with(itemView.context)
                    .load(article.imagePath)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView)

                titleTextView.text = article.title
                summaryTextView.text = article.summary
            }
        }
    }
}