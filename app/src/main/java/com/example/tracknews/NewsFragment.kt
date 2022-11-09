package com.example.tracknews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.News.*
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.FragmentFunction
import com.example.tracknews.classes.NameTab
import com.example.tracknews.databinding.FragmentNewsBinding
import com.google.android.material.tabs.TabLayoutMediator

class NewsFragment : Fragment() {
    private val logNameClass = "NewsFragment"

    private val vm: ViewModel by activityViewModels()
    private lateinit var binding: FragmentNewsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated")
        val nameTab = NameTab(vm)
        Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > listFragment: ${nameTab.listFragment}")
        //Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > fragmentName: ${nameTab.fragmentName()}")
        //Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > tabName: ${nameTab.tabName(view.context)}")
        val adapter = ViewPager2Adapter(this, nameTab.listFragment) //?? old



        //val adapter = ViewPager2Adapter(this, nameTab.fragmentName())
        binding.fragNewsViewPager2.adapter = adapter



        /*vm.searchItemActive.observe(activity as LifecycleOwner) {
            FragmentFunction(vm).nameTab(view ,binding.fragNewsTab, binding.fragNewsViewPager2)
        }*/
        vm.newsItemArrayAll.observe(activity as LifecycleOwner) {
            FragmentFunction(vm).nameTab(view ,binding.fragNewsTab, binding.fragNewsViewPager2)
            Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > newsItemArrayAll.OBSERVE === START")
            Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > listFragment: ${nameTab.listFragment}")
            //Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > fragmentName: ${nameTab.fragmentName()}")
            //Log.d(Constants.TAG_DEBUG, "$logNameClass > onViewCreated > tabName: ${nameTab.tabName(view.context)}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsFragment()
    }

}

//BACKUP
/*val fragListTitles = listOf(
            //getString(R.string.news_today),
            getString(R.string.news_today) + "",
            getString(R.string.news_week),
            getString(R.string.news_all),
            getString(R.string.news_saved)
        )

        val adapter = ViewPager2Adapter(this, fragList2)
        binding.fragNewsViewPager2.adapter = adapter
        TabLayoutMediator(binding.fragNewsTab, binding.fragNewsViewPager2) {
                tab, pos -> tab.text = fragListTitles[pos]
        }.attach()*/

/*
private val fragList2: List<Fragment> = listOf(
    NewsTodayFragment.newInstance(),
    NewsWeekFragment.newInstance(),
    NewsAllFragment.newInstance(),
    NewsSavedFragment.newInstance()
)*/
