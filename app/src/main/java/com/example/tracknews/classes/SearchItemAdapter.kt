package com.example.tracknews.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.R
import com.example.tracknews.databinding.RecyclerViewSearchItemBinding

class SearchItemAdapter: RecyclerView.Adapter<SearchItemAdapter.SearchItemHolder>() {

    private val searchItemList = ArrayList<SearchItem>()

    class SearchItemHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = RecyclerViewSearchItemBinding.bind(item)
        fun bind(searchItem: SearchItem) = with (binding){
            searchItemButton.text = searchItem.search
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchItemAdapter.SearchItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_search_item, parent, false)
        return SearchItemAdapter.SearchItemHolder(view)
    }

    override fun onBindViewHolder(holder: SearchItemAdapter.SearchItemHolder, position: Int) {
        holder.bind(searchItemList[position])
    }

    override fun getItemCount(): Int {
        return searchItemList.size
    }

    fun addSearchItem(searchItem: SearchItem){
        searchItemList.add(searchItem)
        notifyDataSetChanged()
    }

    fun addAllSearch(list: List<SearchItem>){
        searchItemList.clear()
        searchItemList.addAll(list)
        notifyDataSetChanged()
    }
}