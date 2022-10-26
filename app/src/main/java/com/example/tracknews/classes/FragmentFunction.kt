package com.example.tracknews.classes

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
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
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, активити и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
        const val TAG_DATA_IF = Constants.TAG_DATA_IF //переменные и данные в циклах

        //Имена файлов
        //const val FILE_SEARCH_ITEM = Constants.FILE_SEARCH_ITEM
    }

    private val vm = viewModel

    fun nameTab(view: View, tabLayout: TabLayout, viewPager2: ViewPager2){
        val nameTab = NameTab(vm)
        val listNameTab = nameTab.listName(view.context)
        Log.d(Constants.TAG, "$logNameClass > nameTab: $listNameTab")

        /*val adapter = ViewPager2Adapter(this, nameTab.listFragment)
        binding.fragNewsViewPager2.adapter = adapter*/
        TabLayoutMediator(tabLayout, viewPager2) {
                tab, pos -> tab.text = nameTab.listName(view.context)[pos]
        }.attach()
    }

    //загрузка интеренет страницы
    fun loadWebsiteFragment(newsItem: NewsItem, activity: MainActivity) {
        Log.d(TAG_DEBUG, "$logNameClass >f loadWebsiteFragment === START")
        Log.d(TAG_DATA, "$logNameClass >f loadWebsiteFragment > newsItem: $newsItem")
        //vm.tempWebsiteLink.value = newsItem.link
        vm.tempNewsItemOpenWebsite.value = newsItem
        //vm.url2 = url
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > url: $url")
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > vm.tempUrl: ${vm.tempWebsiteLink.value}")

        if (vm.statusLandscape.value == "true") {
            //загрузка интернет страницы
            //2й вариант - замена одного фрагмента на другой - WebSiteFragment
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.example.tracknews.R.id.frameLayoutSuperMain, WebsiteFragment.newInstance())
                .addToBackStack("main")
                .commit()
        }
        else {
            //bindingWeb.fragWebsiteWebView.loadUrl("https://www.google.ru/")
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.example.tracknews.R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
                .commit()
        }
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
        return false //если поставить true, то скролл перестает работать
    }

    //Подключаем RecyclerView и отображаем данные из SQLite
    @SuppressLint("ClickableViewAccessibility") //для setOnTouchListener
    fun startRecyclerView(rcView: RecyclerView, newsItemAdapter: NewsItemAdapter){
        //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
        rcView.layoutManager = LinearLayoutManager(rcView.context) //проверить
        rcView.adapter = newsItemAdapter

        //Отслеживаем движение по экрану, чтобы скрывать форму поиска
        rcView.setOnTouchListener { _, event -> onTouch(event)}
    }
}

//BACKUP
/*private fun onTouchZapas(event: MotionEvent): Boolean {
        var x = event.x//event.getX()
        var y = event.y//event.getY()

        var sDown = ""
        var sMove = ""
        var sUp = ""

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                sDown = "Down: $x,$y"
                sMove = ""
                sUp = ""
                if (vm.statusSearchMutable.value == true.toString()) {
                    vm.statusSearchMutable.value = false.toString()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (vm.statusSearchMutable.value == true.toString()) {
                    vm.statusSearchMutable.value = false.toString()
                }
                //sMove = "Move: $x,$y";
            }
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_CANCEL -> {
                sMove = "";
                sUp = "Up: $x,$y";
            }
        }
        //binding.testFragNewsSavedTextView.text = sDown + "\n" + sMove + "\n" + sUp
        return true
    }*/