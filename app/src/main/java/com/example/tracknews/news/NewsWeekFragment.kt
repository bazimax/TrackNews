package com.example.tracknews.news

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.MainActivity
import com.example.tracknews.ViewModel
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.FragmentFunction
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentNewsWeekBinding


class NewsWeekFragment : Fragment(), NewsItemAdapter.Listener {
    private val logNameClass = "NewsWeekFragment"

    lateinit var binding: FragmentNewsWeekBinding
    private val vm: ViewModel by activityViewModels()
    private val newsItemAdapter = NewsItemAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewsWeekBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rcView = binding.fragNewsSavedRecyclerView
        FragmentFunction(vm).startRecyclerView(rcView, newsItemAdapter)
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsWeekFragment()
    }

    private fun init(){
        //запуск фрагмента

        //отслеживаем изменения в данных для RecyclerView (SQLite > ViewModel)
        vm.newsItemArrayMonth.value?.let { newsItemAdapter.addAllNews(it) }
        vm.newsItemArrayMonth.observe(activity as LifecycleOwner) {
            vm.newsItemArrayMonth.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
        }
    }


    //одинаково во всех 4-х фрагментах (today, week, all, saved) >
    //При клике на элемент > загрузка интернет страницы
    override fun runWebsite(newsItem: NewsItem) {
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f runWebsite")
        FragmentFunction(vm).loadWebsiteFragment(newsItem, activity as MainActivity) //загрузка интернет страницы
    }

    //сохраняем новость (отмечаем звездочкой)
    override fun changeStatusSaved(newsItem: NewsItem) {
        vm.newsItemUpdateItem.value = newsItem
    }

    //показываем полный текст NewsItem //??"в работе"
    override fun expandContent(newsItem: NewsItem) {
        Toast.makeText(view?.context, newsItem.content, Toast.LENGTH_SHORT).show()
    }
    //одинаково во всех 4х фрагментах (today, week, all, saved) ^
}