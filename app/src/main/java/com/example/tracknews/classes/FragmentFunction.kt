package com.example.tracknews.classes

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tracknews.MainActivity
import com.example.tracknews.ViewModel
import com.example.tracknews.WebsiteFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FragmentFunction(viewModel: ViewModel) {
    private val logNameClass = "FragmentFunction" //для логов

    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
    }

    private val vm = viewModel

    fun nameTab(view: View, tabLayout: TabLayout, viewPager2: ViewPager2){
        Log.d(TAG_DEBUG, "$logNameClass >f nameTab === START")

        val nameTab = NameTab(vm)
        val listNameTab = nameTab.listName(view.context) //V1

        Log.d(TAG_DATA, "$logNameClass > nameTab: $listNameTab")

        TabLayoutMediator(tabLayout, viewPager2) {
                tab, pos -> tab.text = nameTab.listName(view.context)[pos]
        }.attach()

        Log.d(TAG_DEBUG, "$logNameClass >f nameTab ----- END")
    }

    //загрузка интернет страницы
    fun loadWebsiteFragment(newsItem: NewsItem, activity: MainActivity) {
        Log.d(TAG_DEBUG, "$logNameClass >f loadWebsiteFragment === START")
        Log.d(TAG_DATA, "$logNameClass >f loadWebsiteFragment > newsItem: $newsItem")

        if (newsItem.link.contains("https://")) {
            vm.tempNewsItemOpenWebsite.value = newsItem

            if (vm.statusLandscape.value == "true") {
                //загрузка интернет страницы
                //2-й вариант - замена одного фрагмента на другой - WebSiteFragment
                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(com.example.tracknews.R.id.frameLayoutSuperMain, WebsiteFragment.newInstance())
                    .addToBackStack("main")
                    .commit()
            }
            else {
                activity.supportFragmentManager
                    .beginTransaction()
                    .replace(com.example.tracknews.R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
                    .commit()
            }
        }
        else {
            Toast.makeText(activity, "Bad link", Toast.LENGTH_SHORT).show() //??
        }
        Log.d(TAG_DEBUG, "$logNameClass >f loadWebsiteFragment ----- END")
    }

    //отслеживаем движение по экрану, чтобы скрывать форму поиска
    private fun onTouch(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                if (vm.statusSearchMutable.value == true.toString()) {
                    vm.statusSearchMutable.value = false.toString()
                }
            }
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
        }
        return false //если поставить true, то scroll перестает работать
    }

    //Подключаем RecyclerView и отображаем данные из SQLite
    @SuppressLint("ClickableViewAccessibility") //для setOnTouchListener
    fun startRecyclerView(rcView: RecyclerView, newsItemAdapter: NewsItemAdapter){

        rcView.layoutManager = LinearLayoutManager(rcView.context) //проверить
        rcView.adapter = newsItemAdapter

        //Отслеживаем движение по экрану, чтобы скрывать форму поиска
        rcView.setOnTouchListener { _, event -> onTouch(event)}
    }

    //прогресс-бар
    fun progressBar(view: View) {

        if (vm.statusProgressBar.value == true) {
            view.visibility = View.VISIBLE
        }
        else {
            view.visibility = View.GONE
        }
    }

    fun progressBarSwap(view: View, progressBar: View) {

        if (vm.statusProgressBar.value == true) {
            progressBar.visibility = View.VISIBLE
            view.visibility = View.GONE
        }
        else {
            progressBar.visibility = View.GONE
            view.visibility = View.VISIBLE
        }
    }
}