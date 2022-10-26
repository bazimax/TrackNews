package com.example.tracknews.classes

import android.content.Context
import android.icu.text.RelativeDateTimeFormatter
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.MainActivity
import com.example.tracknews.News.NewsAllFragment
import com.example.tracknews.News.NewsSavedFragment
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.News.NewsWeekFragment
import com.example.tracknews.R
import com.example.tracknews.ViewModel
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites
import com.example.tracknews.services.WorkerFindNewsFun
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//Основная логика приложения завязанная на ViewModel
class ViewModelFunctions(viewModel: ViewModel) {
    private val logNameClass = "ViewModelFunctions" //для логов
    //private val logFunName = Throwable().stackTrace[0].methodName//object{}.javaClass.enclosingMethod?.name //для логов

    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, активити и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
        const val TAG_DATA_IF = Constants.TAG_DATA_IF //переменные и данные в циклах

        //Имена файлов
        const val FILE_SEARCH_ITEM = Constants.FILE_SEARCH_ITEM
    }

    private val vm = viewModel
    private var parserSites = ParserSites() //парсинг

    //отслеживание изменений в ViewModel
    fun observeVM(mainDbManager: MainDbManager, searchItemAdapter: SearchItemAdapter, owner: LifecycleOwner){
        //подключаем observe
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite (появились новые "новости" для записи в БД)
        vm.newsItemTempArrayInBd.observe(owner){
            Log.d(TAG_DEBUG, "$logNameClass >f newsItemTempArrayInBd.OBSERVE === START")
            Log.d(TAG_DEBUG, "$logNameClass >f newsItemTempArrayInBd.OBSERVE // следим за изменениями в DataModel(ViewModel) и передаем их в SQLite (появились новые <новости> для записи в БД)")
            viewModelToSQLite(mainDbManager)
        }

        //изменяем элементы БД
        vm.newsItemUpdateItem.observe(owner) {
            Log.d(TAG_DEBUG, "$logNameClass >f newsItemUpdateItem.OBSERVE === START")
            Log.d(TAG_DEBUG, "$logNameClass >f newsItemUpdateItem.OBSERVE // изменяем элементы БД")
            updateElementOfSQLite(mainDbManager)
        }

        //отслеживаем изменения активного searchItem и обновляем отображаемые новости для конкретного "поискового запроса"
        vm.searchItemActive.observe(owner) {
            Log.d(TAG_DEBUG, "$logNameClass >f searchItemActive.OBSERVE === START")
            Log.d(TAG_DEBUG, "$logNameClass >f searchItemActive.OBSERVE // отслеживаем изменения активного searchItem и обновляем отображаемые новости для конкретного <поискового запроса>")
            vm.searchItemActive.value?.let { it1 -> loadSQLiteToViewModelActive(mainDbManager, it1) }
            searchItemAdapter.notifyDataSetChanged()
        }

        //если изменился общий список новостей сортируем остальные списки по дате (за сегодня, за месяц и тд)
        vm.newsItemArrayAll.observe(owner) {
            Log.d(TAG_DEBUG, "$logNameClass >f newsItemArrayAll.OBSERVE === START")
            Log.d(TAG_DEBUG, "$logNameClass >f newsItemArrayAll.OBSERVE // если изменился общий список новостей сортируем остальные списки по дате (за сегодня, за месяц и тд)")
            sortNewsItemsByDate()
        }
    }

    //ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей
    private fun loadSQLiteToViewModelActive(mainDbManager: MainDbManager, search: String){
        Log.d(TAG_DEBUG, "$logNameClass >f loadSQLiteToViewModelActive === START")
        Log.d(TAG_DEBUG, "$logNameClass >f loadSQLiteToViewModelActive // Ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей")

        vm.newsItemArrayAll.value = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, search) //search
        //val counter = vm.newsItemArrayAll.value!!.size
        //Log.d(TAG_DATA, "ViewModelFunctions >f loadSQLiteToViewModel > vm.newsItemArray > Size: ${vm.newsItemArray.value!!.size}")
        //Log.d(TAG_DATA, "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")

        Log.d(TAG_DEBUG, "$logNameClass >f loadSQLiteToViewModelActive ----- END")
    }

    //изменяем элементы БД
    private fun updateElementOfSQLite(mainDbManager: MainDbManager){
        Log.d(TAG_DEBUG, "$logNameClass >f updateElementOfSQLite === START")
        Log.d(TAG_DEBUG, "$logNameClass >f updateElementOfSQLite // изменяем элементы БД")
        //обновляем статус у новости (сохранено или нет) ////удалем элемент/строку из Базы Данных
        Log.d(TAG_DATA, "$logNameClass >f updateElementOfSQLite > vm.newsItemUpdateItem.value: ${vm.newsItemUpdateItem.value}")
        val statusSaved = vm.newsItemUpdateItem.value?.statusSaved
        val id = vm.newsItemUpdateItem.value?.id
        val link = vm.newsItemUpdateItem.value?.link
        if (statusSaved != null) {
            if (link != null) {
                mainDbManager.updateDbElementStatusSaved(statusSaved, link)
                //mainDbManager.deleteDbElement(link, "link")
            }
        }

        //читаем Базу Данных учитывая Активный "поиск"
        vm.searchItemActive.value?.let { it1 -> loadSQLiteToViewModelActive(mainDbManager, it1) } //reload
        vm.searchItemActive.value = vm.searchItemActive.value //обновляем vm чтобы NewsFragment подхвватил изменения
        Log.d(TAG_DEBUG, "$logNameClass >f updateElementOfSQLite ----- END")
    }

    //Ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей
    private fun loadNewsItemsByActive(search: String, mainDbManager: MainDbManager){
        vm.newsItemArrayAll.value = vm.searchItemActive.value?.let { it1 ->
            mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, it1)
        }
        vm.newsItemArrayAll.value = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, search)
    }

    //сортируем общий список элементов по дате для разных разделов (новости за сегодня, за месяц и тд)
    private fun sortNewsItemsByDate(){
        //сортируем по дате, сохраненным и тд
        val newsItemArrayDay = ArrayList<NewsItem>() //готовим список newsItem - "за сегодня"
        val newsItemArrayMonth = ArrayList<NewsItem>() //готовим список newsItem - "за месяц"
        val newsItemArraySavedSearch = ArrayList<NewsItem>() //готовим список newsItem - "сохраненные"

        //добавляем в соответствующие списки
        vm.newsItemArrayAll.value?.forEach {
            Log.d(TAG_DATA_IF, "$logNameClass >f sortNewsItemsByDate > newsItemArrayAll.forEach > it.date: ${it.date}")
            Log.d(TAG_DATA_IF, "$logNameClass >f sortNewsItemsByDate > newsItemArrayAll.forEach > it.statusSaved: ${it.statusSaved}")

            //проверка минимальной версии. LocalDate и DateTimeFormatter работают с sdk26 и выше
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){}

            val dateNews = LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            val dateNow = LocalDate.now()

            //если совпадает месяц
            if (dateNews.month == dateNow.month) {
                newsItemArrayMonth.add(it)

                //если совпадает и день
                if (dateNews == dateNow) {
                    newsItemArrayDay.add(it)
                }
            }

            //если помечен как сохраненный
            if (it.statusSaved == "true") {
                newsItemArraySavedSearch.add(it)
            }
        }
        vm.newsItemArrayDay.value = newsItemArrayDay
        vm.newsItemArrayMonth.value = newsItemArrayMonth
        vm.newsItemArraySaved.value = newsItemArraySavedSearch
    }

    //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
    private fun viewModelToSQLite(mainDbManager: MainDbManager){
        Log.d(TAG_DEBUG, "$logNameClass >f viewModelToSQLite === START")
        //Получили новые новости

        //полученные временные данные отправляем в ViewModel для последующей записи в БД
        //Log.d(TAG_DEBUG, "$logNameClass >f viewModelToSQLite > newsItemTempYa.OBSERVE === START")
        if (vm.newsItemTempArrayInBd.value != null) {
            vm.newsItemTempArrayInBd.value!!.forEach {
                //записываем найденные новости в БД если они не совпадают со старыми
                val findSameNewsItem = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, it.link)
                //если совпадений не найдено -> новость новая\уникальная -> записываем ее в БД
                if (findSameNewsItem.size == 0) {
                    val search = it.search
                    val img = it.img
                    val date = it.date
                    val title = it.title
                    val content = it.content
                    val link = it.link
                    val statusSaved = it.statusSaved
                    mainDbManager.insertToDb(search ,img, date, title, content, link, statusSaved)
                }
            }
        }
        Log.d(TAG_DEBUG, "$logNameClass >f viewModelToSQLite ----- END")
    }

    //поиск новостей через строку поиска ("НЕсохраненный поиск"(запрос/подписк))
    fun findNews(search: String, context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f findNews === START")
        Log.d(TAG_DEBUG, "$logNameClass >f findNews // поиск новостей через строку поиска")
        //читаем сохраненный список SearchItem
        val searchItemArrayList = readSearchItemArrayList(context)

        //если строка поиска не пустая. Иначе ничего не делать
        if (search != "") {
            //проверка интернета //??
            //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()

            //запускаем парсинг новостных сайтов/сайта
            val resultParse = parserSites.parse(search, context)
            Log.d(TAG_DATA_BIG, "$logNameClass >f findNews > resultParse:\n" +
                    "-statusEthernet: ${resultParse.statusEthernet}\n" +
                    "- list: ${resultParse.list}")

            if (resultParse.statusEthernet == false.toString()) {
                //если парсинг не удался
                Log.d(TAG, "$logNameClass >f findNews > парсинг не удался")
                val messageLoadWebsite = context.resources.getString(com.example.tracknews.R.string.loadWebsiteFail)
                Toast.makeText(context, messageLoadWebsite, Toast.LENGTH_SHORT).show()
            }
            else {
                //если парсинг OK
                Log.d(TAG, "$logNameClass >f findNews > парсинг OK")
                //все старые элементы делаем не активными
                //??
                searchItemArrayList.list.forEach {
                    it.searchItem.active = false
                }

                //проверка "нового поиска" на совпадение с ранее "сохранеными поисками" в searchItemArrayList
                //если совпадения есть, то меняем активный searchItem на этот и обновляем БД. Иначе показываем все новости без записи в БД
                if (findSameSearchItem(search)) {
                    Log.d(TAG_DATA_IF, "$logNameClass >f findNews > IF")
                    //полученные новости отправляем в ViewModel для последующей проверки и записи в БД
                    vm.newsItemTempArrayInBd.value = null
                    vm.newsItemTempArrayInBd.value = resultParse.list
                    Log.d(TAG_DATA_IF, "$logNameClass >f findNews > vm.newsItemTempArrayInBd.value: ${vm.newsItemTempArrayInBd.value}")

                    //меняем активный searchItem на этот.
                    searchItemArrayList.list.forEach {
                        if (it.searchItem.search == search) it.searchItem.active = true
                    }
                    //Записываем обновленный список "сохраненных поисков" обратно в JSON
                    writeSearchItemArrayList(searchItemArrayList, context)
                    //обновляем vm
                    vm.searchItemActive.value = search
                } else {
                    Log.d(TAG_DATA_IF, "$logNameClass >f findNews > ELSE")
                    //показываем все новости без записи в БД
                    vm.newsItemArrayAll.value = null
                    vm.newsItemArrayAll.value = resultParse.list
                    Log.d(TAG_DATA_IF, "$logNameClass >f findNews > vm.newsItemArrayAll.value: ${vm.newsItemArrayAll.value}")
                }
            }
        }
    }

    //сохраняем поисковой запрос
    fun saveSearch(search: String, context: Context) {
        Log.d(TAG_DEBUG, "$logNameClass >f saveSearch === START")
        Log.d(TAG_DEBUG, "$logNameClass >f saveSearch // сохраняем поисковой запрос")
        //если запрос не пустой
        if (search != "") {
            //читаем сохраненный список SearchItem
            val searchItemArrayList = readSearchItemArrayList(context)

            var checkSameSearchItem = false

            //все старые элементы делаем не активными
            searchItemArrayList.list.forEach {
                it.searchItem.active = false

                //проверяем есть ли уже такое имя в списке (был ли он ранее сохранен)
                //сделаем через смену кнопок - UPD: не, лучше кодом, можно успеть нажать
                if(search == it.searchItem.search) checkSameSearchItem = true
            }

            if (!checkSameSearchItem) {
                //создаем новый элемент, добавляем в список и делаем активным
                val searchItemWorker = SearchItemWorker(SearchItem(search, 0, 0, true))
                searchItemArrayList.list.add(searchItemWorker)
                //??
                /*if (searchItemArrayList.list.size != 0) {
                    searchItemArrayList.list.add(searchItemWorker)
                }*/
                Log.d(TAG_DATA, "$logNameClass >f saveSearch > ADD > searchItemArrayList: $searchItemArrayList")

                //проверяем newsItemArrayAll - если там есть новости с тем же тегом записываем их в бд
                //например - сначала нажали на поиск новостей, и только потом "сохранить поиск"
                val newsItemArrayAll = vm.newsItemArrayAll.value
                if ((newsItemArrayAll != null) && (newsItemArrayAll.size != 0)) {
                    if (newsItemArrayAll[0].search == search) {
                        vm.newsItemTempArrayInBd.value = null
                        vm.newsItemTempArrayInBd.value = vm.newsItemArrayAll.value
                    }
                }

                //Записываем обновленный список "сохраненных поисков" обратно в JSON
                writeSearchItemArrayList(searchItemArrayList, context)

                //сортируем список SearchItem в Recycler View
                sortSearchItemArrayList(context)

                //обновляем список SearchItem в Recycler View
                readSearchItemListToRcView(context)

                //сообщение с подверждением
                val messageLoadWebsite = context.resources.getString(com.example.tracknews.R.string.searchItemSave)
                Toast.makeText(context, messageLoadWebsite, Toast.LENGTH_SHORT).show()

                //??
                //перезапуск worker(задачи) на поиск новостей
                WorkerFindNewsFun().workerFindNewsFirst(context)
            }
        }
        Log.d(TAG_DEBUG, "$logNameClass >f saveSearch ----- END")
    }

    //обновляем список SearchItem в Recycler View
    fun readSearchItemListToRcView(context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f readSearchItemListToRcView === START")
        Log.d(TAG_DEBUG, "$logNameClass >f readSearchItemListToRcView // обновляем список SearchItem в Recycler View")
        //читаем сохраненный список SearchItem
        val searchItemArrayList = readSearchItemArrayList(context)
        //переделываем под нужный ArrayList
        val searchItemList = ArrayList<SearchItem>()
        searchItemArrayList.list.forEach {
            searchItemList.add(it.searchItem)
        }
        vm.searchItemList.value = searchItemList
        Log.d(TAG_DATA, "$logNameClass >f readSearchItemListToRcView > vm.searchItemList.value: ${vm.searchItemList.value}")

        Log.d(TAG_DEBUG, "$logNameClass >f readSearchItemListToRcView ----- END")
    }

    //в процессе пеереключения между "сохраненными поисками" мы не сохраняем это в JSON. Поэтому при onPause->onDestroy мы можем сохранить
    //сохраняем активный SearchItem на время паузы приложения
    fun saveSearchItemActive(context: Context){
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)
        //Log.d(TAG_DATA, "ViewModelFunctions >f saveSearchItemActive > forEach > active: ${vm.searchItemActive.value}")
        searchItemArrayList.list.forEach {
            //Log.d(TAG_DATA, "ViewModelFunctions >f saveSearchItemActive > forEach > it: ${it.searchItem.search}, ${it.searchItem.active}")
            it.searchItem.active = it.searchItem.search == vm.searchItemActive.value
        }

        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)
    }

    //сбрасываем активный SearchItem на позицую 0
    fun resetSearchItemActive(context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f resetSearchItemActive === START")
        Log.d(TAG_DEBUG, "$logNameClass >f resetSearchItemActive // сбрасываем активный SearchItem на позицую 0")
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)

        if (searchItemArrayList.list.size != 0) {
            //обнуляем все значения кроме 0
            searchItemArrayList.list.forEach {
                it.searchItem.active = false
            }
            searchItemArrayList.list[0].searchItem.active = true
            vm.searchItemActive.value = searchItemArrayList.list[0].searchItem.search
        }
        else {
            vm.searchItemActive.value = ""
        }
        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)
        Log.d(TAG_DEBUG, "$logNameClass >f resetSearchItemActive ----- END")
    }

    //выбираем активный SearchItem если есть в
    fun selectSearchItemActive(context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f selectSearchItemActive === START")
        Log.d(TAG_DEBUG, "$logNameClass >f selectSearchItemActive // выбираем активный SearchItem")
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)

        //если список не пустой
        if (searchItemArrayList.list.size != 0) {
            //проверяем есть ли активный "сохраненный поиск"
            var checkActiveSearchItem = false
            //если есть записываем в VM
            searchItemArrayList.list.forEach {
                //Log.d(TAG_DATA, "$logNameClass >f selectSearchItemActive > forEach > it: ${it.searchItem.search}, ${it.searchItem.active}")
                if (it.searchItem.active) {
                    //Log.d(TAG_DATA, "$logNameClass >f selectSearchItemActive > forEach > IF > it: ${it.searchItem.search}, ${it.searchItem.active}")
                    //обновляем VM
                    vm.searchItemActive.value = it.searchItem.search
                    checkActiveSearchItem = true
                }
                //Log.d(TAG_DATA, "$logNameClass >f selectSearchItemActive > forEach > active: ${vm.searchItemActive.value}")
            }
            //если нет, то перезагрузка списка
            if (!checkActiveSearchItem) {
                resetSearchItemActive(context)
            }
        } else vm.searchItemActive.value = ""
        Log.d(TAG_DEBUG, "$logNameClass >f selectSearchItemActive ----- END")
    }

    //сортировка списка searchItem
    private fun sortSearchItemArrayList(context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f sortSearchItemArrayList === START")
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)
        //сортируем
        searchItemArrayList.list.sortBy { it.searchItem.search }
        Log.d(TAG_DATA, "$logNameClass >f sortSearchItemArrayList > searchItemArrayList обновленный: $searchItemArrayList")

        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)

        selectSearchItemActive(context)
        Log.d(TAG_DEBUG, "$logNameClass >f sortSearchItemArrayList ----- END")
    }

    //обновляем счетчики новостей для всех searchItem
    fun updateNewsCountForEachSearchItem(mainDbManager: MainDbManager, context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f updateNewsCountForEachSearchItem === START")
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)

        //для каждого searchItem считаем количество новстей
        searchItemArrayList.list.forEach {
            it.searchItem.counterAllNews = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, it.searchItem.search).size
        }

        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)
        Log.d(TAG_DATA, "$logNameClass >f updateNewsCountForEachSearchItem > searchItemArrayList: $searchItemArrayList")
        Log.d(TAG_DEBUG, "$logNameClass >f updateNewsCountForEachSearchItem ----- END")
    }

    //обрабатываем нажатия на searchItems >
    //при клике на элемент searchItem в recycler view -> он становится активным -> из БД загружаются все новости с этим именем
    fun clickOnSearchItem(searchItem: SearchItem, context: Context) {
        Log.d(TAG_DEBUG, "$logNameClass >f clickOnSearchItem === START")
        Log.d(TAG_DEBUG, "$logNameClass >f clickOnSearchItem // кликнули на элемент searchItem -> он становится активным -> из БД загружаются все новости с этим именем")
        Log.d(TAG_DATA, "$logNameClass >f clickOnSearchItem > имя элемента: ${searchItem.search}")
        Log.d(TAG_DATA, "$logNameClass >f clickOnSearchItem > searchItem: $searchItem")

        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)
        //Log.d(TAG_DEBUG, "$logNameClass >f clickOnSearchItem > searchItemArrayList: ${searchItemArrayList.list}")

        //узнаем индекс элемента
        val index = searchItemArrayList.list.indexOfFirst { it.searchItem.search == searchItem.search }

        //сбрасываем счетчик новых новостей у нужного элемента
        searchItemArrayList.list[index].searchItem.counterNewNews = 0

        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)


        //val index = searchItemArrayList.list.indexOfFirst { it.searchItem.search == searchItem.search }

        //выделяем элемент, меняя его состояние (active)
        vm.searchItemList.value?.forEach {
            it.active = false
            if (it.search == searchItem.search) {
                it.active = true
                it.counterNewNews = 0
            }
            //Log.d(TAG_DATA, "MainActivity >f clickOnSearchItem > it Active: ${it.search}")
            //Log.d(TAG_DATA, "MainActivity >f clickOnSearchItem > it: ${it.active}")
        }
        //обновляем searchItemList, чтобы LiveData подхватила изменения и отобразила измененные кнопки
        vm.searchItemList.value = vm.searchItemList.value

        vm.searchItemActive.value = searchItem.search
        Log.d(TAG_DEBUG, "$logNameClass >f clickOnSearchItem ----- END")
    }

    //длинное нажатие для выделения этого searchItem и его удаления
    fun selectSearchItem(searchItem: SearchItem) {
        Log.d(TAG_DEBUG, "$logNameClass >f selectSearchItem === START")
        Log.d(TAG_DEBUG, "$logNameClass >f selectSearchItem // выделили searchItem для его последующего удаления")
        var a = vm.searchItemDeleteCount.value
        if (a != null) {
            a++
        }
        vm.searchItemDeleteCount.value = a

        vm.searchItemDeleteArrayList.value?.add(searchItem.search)

        //Log.d(TAG_DATA, "Main Activity >f selectSearchItem >  Count: ${vm.searchItemDeleteCount.value}")
        Log.d(TAG_DATA, "$logNameClass >f selectSearchItem >  Delete: ${vm.searchItemDeleteArrayList.value}")
        Log.d(TAG_DEBUG, "$logNameClass >f selectSearchItem ----- END")
    }

    //длинное нажатие на выделенном элементе для отмены выделения
    fun unSelectSearchItem(searchItem: SearchItem) {
        Log.d(TAG_DEBUG, "$logNameClass >f unSelectSearchItem === START")
        Log.d(TAG_DEBUG, "$logNameClass >f unSelectSearchItem // отмена выделения")
        var a = vm.searchItemDeleteCount.value
        if (a != null) {
            a--
        }
        vm.searchItemDeleteCount.value = a

        //val idUnselect = vm.searchItemDeleteArrayList.value?.indexOf(searchItem.search)
        vm.searchItemDeleteArrayList.value?.remove(searchItem.search)

        //Log.d(TAG_DATA, "Main Activity >f unSelectSearchItem >  Count: ${vm.searchItemDeleteCount.value}")
        Log.d(TAG_DATA, "$logNameClass >f unSelectSearchItem >  Delete: ${vm.searchItemDeleteArrayList.value}")
        Log.d(TAG_DEBUG, "$logNameClass >f unSelectSearchItem ----- END")
    }

    //удалить выбранные "сохраненные поиски" и удалить из БД все новости с ними связанные
    fun deleteSelectSearchItemAndNews(mainDbManager: MainDbManager, searchItemAdapter: SearchItemAdapter, context: Context){
        Log.d(TAG_DEBUG, "$logNameClass >f deleteSelectSearchItemAndNews === START")
        Log.d(TAG_DEBUG, "$logNameClass >f deleteSelectSearchItemAndNews // удаляем выбранные <сохраненные поиски> и удаляем из БД все новости с ними связанные")
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayListOld = readSearchItemArrayList(context)
        val dataListWorker = ArrayList<SearchItemWorker>()
        //val searchItemArrayListDelete = vm.searchItemDeleteArrayList.value
        var checkDeleteActiveSearchItem = false

        Log.d(TAG_DATA, "$logNameClass >f deleteSelectSearchItemAndNews > начало обработки\n" +
                "-- searchItemArrayListOld: $searchItemArrayListOld\n" +
                "-- dataListWorker: $dataListWorker\n" +
                "-- searchItemArrayListDelete: ${vm.searchItemDeleteArrayList.value}")
        searchItemArrayListOld.list.forEach { itSearch ->
            Log.d(TAG_DATA_IF, "$logNameClass >f deleteSelectSearchItemAndNews > forEach > itSearch: ${itSearch.searchItem.search}")
            //проверяем совпадение
            var checkSame = false
            var count = 0
            vm.searchItemDeleteArrayList.value?.forEach { itSearchDelete ->
                count++
                Log.d(TAG_DATA_IF, "$logNameClass >f deleteSelectSearchItemAndNews $count > forEach > itSearchDelete: $itSearchDelete")
                if (itSearch.searchItem.search == itSearchDelete) {
                    //если совпадение есть
                    checkSame = true
                    Log.d(TAG_DATA_IF, "$logNameClass >f deleteSelectSearchItemAndNews> IF > ${searchItemArrayListOld.list.indexOf(itSearch)}")
                }
            }
            //если совпадений нет -> добавляем элемент в новый список "сохраненных поисков"
            if (!checkSame) {
                Log.d(TAG_DATA_IF, "$logNameClass >f deleteSelectSearchItemAndNews > IF NEW> ${searchItemArrayListOld.list.indexOf(itSearch)}")
                dataListWorker.add(itSearch)
            }
            else {
                //удаляем элемент/строку из БД
                mainDbManager.deleteDbElement(itSearch.searchItem.search)

                if (itSearch.searchItem.active) {
                    checkDeleteActiveSearchItem = true
                }
            }
        }
        val searchItemArrayListNew = SearchItemArrayList(dataListWorker)

        Log.d(TAG_DATA, "$logNameClass >f deleteSelectSearchItemAndNews > обработка завершена\n" +
                "- searchItemArrayListNew: $searchItemArrayListNew\n" +
                "- dataListWorker: $dataListWorker\n" +
                "- searchItemArrayListDelete: ${vm.searchItemDeleteArrayList.value}")

        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayListNew, context)

        //если удалили активный "сохраненный поиск", то сбрасываем
        if (checkDeleteActiveSearchItem) {
            resetSearchItemActive(context)
        }

        //обновляем список "сохраненный поиск" во ViewModel
        readSearchItemListToRcView(context)

        //очищаем список на удаление
        vm.searchItemDeleteCount.value = 0 //скрываем кнопки
        vm.searchItemDeleteArrayList.value = ArrayList() //очищаем список на удаление
        searchItemAdapter.notifyDataSetChanged() //обновляем RcView с SearchItem

        //перезапуск worker(задачи) на поиск новостей
        WorkerFindNewsFun().workerFindNewsFirst(context)
        Log.d(TAG_DEBUG, "$logNameClass >f deleteSelectSearchItemAndNews ----- END")
    }

    //отменить выделение "сохраненных поисков"
    fun cancelSelectSearchItem(searchItemAdapter: SearchItemAdapter){
        vm.searchItemDeleteCount.value = 0 //скрываем кнопки
        vm.searchItemDeleteArrayList.value = ArrayList() //очищаем список на удаление
        searchItemAdapter.notifyDataSetChanged() //обновляем RcView с SearchItem
        Log.d(TAG_DEBUG, "$logNameClass >f cancelSelectSearchItem === START / END")
        Log.d(TAG_DEBUG, "$logNameClass >f cancelSelectSearchItem // отменили выделение <сохраненных поисков>")
    }
    //обрабатываем нажатия на searchItems ^

    //загружаем список "сохраненных поисков" (SearchItem) <- читаем данные из JSON
    private fun readSearchItemArrayList(context: Context): SearchItemArrayList {
        Log.d(TAG_DEBUG, "$logNameClass >f readSearchItemArrayList === START")
        Log.d(TAG_DEBUG, "$logNameClass >f readSearchItemArrayList // загружаем список <сохраненных поисков> (SearchItem) <- читаем данные из JSON")
        val searchItemArrayList = FilesWorker().readJSONSearchItemArrayList(FILE_SEARCH_ITEM, context)
        Log.d(TAG_DATA, "$logNameClass >f readSearchItemArrayList > searchItemArrayList: $searchItemArrayList")
        Log.d(TAG_DEBUG, "$logNameClass >f readSearchItemArrayList ----- END")
        return searchItemArrayList
    }

    //записываем обновленный список "сохраненных поисков" обратно в JSON
    private fun writeSearchItemArrayList(searchItemArrayList: SearchItemArrayList, context: Context){
        FilesWorker().writeJSON(searchItemArrayList, FILE_SEARCH_ITEM, context)
        Log.d(TAG_DEBUG, "$logNameClass >f writeSearchItemArrayList === START / END")
        Log.d(TAG_DEBUG, "$logNameClass >f writeSearchItemArrayList // записываем обновленный список <сохраненных поисков> обратно в JSON")
    }

    //проверяем есть ли совпадения с имеющимеся <сохраненными поисками>
    fun findSameSearchItem(search: String): Boolean{
        var checkSame = false
        val searchItemList = vm.searchItemList.value
        if ((searchItemList != null) && searchItemList.isNotEmpty()) {
            searchItemList.forEach {
                if (it.search == search) checkSame = true
            }
        }
        Log.d(TAG_DEBUG, "$logNameClass >f findSameSearchItem === START / END")
        Log.d(TAG_DEBUG, "$logNameClass >f findSameSearchItem // проверяем есть ли совпадения с имеющимеся <сохраненными поисками>")
        Log.d(TAG_DATA, "$logNameClass >f findSameSearchItem > searchItemArrayList: $checkSame")
        return checkSame
    }

    //тестирование получения имен функции и класса
    fun testNameThisFun(){
        val name = object{}.javaClass.enclosingMethod?.name
        Log.d(TAG_DATA, "$logNameClass >f testNameThisFun >  nameFun: $name")
        //Log.d(TAG_DATA, "$logNameClass >f testNameThisFun >  nameFun2: ${logFunName}")
    }
}

