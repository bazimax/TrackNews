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
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import com.example.tracknews.ViewModel


class SearchItemAdapter(private val listener: Listener): RecyclerView.Adapter<SearchItemAdapter.SearchItemHolder>() {

    private val searchItemList = ArrayList<SearchItem>()

    class SearchItemHolder(item: View): RecyclerView.ViewHolder(item) {

        val binding = RecyclerViewSearchItemBinding.bind(item)

        fun bind(searchItem: SearchItem, listener: Listener) = with (binding){
            searchItemButton.text = searchItem.search
            searchItemButtonActive.text = searchItem.search
            searchItemButtonSelect.text = searchItem.search
            searchItemCount.text = searchItem.counterNewNews.toString()

            //Если есть хотя бы одна новость для данного "сохраненного поиска", то показываем количество всех его новостей
            //??
            if (searchItem.counterAllNews > 0) {
                searchItemButton.text = searchItem.search + " " + searchItem.counterAllNews
                searchItemButtonActive.text = searchItem.search + " " + searchItem.counterAllNews
                searchItemButtonSelect.text = searchItem.search + " " + searchItem.counterAllNews
            }

            searchItemButtonSelect.visibility = 4 //??
            searchItemButtonActive.visibility = 4 //??
            searchItemButton.visibility = 0 //??

            //Если это активный "сохраненный поиск", то выделяем его
            if (searchItem.active) {
                searchItemButtonActive.visibility = 0
                searchItemButton.visibility = 4
            }

            //Log.d("TAG1", "bind ${searchItem.active}")
            //Если счетчик новых новостей больше 0, то мы его показываем
            if(searchItem.counterNewNews > 0) {
                searchItemCount.visibility = VISIBLE
            }
            else searchItemCount.visibility = GONE

            //Log.d("TAG1", "SearchItemAdapter >f bind > searchItem: $searchItem")
            //Log.d("TAG1", "SearchItemAdapter >f bind > searchItemButtonSelect: ${searchItemButtonSelect.visibility}")
            //Log.d("TAG1", "SearchItemAdapter >f bind > searchItemButton: ${searchItemButton.visibility}")

            //При нажатии на "сохраненный поиск" в rcView открываются все новости относящиеся к нему
            searchItemButton.setOnClickListener {
                //Log.d("TAG1", "click observe ${newsItem.link}")
                //Log.d("TAG1", "click observe $adapterPosition")
                Log.d("TAG1", "click observe ${searchItem.search}")
                listener.clickOnSearchItem(searchItem)
                /*if (counter == 0) {

                }
                else {
                    counter++
                    Log.d("TAG1", "click observe $counter")
                    searchItemButtonSelect.visibility = VISIBLE
                    searchItemButton.visibility = INVISIBLE
                    listener.selectSearchItem(searchItem)
                }*/
            }

            //Выделяем активный "сохраненный поиск", чтобы в будущем удалить
            searchItemButtonActive.setOnLongClickListener {
                searchItemButtonSelect.visibility = VISIBLE
                searchItemButtonActive.visibility = INVISIBLE
                listener.selectSearchItem(searchItem)
                true
            }

            //Выделяем "сохраненный поиск", чтобы в будущем удалить
            searchItemButton.setOnLongClickListener {
                searchItemButtonSelect.visibility = VISIBLE
                searchItemButton.visibility = INVISIBLE
                listener.selectSearchItem(searchItem)
                true
            }

            //Снимаем выделение с "сохраненного поиска"
            searchItemButtonSelect.setOnClickListener {
                //если снимаем выделение с активного "сохраненного поиска" то показываем выделенной кнопкой. Иначе обычной
                if (searchItem.active) {
                    searchItemButtonSelect.visibility = INVISIBLE
                    searchItemButtonActive.visibility = VISIBLE
                }
                else {
                    searchItemButtonSelect.visibility = INVISIBLE
                    searchItemButton.visibility = VISIBLE
                }
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