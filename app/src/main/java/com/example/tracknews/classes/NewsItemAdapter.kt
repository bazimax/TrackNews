package com.example.tracknews.classes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.R
import com.example.tracknews.databinding.RecyclerViewNewsItemBinding


class NewsItemAdapter(private val listener: Listener): RecyclerView.Adapter<NewsItemAdapter.NewsItemHolder>() {
    private val newsItemList = ArrayList<NewsItem>()

    class NewsItemHolder(item: View): RecyclerView.ViewHolder(item) {

        private val ctx = item.context

        val binding = RecyclerViewNewsItemBinding.bind(item) //использует cardView

        fun bind(newsItem: NewsItem, listener: Listener) = with (binding){

            var date = "1985-10-26T09:00:00+03:00"

            if (newsItem.date != "") {
                date = newsItem.date
            }

            val time = DateFunctions().parseNewsItemDate(date, ctx)
            Log.d(Constants.TAG_DATA_IF, "NewsItemAdapter >f bind > time: $time")
            Log.d(Constants.TAG_DATA_IF, "NewsItemAdapter >f bind > time 0: ${time[0]}")
            Log.d(Constants.TAG_DATA_IF, "NewsItemAdapter >f bind > time 1: ${time[1]}")
            Log.d(Constants.TAG_DATA_IF, "NewsItemAdapter >f bind > time 2: ${time[2]}")
            Log.d(Constants.TAG_DATA_IF, "NewsItemAdapter >f bind > time size: ${time.size}")


            newsItemTextViewTime.text = time[0]
            newsItemTextViewDayMonth.text = time[1]
            newsItemTextViewYear.text = time[2]
            newsItemTextViewTitle.text = newsItem.title
            newsItemTextViewContent.text = newsItem.content
            newsItemTextViewLink.text = newsItem.link

            //если года нет, то скрываем поле
            // (у xiaomi, при включении автоматической темной темы, отсутствующее поле года заливается ровным цветом и становится видимым)
            if (time[2] == "") {
                newsItemTextViewYear.visibility = View.GONE
            }

            //Если новость сохранена ранее отмечаем соответствующей кнопкой. Если нет, то по умолчанию
            if (newsItem.statusSaved == "true") {
                newsItemButtonSave.visibility = View.VISIBLE
                newsItemButtonUnSave.visibility = View.GONE
            }
            else {
                newsItemButtonUnSave.visibility = View.VISIBLE
                newsItemButtonSave.visibility = View.GONE
            }

            newsItemButtonSave.setOnClickListener {
                //Сохраняем новость в закладки
                listener.changeStatusSaved(newsItem)
            }
            newsItemButtonUnSave.setOnClickListener {
                //Убираем новость из закладок
                listener.changeStatusSaved(newsItem)
            }

            newsItemCardView.setOnClickListener {
                //Открываем новость в браузере
                listener.runWebsite(newsItem)
            }

            newsItemTextViewContent.setOnClickListener {
                //При нажатии на "content" (частичный текст) -> показывается "весь текст" и наоборот
                if (newsItemTextViewContent.lineCount > 1 && newsItemTextViewContent.maxLines == 2) {
                    newsItemTextViewContent.layoutParams.height = -1
                    newsItemTextViewContent.maxLines = 999
                }
                else if (newsItemTextViewContent.lineCount == 1) {
                    listener.runWebsite(newsItem)
                }
                else {
                    newsItemTextViewContent.layoutParams.height = 88
                    newsItemTextViewContent.maxLines = 2
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_news_item, parent, false) //использует cardView
        return NewsItemHolder(view)
    }

    override fun onBindViewHolder(holder: NewsItemHolder, position: Int) {
        holder.bind(newsItemList[position], listener)
    }

    override fun getItemCount(): Int {
        return newsItemList.size
    }

    fun addAllNews(listNewsItem: List<NewsItem>){
        newsItemList.clear()
        newsItemList.addAll(listNewsItem)
        notifyDataSetChanged() //!! без этой штуки не работает "звездочка" пометки лучшее на новости
    }

    interface Listener {
        fun runWebsite(newsItem: NewsItem)
        fun changeStatusSaved(newsItem: NewsItem)
        fun expandContent(newsItem: NewsItem)
    }
}