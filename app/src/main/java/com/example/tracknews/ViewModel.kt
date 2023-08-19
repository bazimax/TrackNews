package com.example.tracknews

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.SearchItem

open class ViewModel : ViewModel() {

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

    //Отслеживаем поворот экрана
    val statusLandscape: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }


    var sizeFAButton: Int = -99 //размер кнопки, как точка отсчета для подгонки интерфейса
    var statusSavedSearchesView: Boolean = false //статус кнопки сохранённых поисков (показывает скрыт ли список "сохранённых поисков")


    //отслеживаем элемент поиска (скрыт или нет)
    val statusSearchMutable: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    //список searchItem на удаление
    val searchItemDeleteArrayList = MutableLiveData<ArrayList<String>>()
    var statusInstruction = MutableLiveData("step0") //начальная инструкция
    var statusProgressBar = MutableLiveData(false)
    var serviceMessage = MutableLiveData("")

    //Активный "поисковой запрос"(или сохраненный поиск) для которого
    //выводятся все найденные новости. Если запрос пустой - значит еще нет "поисковых запросов"
    var searchItemActive = MutableLiveData("")

    //счетчик элементов searchItem на удаление (если счетчик = 0,
    //то кнопки для удаления скрываются)
    var searchItemDeleteCount = MutableLiveData<Int>()

    var tempNewsItemOpenWebsite = MutableLiveData<NewsItem>() //временный элемент новости
    var newsItemUpdateItem = MutableLiveData<NewsItem>()

    init {

        searchItemDeleteArrayList.value = ArrayList() //пустой список searchItem на удаление
        searchItemDeleteCount.value = 0
        newsItemUpdateItem.value?.id = 0
    }
}
