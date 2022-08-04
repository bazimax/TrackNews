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
    private val fragListTitles = listOf(
        "Today",
        "Week",
        "All Time",
        "Saved"
    )
    private lateinit var binding: FragmentNewsBinding
    //lateinit var bindingActivityMainBinding: ActivityMainBinding
    private val vm: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.url2 = "0001" //не работает почему-то

        binding.fragNewsButtonShow.setOnClickListener {
            //раскрываем общую группу запросов/закладок/поисков/сохраненных поисков
            Log.d("TAG1", "mMainFrag = ${binding.fragNewsTableLayout.visibility}")
            Log.d("TAG1", "-------------------")
            Toast.makeText(context,"123",Toast.LENGTH_SHORT).show()
            if (binding.fragNewsTableLayout.visibility == View.VISIBLE) {
                binding.fragNewsTableLayout.visibility = View.GONE
            }
            else {
                binding.fragNewsTableLayout.visibility = View.VISIBLE
            }
        }

        val adapter = ViewPager2Adapter(this, fragList)
        binding.fragNewsViewPager2.adapter = adapter
        TabLayoutMediator(binding.fragNewsTab, binding.fragNewsViewPager2) {
            tab, pos -> tab.text = fragListTitles[pos]
        }.attach()

        /*binding.fragNewsTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    parentFragmentManager.beginTransaction().replace(R.id.fragNewsPlaceHolder, fragList[tab.position]).commit() //смена фрагментов в зависимости от TabLayout
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
            }
        })*/

    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsFragment()
    }
}