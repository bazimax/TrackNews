package com.example.tracknews.News

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
import okhttp3.*

import java.io.IOException


class NewsWeekFragment : Fragment(), NewsItemAdapter.Listener {
    private val logNameClass = "NewsWeekFragment"

    lateinit var binding: FragmentNewsWeekBinding
    private val vm: ViewModel by activityViewModels()
    private val newsItemAdapter = NewsItemAdapter(this)
    var okHttpClient: OkHttpClient = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewsWeekBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news_week, container, false)
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
            //Log.d(MainActivity.TAG, "NewsWeekFragment >f newsItemArrayMonth.OBSERVE > value: ${vm.newsItemArrayMonth.value}")
            vm.newsItemArrayMonth.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
        }
    }


    //одинаково во всех 4х фрагментах (today, week, all, saved) >
    //При клике на элемент > загрузка интеренет страницы
    override fun runWebsite(newsItem: NewsItem) {
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f runWebsite")
        loadProgressBar(newsItem.link) //прогрессбар
        FragmentFunction(vm).loadWebsiteFragment(newsItem, activity as MainActivity) //загрузка интеренет страницы
    }

    //сохраняем новость (отмечаем звездочкой)
    override fun changeStatusSaved(newsItem: NewsItem) {
        vm.newsItemUpdateItem.value = newsItem
    }

    //показываем полный текст NewsItem //??"в работе"
    override fun expandContent(newsItem: NewsItem) {
        Toast.makeText(view?.context, newsItem.content, Toast.LENGTH_SHORT).show()
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        //прогрессбар
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    private fun loadProgressBar(url: String) {
        //прогрессбар, продолжение
        val mess = resources.getString(com.example.tracknews.R.string.loadWebsiteFail)

        runOnUiThread {
            binding.fragNewsSavedProgressBar.visibility = View.VISIBLE
        }

        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                vm.messageLoadWebsite.value = mess
                //vm.messageFact.value = "Fail"
            }

            override fun onResponse(call: Call?, response: Response?) {
                runOnUiThread {
                    binding.fragNewsSavedProgressBar.visibility = View.GONE
                    //dataModel.messageFact.value = Html.fromHtml(txt).toString()
                    vm.messageLoadWebsite.value = "Good"
                }
            }
        })
    }
    //одинаково во всех 4х фрагментах (today, week, all, saved) ^
}