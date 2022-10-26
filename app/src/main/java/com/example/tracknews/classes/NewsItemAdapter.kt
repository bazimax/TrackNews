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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NewsItemAdapter(private val listener: Listener): RecyclerView.Adapter<NewsItemAdapter.NewsItemHolder>() {
    private val newsItemList = ArrayList<NewsItem>()

    class NewsItemHolder(item: View): RecyclerView.ViewHolder(item) {
        //private val dateFun = DateFunctions()
        private val ctx = item.context

        val binding = RecyclerViewNewsItemBinding.bind(item) //использует cardView
        //val binding = RecyclerViewNewsItemV2Binding.bind(item) //использует linearLayout
        fun bind(newsItem: NewsItem, listener: Listener) = with (binding){
            val time = DateFunctions().parseNewsItemDate(newsItem.date, ctx)

            newsItemTextViewTime.text = time[0]
            newsItemTextViewDayMonth.text = time[1]
            newsItemTextViewYear.text = time[2]
            //newsItemTextViewImg.text = newsItem.img
            newsItemTextViewTitle.text = newsItem.title
            newsItemTextViewContent.text = newsItem.content
            newsItemTextViewLink.text = newsItem.link

            //Если новость сохранена ранее отмечаем соответсвтующей кнопкой. Если нет то по умолчанию
            if (newsItem.statusSaved == "true") {
                newsItemButtonSave.visibility = View.VISIBLE
                newsItemButtonUnSave.visibility = View.GONE
                //newsItemButtonSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_select, 0, 0, 0)
            }
            else {
                newsItemButtonUnSave.visibility = View.VISIBLE
                newsItemButtonSave.visibility = View.GONE
                //newsItemButtonSave.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_unselect, 0, 0, 0)
            }

            newsItemButtonSave.setOnClickListener {
                //Сохраняем новость в закладки
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.changeStatusSaved(newsItem)
            }
            newsItemButtonUnSave.setOnClickListener {
                //Убираем новость из закладок
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.changeStatusSaved(newsItem)
            }

            newsItemCardView.setOnClickListener {
                //Открываем новость в браузере
                //vm = ViewModelProvider(this).get(ViewModel)
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.runWebsite(newsItem)
                //Log.d("TAG1", "click observe ${vm}")
            }

            newsItemTextViewContent.setOnClickListener {
                //При нажатии на "content" (частичный текст) -> показывается "весь текст" и наоборот
                //listener.changeStatusSaved(newsItem)
                if (newsItemTextViewContent.lineCount > 1 && newsItemTextViewContent.maxLines == 2) {
                    newsItemTextViewContent.layoutParams.height = -1
                    newsItemTextViewContent.maxLines = 999
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > IF > height: ${newsItemTextViewContent.height}")
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > IF > lineCount: ${newsItemTextViewContent.lineCount}")
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > IF > maxLines: ${newsItemTextViewContent.maxLines}")
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > IF > lineHeight: ${newsItemTextViewContent.lineHeight}")
                    //newsItemButtonExpand.visibility = View.VISIBLE
                }
                else if (newsItemTextViewContent.lineCount == 1) {
                    listener.runWebsite(newsItem)
                }
                else {
                    newsItemTextViewContent.layoutParams.height = 88
                    newsItemTextViewContent.maxLines = 2
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > ELSE > height: ${newsItemTextViewContent.height}")
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > ELSE > lineCount: ${newsItemTextViewContent.lineCount}")
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > ELSE > maxLines: ${newsItemTextViewContent.maxLines}")
                    //Log.d("TAG1", "NewsItemAdapter >f bind > Content > ELSE > lineHeight: ${newsItemTextViewContent.lineHeight}")
                    //newsItemButtonExpand.visibility = View.INVISIBLE
                }

            }

            newsItemButtonExpand.setOnClickListener {
                //скрываем текст
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")

            }
            //Thread.sleep(2000L)
            //Log.d("TAG1", "NewsItemAdapter >f bind > MaxLines: ${newsItemTextViewContent.lineCount}")
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

    fun addAllNews(listNewsItem: List<NewsItem>){
        newsItemList.clear()
        newsItemList.addAll(listNewsItem)
        notifyDataSetChanged()
    }

    interface Listener {
        fun runWebsite(newsItem: NewsItem)
        fun changeStatusSaved(newsItem: NewsItem)
        fun expandContent(newsItem: NewsItem)
    }
}