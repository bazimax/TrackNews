package com.example.tracknews.News

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracknews.R
import com.example.tracknews.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentNewsSavedBinding
import com.example.tracknews.parseSite.ParserSites

class NewsSavedFragment : Fragment() {

    lateinit var binding: FragmentNewsSavedBinding
    private val vm: ViewModel by activityViewModels()
    private val newsItemAdapter = NewsItemAdapter()
    private var parserSites = ParserSites()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsSavedBinding.inflate(inflater)
        return binding.root
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_news_saved, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.newsItemArray.value?.let { newsItemAdapter.addAllNews(it) }

        vm.newsItemArray.observe(activity as LifecycleOwner) {
            vm.newsItemArray.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.testFragNewsSavedGO.setOnClickListener {
            Log.d("TAG1", "Click - Button")
            vm.newsItemTempYa.value = null
            Log.d("TAG1", "vm.newsItemTempYa: ${vm.newsItemTempYa.value}")
            vm.newsItemTempYa.value = parserSites.parse(binding.testFragNewsSavedEditText.text.toString())
            Log.d("TAG1", "vm.newsItemTempYa: ${binding.testFragNewsSavedEditText.text}")
            Log.d("TAG1", "vm.newsItemTempYa: ${vm.newsItemTempYa.value}")
            Log.d("TAG1", "End button click -----")
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = NewsSavedFragment()
    }

    private fun init(){
        binding.apply {
            //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
            testFragNewsSavedRecyclerView.layoutManager = LinearLayoutManager(view?.context)
            testFragNewsSavedRecyclerView.adapter = newsItemAdapter

            /*vm.newsItem.value?.let { newsItemAdapter.addAllNews(it) }

            vm.newsItem.observe(activity as LifecycleOwner) {
                vm.newsItem.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
            }*/
        }
    }
}