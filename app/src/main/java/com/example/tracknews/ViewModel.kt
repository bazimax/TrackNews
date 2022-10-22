package com.example.tracknews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemWork
import com.example.tracknews.classes.SearchItem
import com.example.tracknews.classes.SearchItemWorker
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager

open class ViewModel : ViewModel() {

    /*init {
        Log.d("TAG1", "ViewModel created")
    }*/
    //Список новостей (NewsItem)
    val newsItemArrayAll: MutableLiveData<ArrayList<NewsItem>> by lazy { //Список новостей (NewsItem) - "всех"
        MutableLiveData<ArrayList<NewsItem>>()
    }
    val newsItemArrayDay: MutableLiveData<ArrayList<NewsItem>> by lazy { //Список новостей (NewsItem) - "за сегодня"
        MutableLiveData<ArrayList<NewsItem>>()
    }
    val newsItemArrayMonth: MutableLiveData<ArrayList<NewsItem>> by lazy { //Список новостей (NewsItem) - "за месяц"
        MutableLiveData<ArrayList<NewsItem>>()
    }
    val newsItemArraySaved: MutableLiveData<ArrayList<NewsItem>> by lazy { //Список новостей (NewsItem) - "сохраненные"
        MutableLiveData<ArrayList<NewsItem>>()
    }

    //список "сохраненных поисков" (SearchItem) для постоянного отслеживания (подписка на определенные новости)
    val searchItemList: MutableLiveData<List<SearchItem>> by lazy {
        MutableLiveData<List<SearchItem>>()
    }

    /*//список сохраненных поисков для записи
    val searchItemArrayTemp: MutableLiveData<Array<String>> by lazy {
        MutableLiveData<Array<String>>()
    }

    //временные данные из яндекса?
    val newsItemTempYa: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }*/
    //временные данные после парсинга для записи в БД
    val newsItemTempArrayInBd: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }
    //временные данные после парсинга
    val newsItemTempArray: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }

    /*//временные данные?
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
    }*/

    //Отслеживаем поворот экрана
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
    var sizeFAButton: Int = -99 //размер кнопки, как точка отсчета для подгонки интерфейса
    var statusChannelNotification = false
    var statusSavedSearchesView: Boolean = false //статус кнопки сохранённых поисков (показывает скрыт ли список "сохранённых поисков")
    var statusUpdateWorker: Boolean = false //есть ли обновления для SQLite от Worker
    var statusSearchButton: Boolean = false //

    val webSiteData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    //отслеживаем элемент поиска (скрыт или нет)
    val statusSearchMutable: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    //список searchItem на удаление
    val searchItemDeleteArrayList = MutableLiveData<ArrayList<String>>()



    var searchItemActive = MutableLiveData("") //активный "поисковой запрос"(или сохраненный поиск) для которого выводятся все найденные новости. Если запрос пустой - значит еще нет "поисковых запросов"
    var searchItemDeleteCount = MutableLiveData<Int>() //счетчик элементов searchItem на удаление (если счетчик = 0, то кнопки для удаления скрываются)
    var tempWebsiteLink = MutableLiveData<String>() //ссылка на новость
    //var newsItemDeleted = MutableLiveData<String>()
    //var newsItemWorkId = MutableLiveData<Int>()
    //var newsItemWork = MutableLiveData<String>()
    var newsItemDeleted2 = MutableLiveData("Moroz")
    var testParserSitesString = MutableLiveData("Site")
    var testSiteString = MutableLiveData("")

    var newsItemUpdateItem = MutableLiveData<NewsItem>()

    init {
        //запускать init в activity или fragment не надо?

        searchItemDeleteArrayList.value = ArrayList() //пустой список searchItem на удаление
        searchItemDeleteCount.value = 0
        tempWebsiteLink.value = "-1"
        //newsItemDeleted.value = "false"
        newsItemUpdateItem.value?.id = 0
        testSiteString.value = ""
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
