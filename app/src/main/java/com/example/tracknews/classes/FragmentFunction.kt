package com.example.tracknews.classes

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
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
        Log.d(TAG_DEBUG, "$logNameClass >f nameTab === START")
        val nameTab = NameTab(vm)
        val listNameTab = nameTab.listName(view.context) //V1
        //val listNameTab = nameTab.tabName(view.context)//V2 - мало полезен
        Log.d(TAG_DATA, "$logNameClass > nameTab: $listNameTab")

        //V1
        TabLayoutMediator(tabLayout, viewPager2) {
                tab, pos -> tab.text = nameTab.listName(view.context)[pos]
        }.attach()
        //V2 - мало полезен
        /*TabLayoutMediator(tabLayout, viewPager2) {
                tab, pos -> tab.text = nameTab.tabName(view.context)[pos]
        }.attach()*/
        Log.d(TAG_DEBUG, "$logNameClass >f nameTab ----- END")
    }

    //загрузка интеренет страницы
    fun loadWebsiteFragment(newsItem: NewsItem, activity: MainActivity) {
        Log.d(TAG_DEBUG, "$logNameClass >f loadWebsiteFragment === START")
        Log.d(TAG_DATA, "$logNameClass >f loadWebsiteFragment > newsItem: $newsItem")

        if (newsItem.link.contains("https://")) {
            vm.tempNewsItemOpenWebsite.value = newsItem
            //Log.d("TAG1", "fragNewsSaved >f loadWebsite > url: $url")

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

    //прогрессбар
    fun progressBar(view: View) {
        //vm.statusProgressBar.observe(owner) { }

        if (vm.statusProgressBar.value == true) {
            view.visibility = View.VISIBLE
        }
        else {
            view.visibility = View.GONE
        }
    }

    //сервисные сообщения
    fun serviceMessage(view: TextView) {
        //vm.serviceMessage.observe(owner) { }

        Log.d(Constants.TAG_DEBUG, "$logNameClass >f serviceMessage >  ${vm.serviceMessage.value}")

        if (vm.serviceMessage.value != "") {
            view.visibility = View.VISIBLE
            view.text = vm.serviceMessage.value
        }
        else {
            view.visibility = View.GONE
        }
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