class NameTab(viewModel: ViewModel, ) {

    val listFragment: List<Fragment> = listOf(
        NewsTodayFragment.newInstance(),
        NewsWeekFragment.newInstance(),
        NewsAllFragment.newInstance(),
        NewsSavedFragment.newInstance()
    )
    private val vm = viewModel

    fun listName(context: Context): List<String> {
        var counterDay = ""
        var counterMonth = ""
        var counterAll = ""
        var counterSaved = ""

        if (vm.newsItemArrayDay.value != null) {
            if (vm.newsItemArrayDay.value!!.size != 0) {
                counterDay = " ${vm.newsItemArrayDay.value!!.size}"
            }
        }
        if (vm.newsItemArrayMonth.value != null) {
            if (vm.newsItemArrayMonth.value!!.size != 0) {
                counterMonth = " ${vm.newsItemArrayMonth.value!!.size}"
            }
        }
        if (vm.newsItemArrayAll.value != null) {
            if (vm.newsItemArrayAll.value!!.size != 0) {
                counterAll = " ${vm.newsItemArrayAll.value!!.size}"
            }
        }
        if (vm.newsItemArraySaved.value != null) {
            if (vm.newsItemArraySaved.value!!.size != 0) {
                counterSaved = " ${vm.newsItemArraySaved.value!!.size}"
            }
        }

        return listOf(
            //getString(R.string.news_today),
            context.getString(R.string.news_today) + counterDay,
            context.getString(R.string.news_week) + counterMonth,
            context.getString(R.string.news_all) + counterAll,
            context.getString(R.string.news_saved) + counterSaved
        )
    }
}

