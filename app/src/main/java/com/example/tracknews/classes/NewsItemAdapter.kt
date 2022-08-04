package com.example.tracknews.classes

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.R
import com.example.tracknews.ViewModel
import com.example.tracknews.databinding.RecyclerViewNewsItemBinding
import com.example.tracknews.databinding.RecyclerViewNewsItemV2Binding

class NewsItemAdapter: RecyclerView.Adapter<NewsItemAdapter.NewsItemHolder>() {

    private val newsItemList = ArrayList<NewsItem>()

    class NewsItemHolder(item: View): RecyclerView.ViewHolder(item) {
        //val binding = RecyclerViewNewsItemBinding.bind(item) //использует cardView
        val binding = RecyclerViewNewsItemV2Binding.bind(item) //использует linearLayout
        fun bind(newsItem: NewsItem) = with (binding){
            newsItemTextViewTitle.text = newsItem.title
            newsItemTextViewContent.text = newsItem.content
            newsItemTextViewLink.text = "new@yandex.ru"
        }
        /*//без with (binding)
        fun bind2(newsItem: NewsItem){
            binding.newsItemTextViewTitle.text = newsItem.title
            newsItemTextViewContent.text = newsItem.content
            newsItemTextViewLink.text = "new@yandex.ru"
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsItemHolder {
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_news_item, parent, false) //использует cardView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_news_item_v2, parent, false) //использует linearLayout
        return NewsItemHolder(view)
    }

    override fun onBindViewHolder(holder: NewsItemHolder, position: Int) {
        holder.bind(newsItemList[position])
    }

    override fun getItemCount(): Int {
        return newsItemList.size
    }

    fun addNewsItem(newsItem: NewsItem){
        newsItemList.add(newsItem)
        notifyDataSetChanged()
    }

    fun addAllNews(list: List<NewsItem>){
        newsItemList.clear()
        newsItemList.addAll(list)
        notifyDataSetChanged()
    }
}