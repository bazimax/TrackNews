package com.example.tracknews

import android.util.Log
import androidx.fragment.app.Fragment
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

    //временные данные после парсинга для записи в БД
    val newsItemTempArrayInBd: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }
    /*//временные данные после парсинга
    val newsItemTempArray: MutableLiveData<ArrayList<NewsItem>> by lazy {
        MutableLiveData<ArrayList<NewsItem>>()
    }*/

    //Отслеживаем поворот экрана
    val statusLandscape: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    /*val messagePortrait: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val messageLand: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }*/

    //?? Delete
    val messageLoadWebsite: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val url: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var sizeFAButton: Int = -99 //размер кнопки, как точка отсчета для подгонки интерфейса
    //var statusChannelNotification = false
    var statusSavedSearchesView: Boolean = false //статус кнопки сохранённых поисков (показывает скрыт ли список "сохранённых поисков")
    //var statusUpdateWorker: Boolean = false //есть ли обновления для SQLite от Worker
    //var statusSearchButton: Boolean = false //

    /*val webSiteData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }*/

    //отслеживаем элемент поиска (скрыт или нет)
    val statusSearchMutable: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    //список searchItem на удаление
    val searchItemDeleteArrayList = MutableLiveData<ArrayList<String>>()



    var statusInstruction = MutableLiveData("step0") //начальная инструкция
    var statusProgressBar = MutableLiveData(false)
    var serviceMessage = MutableLiveData("")
    var searchItemActive = MutableLiveData("") //активный "поисковой запрос"(или сохраненный поиск) для которого выводятся все найденные новости. Если запрос пустой - значит еще нет "поисковых запросов"
    var searchItemDeleteCount = MutableLiveData<Int>() //счетчик элементов searchItem на удаление (если счетчик = 0, то кнопки для удаления скрываются)
    //var tempWebsiteLink = MutableLiveData<String>() //ссылка на новость (при нажатии на элемент новости)
    var tempNewsItemOpenWebsite = MutableLiveData<NewsItem>() //временный элемент новости
    //var newsItemDeleted = MutableLiveData<String>()
    //var newsItemWorkId = MutableLiveData<Int>()
    //var newsItemWork = MutableLiveData<String>()
    //var newsItemDeleted2 = MutableLiveData("Moroz")
    //var testParserSitesString = MutableLiveData("Site")
    //var testSiteString = MutableLiveData("")

    var newsItemUpdateItem = MutableLiveData<NewsItem>()

    init {
        //запускать init в activity или fragment не надо?

        searchItemDeleteArrayList.value = ArrayList() //пустой список searchItem на удаление
        searchItemDeleteCount.value = 0
        //tempWebsiteLink.value = "-1"
        //newsItemDeleted.value = "false"
        newsItemUpdateItem.value?.id = 0
        //testSiteString.value = ""
        //newsItemWork.value = "false"
        //newsItemWorkId.value = -1
    }

    /*class SavedStateViewModel(private val state: SavedStateHandle) : ViewModel() {
        //сохранение состояния
    }*/

    //МАССИВЫ ПУСТЫЕ
    //val searchItemDeleteArrayList = MutableLiveData<ArrayList<String>>()
    //val list = ArrayList<Fragment>()
    //val dataList = ArrayList<NewsItem>()
    //val dataListWorker = ArrayList<SearchItemWorker>()
    //val searchItemArrayList = SearchItemArrayList(dataListWorker)
    //vm.searchItemDeleteArrayList.value = ArrayList()

}
