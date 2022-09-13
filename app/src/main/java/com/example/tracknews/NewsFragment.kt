package com.example.tracknews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.tracknews.News.*
import com.example.tracknews.databinding.FragmentNewsBinding
import com.google.android.material.tabs.TabLayoutMediator

class NewsFragment : Fragment() {
    private val fragList = listOf(
        NewsTodayFragment.newInstance(),
        NewsWeekFragment.newInstance(),
        NewsAllFragment.newInstance(),
        NewsSavedFragment.newInstance()
    )
    /*private var fragListTitles = listOf(
        //getString(R.string.news_today),
        *//*getString(R.string.news_today),
        getString(R.string.news_week),
        getString(R.string.news_all),
        getString(R.string.news_saved)*//*
        "Today",
        "Week",
        "All Time",
        "Saved"
    )*/


    private lateinit var binding: FragmentNewsBinding
    //private val vm: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewsBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Log.d("TAG1", "R string: ${getString(R.string.news_today)}")

        val fragListTitles = listOf(
            //getString(R.string.news_today),
            getString(R.string.news_today),
            getString(R.string.news_week),
            getString(R.string.news_all),
            getString(R.string.news_saved)
        )

        val adapter = ViewPager2Adapter(this, fragList)
        binding.fragNewsViewPager2.adapter = adapter
        TabLayoutMediator(binding.fragNewsTab, binding.fragNewsViewPager2) {
            tab, pos -> tab.text = fragListTitles[pos]
        }.attach()
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsFragment()
    }
}