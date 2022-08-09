package com.example.tracknews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager

open class ViewModel : ViewModel() {

    /*init {
        Log.d("TAG1", "ViewModel created")
    }*/

    val newsItemArray: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }
    val newsItemTempYa: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }
    val newsItemTemp: MutableLiveData<NewsItem> by lazy {
        MutableLiveData<NewsItem>()
    }
    val newsItemTempGoo: MutableLiveData<NewsItem> by lazy {
        MutableLiveData<NewsItem>()
    }

    val testViewToSQLite: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val statusLandscape: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messagePortrait: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messageLand: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messageFact: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val url: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    var url2 = "https://yandex.ru/"
    var nState = 0
    var counterB: Int = 0
    val webSiteData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    /*class SavedStateViewModel(private val state: SavedStateHandle) : ViewModel() {
        //сохранение состояния
    }*/

}
