package com.example.tracknews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemWork
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager

open class ViewModel : ViewModel() {

    /*init {
        Log.d("TAG1", "ViewModel created")
    }*/
    //данные?
    val newsItemArray: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }

    //временные данные из яндекса?
    val newsItemTempYa: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }

    //временные данные?
    val newsItemTemp: MutableLiveData<NewsItem> by lazy {
        MutableLiveData<NewsItem>()
    }

    //изменение элемента в SQLite
    val newsItemChanged: MutableLiveData<NewsItem> by lazy {
        MutableLiveData<NewsItem>()
    }

    //данные из гугла
    val newsItemTempGoo: MutableLiveData<NewsItem> by lazy {
        MutableLiveData<NewsItem>()
    }

    val testViewToSQLite: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    //поворот экрана
    val statusLandscape: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messagePortrait: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messageLand: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messageLoadWebsite: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val url: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    var url2 = "https://yandex.ru/"
    var nState = 0
    var counterB: Int = 0
    var sizeFAButton: Int = -99
    var statusSavedSearchesView: Boolean = false //статус кнопки сохранённых поисков
    var statusSearchButton: Boolean = false //
    val webSiteData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    //отслеживаем элемент поиска (скрыт или нет)
    val statusSearchMutable: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var tempWebsiteLink = MutableLiveData<String>()
    var newsItemDeleted = MutableLiveData<String>()
    //var newsItemWorkId = MutableLiveData<Int>()
    //var newsItemWork = MutableLiveData<String>()
    var newsItemDeleted2 = MutableLiveData("Moroz")
    var testParserSitesString = MutableLiveData("Site")

    var newsItemUpdateItem = MutableLiveData<NewsItem>()

    init {
        //запускать init в activity или fragment не надо?
        tempWebsiteLink.value = "-1"
        newsItemDeleted.value = "false"
        newsItemUpdateItem.value?.id = 0
        //newsItemWork.value = "false"
        //newsItemWorkId.value = -1
    }

    /*
    // надо протестировать
    var count1 = 0
    var count2 = MutableLiveData<Int>()
    init {
        //запускать init в activity или fragment не надо?
        count2.value = 0
    }
    fun updateCount(){
        ++count1
        count2.value = (count2.value)?.plus(1)
    }*/
    /*class SavedStateViewModel(private val state: SavedStateHandle) : ViewModel() {
        //сохранение состояния
    }*/

}
