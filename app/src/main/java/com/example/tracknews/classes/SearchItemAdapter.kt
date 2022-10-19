package com.example.tracknews.classes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.R
import com.example.tracknews.databinding.RecyclerViewSearchItemBinding
import android.widget.Toast


class SearchItemAdapter(private val listener: Listener): RecyclerView.Adapter<SearchItemAdapter.SearchItemHolder>() {

    private val searchItemList = ArrayList<SearchItem>()

    class SearchItemHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = RecyclerViewSearchItemBinding.bind(item)
        fun bind(searchItem: SearchItem, listener: Listener) = with (binding){

            searchItemButton.text = searchItem.search
            searchItemButtonSelect.text = searchItem.search
            searchItemCount.text = searchItem.counterNewNews.toString()

            searchItemButtonSelect.visibility = 4 //??
            searchItemButton.visibility = 0 //??

            //Если счетчик новых новостей больше 0, то мы его показываем
            if(searchItem.counterNewNews > 0) {
                searchItemCount.visibility = VISIBLE
            }
            else searchItemCount.visibility = INVISIBLE

            Log.d("TAG1", "SearchItemAdapter >f bind > searchItem: $searchItem")
            Log.d("TAG1", "SearchItemAdapter >f bind > searchItemButtonSelect: ${searchItemButtonSelect.visibility}")
            Log.d("TAG1", "SearchItemAdapter >f bind > searchItemButton: ${searchItemButton.visibility}")

            searchItemButton.setOnClickListener {
                //При нажатии на "сохраненный поиск" в rcView открываются все новости относящиеся к нему
                //Delete -> Сохраняем новость в закладки
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                listener.clickOnSearchItem(searchItem)
            }

            searchItemButton.setOnLongClickListener {
                //Выделяем "сохраненный поиск", чтобы в будущем удалить
                searchItemButtonSelect.visibility = VISIBLE
                searchItemButton.visibility = INVISIBLE
                listener.selectSearchItem(searchItem)
                true
            }

            searchItemButtonSelect.setOnClickListener {
                //Снимаем выделение с "сохраненного поиска"
                searchItemButtonSelect.visibility = INVISIBLE
                searchItemButton.visibility = VISIBLE
                listener.unSelectSearchItem(searchItem)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemHolder {
        //Log.d("TAG1", "SearchItemAdapter >f onCreateViewHolder ======START")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_search_item, parent, false)
        return SearchItemHolder(view)
    }

    override fun onBindViewHolder(holder: SearchItemHolder, position: Int) {
        holder.bind(searchItemList[position], listener)
    }

    override fun getItemCount(): Int {
        return searchItemList.size
    }

    fun addSearchItem(searchItem: SearchItem){
        searchItemList.add(searchItem)
        notifyDataSetChanged()
    }

    fun addAllSearch(list: List<SearchItem>){
        //Log.d("TAG1", "SearchItemAdapter >f addAllSearch > list: $list")
        searchItemList.clear()
        searchItemList.addAll(list)
        notifyDataSetChanged()
    }

    interface Listener {
        fun clickOnSearchItem(searchItem: SearchItem)
        fun selectSearchItem(searchItem: SearchItem)
        fun unSelectSearchItem(searchItem: SearchItem)
    }
}