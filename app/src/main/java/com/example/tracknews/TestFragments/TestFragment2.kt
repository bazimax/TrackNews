package com.example.tracknews.TestFragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tracknews.R
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentTest2Binding


class TestFragment2 : Fragment() {
    lateinit var binding: FragmentTest2Binding
    private val adapter = NewsItemAdapter()
    private var index = 0

    private var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTest2Binding.inflate(layoutInflater)

        /*recyclerView = view?.findViewById(R.id.fragTest2RecyclerView);
        recyclerView?.setHasFixedSize(true);
        recyclerView?.layoutManager = LinearLayoutManager(view?.context)
        recyclerView?.adapter = adapter*/

        return binding.root
        //return inflater.inflate(R.layout.fragment_test2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init2()
    }

    companion object {

        @JvmStatic
        fun newInstance() = TestFragment2()
    }

    private fun init(){
        binding.apply {
            //fragTest2RecyclerView.layoutManager = LinearLayoutManager((view?.context ?: this) as Context?)
            fragTest2RecyclerView.layoutManager = LinearLayoutManager(view?.context)
            //fragTest2RecyclerView.layoutManager = GridLayoutManager((this@TestFragment2 as Context), 3)
            fragTest2RecyclerView.adapter = adapter
            fragTest2ButtonAddNewsItem.setOnClickListener {
                Log.d("TAG1", "button AddNewsItem click")
                val title = if (fragTest2TextViewTitle.text != null) fragTest2TextViewTitle.text else "baseNone"
                val content = if (fragTest2TextViewContent.text != null) fragTest2TextViewContent.text else "///"
                val link = if (fragTest2TextViewLink.text != null) fragTest2TextViewLink.text else "0000@ji.ru"
                val newsItem = NewsItem(title.toString(), content.toString(), link.toString())
                adapter.addNewsItem(newsItem)
            }
        }
    }

    private fun init2(){
        recyclerView = view?.findViewById(R.id.fragTest2RecyclerView);
        recyclerView?.setHasFixedSize(true);
        recyclerView?.layoutManager = LinearLayoutManager(view?.context)
        recyclerView?.adapter = adapter

        binding.apply {
            fragTest2ButtonAddNewsItem.setOnClickListener {
                Log.d("TAG1", "button AddNewsItem click")
                val title = if (fragTest2TextViewTitle.text != null) fragTest2TextViewTitle.text else "baseNone"
                val content = if (fragTest2TextViewContent.text != null) fragTest2TextViewContent.text else "///"
                val link = if (fragTest2TextViewLink.text != null) fragTest2TextViewLink.text else "0000@ji.ru"
                val newsItem = NewsItem(title.toString(), content.toString(), link.toString())
                adapter.addNewsItem(newsItem)
            }
        }
    }
}