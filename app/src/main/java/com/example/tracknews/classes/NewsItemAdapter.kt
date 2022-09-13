package com.example.tracknews.classes

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.MainActivity
import com.example.tracknews.R
import com.example.tracknews.ViewModel
import com.example.tracknews.databinding.RecyclerViewNewsItemBinding
import com.example.tracknews.databinding.RecyclerViewNewsItemV2Binding
import java.security.AccessController.getContext

class NewsItemAdapter(val listener: Listener): RecyclerView.Adapter<NewsItemAdapter.NewsItemHolder>() {

    private val newsItemList = ArrayList<NewsItem>()

    class NewsItemHolder(item: View): RecyclerView.ViewHolder(item) {
        //private lateinit var vm: ViewModel

        val binding = RecyclerViewNewsItemBinding.bind(item) //использует cardView
        //val binding = RecyclerViewNewsItemV2Binding.bind(item) //использует linearLayout
        fun bind(newsItem: NewsItem, listener: Listener) = with (binding){
            newsItemTextViewDate.text = newsItem.date
            //newsItemTextViewImg.text = newsItem.img
            newsItemTextViewTitle.text = newsItem.title
            newsItemTextViewContent.text = newsItem.content
            newsItemTextViewLink.text = newsItem.link

            /*itemView.setOnClickListener {
                Log.d("TAG1", "click observe ${newsItem.title}")
                Log.d("TAG1", "click observe $adapterPosition")
                listener.onClick(newsItem)
            }*/

            newsItemButtonSave.setOnClickListener {
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.changeStatusSaved(newsItem)
            }

            newsItemCardView.setOnClickListener {
                //vm = ViewModelProvider(this).get(ViewModel)
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.runWebsite(newsItem)
                //Log.d("TAG1", "click observe ${vm}")
            }

            newsItemButtonExpand.setOnClickListener {
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.changeStatusSaved(newsItem)
            }
        }

        /*//без with (binding)
        fun bind2(newsItem: NewsItem){
            binding.newsItemTextViewTitle.text = newsItem.title
            newsItemTextViewContent.text = newsItem.content
            newsItemTextViewLink.text = "new@yandex.ru"
        }*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_news_item, parent, false) //использует cardView
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_news_item_v2, parent, false) //использует linearLayout
        return NewsItemHolder(view)
    }

    override fun onBindViewHolder(holder: NewsItemHolder, position: Int) {
        holder.bind(newsItemList[position], listener)
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

    interface Listener {
        fun runWebsite(newsItem: NewsItem)
        fun changeStatusSaved(newsItem: NewsItem)
        fun expandContent(newsItem: NewsItem)
    }
}