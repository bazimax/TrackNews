package com.example.tracknews.TestFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.R
import com.example.tracknews.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentTest2Binding


class TestFragment2 : Fragment() {
    lateinit var binding: FragmentTest2Binding
    private val newsItemAdapter = NewsItemAdapter()
    private var index = 0
    private val vm: ViewModel by activityViewModels()

    //private var recyclerView: RecyclerView? = null //для fun init2()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTest2Binding.inflate(layoutInflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_test2, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.newsItem.value?.let { newsItemAdapter.addAllNews(it) }

        vm.newsItem.observe(activity as LifecycleOwner) {
            vm.newsItem.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    companion object {

        @JvmStatic
        fun newInstance() = TestFragment2()
    }

    private fun init(){
        binding.apply {
            //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
            fragTest2RecyclerView.layoutManager = LinearLayoutManager(view?.context)
            fragTest2RecyclerView.adapter = newsItemAdapter

            /*vm.newsItem.value?.let { newsItemAdapter.addAllNews(it) }

            vm.newsItem.observe(activity as LifecycleOwner) {
                vm.newsItem.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
            }*/

            fragTest2ButtonAddNewsItem.setOnClickListener {
                Log.d("TAG1", "button AddNewsItem click")
                val title = if (fragTest2TextViewTitle.text != null && fragTest2TextViewTitle.text.toString() != "") fragTest2TextViewTitle.text else "baseNone"
                //Log.d("TAG1", "click addNewsItem: $title + ${fragTest2TextViewTitle.text}")
                val content = if (fragTest2TextViewContent.text != null) fragTest2TextViewContent.text else "///"
                val link = if (fragTest2TextViewLink.text != null) fragTest2TextViewLink.text else "0000@ji.ru"
                val newsItem = NewsItem(title.toString(), content.toString(), link.toString())
                newsItemAdapter.addNewsItem(newsItem)
                vm.newsItemTemp.value = newsItem

                /*if (vm.newsItem.value != null) {
                    newsItemAdapter.addAllNews(vm.newsItem.value!!)
                }*/

                //vm.newsItem.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }

                Log.d("TAG1", "AddNewsItem > newsItemTemp: $newsItem")
                Log.d("TAG1", "AddNewsItem > newsItem value: ${vm.newsItem.value}")
                Log.d("TAG1", "AddNewsItem > newsItemTemp value: ${vm.newsItemTemp.value}")
                Log.d("TAG1", "AddNewsItem > button AddNewsItem click ------------END")
            }
        }
    }

    /*private fun init2(){
        //почти тоже самое что и простой init
        recyclerView = view?.findViewById(R.id.fragTest2RecyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(view?.context)
        recyclerView?.adapter = newsItemAdapter

        binding.apply {
            fragTest2ButtonAddNewsItem.setOnClickListener {
                Log.d("TAG1", "button AddNewsItem click")
                val title = if (fragTest2TextViewTitle.text != null) fragTest2TextViewTitle.text else "baseNone"
                val content = if (fragTest2TextViewContent.text != null) fragTest2TextViewContent.text else "///"
                val link = if (fragTest2TextViewLink.text != null) fragTest2TextViewLink.text else "0000@ji.ru"
                val newsItem = NewsItem(title.toString(), content.toString(), link.toString())
                newsItemAdapter.addNewsItem(newsItem)
            }
        }
    }*/
}