//BACKUP >

/*if (it.searchItem.search == vm.searchItemActive.value) {
    it.searchItem.active = true
}
else {
    it.searchItem.active = false
}*/
//тоже самое ^ что и -> it.searchItem.active = it.searchItem.search == vm.searchItemActive.value

/*vm.newsItemTemp.observe(owner){
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp.OBSERVE ======START")
            val search = vm.newsItemTemp.value?.search.toString()
            val img = vm.newsItemTemp.value?.img.toString()
            val date = vm.newsItemTemp.value?.date.toString()
            val title= vm.newsItemTemp.value?.title.toString()
            val content = vm.newsItemTemp.value?.content.toString()
            val link = vm.newsItemTemp.value?.link.toString()
            val statusSaved = vm.newsItemTemp.value?.statusSaved.toString()
            mainDbManager.insertToDb(search ,img, date, title, content, link, statusSaved)
            loadSQLiteToViewModel(mainDbManager)

            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItem value: ${vm.newsItem.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp value: ${vm.newsItemTemp.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp.OBSERVE ------------END")
        }*/
/*vm.newsItemTempYa.observe(owner){
    Log.d(TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > newsItemTempYa.OBSERVE ======START")
    if (vm.newsItemTempYa.value != null) {
        vm.newsItemTempYa.value!!.forEach {
            val search = it.search
            val img = it.img
            val date = it.date
            val title = it.title
            val content = it.content
            val link = it.link
            val statusSaved = it.statusSaved
            mainDbManager.insertToDb(search ,img, date, title, content, link, statusSaved)
        }
    }
    else mainDbManager.clearAllDataInDb()

    //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItem value: ${vm.newsItem.value}")
    //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTempYa value: ${vm.newsItemTempYa.value}")
    Log.d(TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > newsItemTempYa.OBSERVE ------------END")
    loadSQLiteToViewModel(mainDbManager)
}*/
/*//через SharedPreferences
        val sharedPrefsRcView = getSharedPreferences("init", Context.MODE_PRIVATE)
        var stringItem = sharedPrefsRcView.getString(MainActivity.SEARCH_ITEM, "") //читаем сохраненную ранее строку с searchItem*/
/*
fun getPosition(key: String): Int = searchItemArrayList.list.indexOfFirst { it.searchItem.search == key }
Log.d(TAG_DEBUG, "$logNameClass >f clickOnSearchItem > a: ${getPosition(searchItem.search)}")
val index = searchItemArrayList.list.indexOfFirst { it.searchItem.search == searchItem.search }*/
