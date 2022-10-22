package com.example.tracknews.classes

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.MainActivity
import com.example.tracknews.ViewModel
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites
import com.example.tracknews.services.WorkerFindNewsFun
import java.time.LocalDate
import java.time.format.DateTimeFormatter

//Основная логика приложения завязанная на ViewModel
class ViewModelFunctions(viewModel: ViewModel) {
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG

        //Имена файлов
        const val FILE_SEARCH_ITEM = Constants.FILE_SEARCH_ITEM
    }

    private val vm = viewModel
    private var parserSites = ParserSites() //парсинг

    //отслеживание изменений в ViewModel
    fun observeVM(mainDbManager: MainDbManager, owner: LifecycleOwner, context: Context){
        //?? подключаем observe
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
        vm.newsItemTempArrayInBd.observe(owner){ viewModelToSQLite(mainDbManager) }
        //viewModelToSQLite(mainDbManager, owner)

        // загружаем БД во viewModel
        //vm.searchItemActive.value?.let { loadSQLiteToViewModelActive(mainDbManager, it) }

        // отслеживаем и удаляем элементы БД
        vm.newsItemUpdateItem.observe(owner) { updateElementOfSQLite(mainDbManager, owner, context) }
        //updateElementOfSQLite(mainDbManager, owner, context)

        //отслеживаем изменения активного searchItem и обновляем отображаемые новости для конкретного "поискового запроса"
        vm.searchItemActive.observe(owner) { loadNewsItemArraysByDate(mainDbManager, owner) }
        //sortNewsItemArrays(mainDbManager, owner)

        /*vm.searchItemActive.observe(owner) {
            //Ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей
            vm.newsItemArray.value = vm.searchItemActive.value?.let { it1 ->
                mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, it1)
            }
            //??
            //сортируем по дате, сохраненным и тд
            val newsItemArrayDay = ArrayList<NewsItem>() //готовим список newsItem - "за сегодня"
            val newsItemArrayMonth = ArrayList<NewsItem>() //готовим список newsItem - "за месяц"
            val newsItemArraySavedSearch = ArrayList<NewsItem>() //готовим список newsItem - "сохраненные"

            vm.newsItemArray.value?.forEach {
                val dateNews = LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val dateNow = LocalDate.now()
                if (dateNews.month == dateNow.month) {
                    //если совпадает месяц
                    newsItemArrayMonth.add(it)
                    if (dateNews == dateNow) {
                        //если совпадает и день
                        newsItemArrayDay.add(it)
                    }
                }
                if (it.statusSaved == "true") {
                    //если помечен как сохраненный
                    newsItemArraySavedSearch.add(it)
                }
            }
            vm.newsItemArrayDay.value = newsItemArrayDay
            vm.newsItemArrayMonth.value = newsItemArrayMonth
            vm.newsItemArraySavedSearch.value = newsItemArraySavedSearch
        }*/
    }

    //читаем Базу Данных
    /*private fun loadSQLiteToViewModel(mainDbManager: MainDbManager){

        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ======START")

        vm.newsItemArrayAll.value = mainDbManager.readDbData()
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ------------END")
    }*/

    //читаем Базу Данных учитывая Активный "поиск"
    private fun loadSQLiteToViewModelActive(mainDbManager: MainDbManager, search: String){

        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ======START")

        var search1 = search
        search1 = "witcher" //Delete
        vm.newsItemArrayAll.value = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, search1) //search
        val counter = vm.newsItemArrayAll.value!!.size
        //Log.d(TAG, "ViewModelFunctions >f loadSQLiteToViewModel > vm.newsItemArray > Size: ${vm.newsItemArray.value!!.size}")
        //Log.d(TAG, "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ------------END")
    }

    // отслеживаем и удаляем элементы БД
    private fun updateElementOfSQLite(mainDbManager: MainDbManager, owner: LifecycleOwner, context: Context){
        //обновляем статус у новости (сохранено или нет) ////удалем элемент/строку из Базы Данных
        Log.d("TAG1", "ViewModelFunctions >f updateElementOfSQLite > vm.newsItemUpdateItem.OBSERVE > value: ${vm.newsItemUpdateItem.value}")
        val statusSaved = vm.newsItemUpdateItem.value?.statusSaved
        val id = vm.newsItemUpdateItem.value?.id
        if (statusSaved != null) {
            //mainDbManager.deleteDbElement(link, "link")
            if (id != null) {
                mainDbManager.updateDbElementStatusSaved(statusSaved, id)
            }
        }
        Toast.makeText(context, statusSaved, Toast.LENGTH_SHORT).show()
        //loadSQLiteToViewModel(mainDbManager)
        vm.searchItemActive.value?.let { it1 -> loadSQLiteToViewModelActive(mainDbManager, it1) } //reload
        vm.searchItemActive.value = vm.searchItemActive.value
    }

    //сортируем общий список элементов по дате для разных разделов (новости за сегодня, за месяц и тд)
    private fun loadNewsItemArraysByDate(mainDbManager: MainDbManager, owner: LifecycleOwner){
        //Ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей
        vm.newsItemArrayAll.value = vm.searchItemActive.value?.let { it1 ->
            mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, it1)
        }
        //??
        //сортируем по дате, сохраненным и тд
        val newsItemArrayDay = ArrayList<NewsItem>() //готовим список newsItem - "за сегодня"
        val newsItemArrayMonth = ArrayList<NewsItem>() //готовим список newsItem - "за месяц"
        val newsItemArraySavedSearch = ArrayList<NewsItem>() //готовим список newsItem - "сохраненные"

        //добавляем в соответствующие списки
        vm.newsItemArrayAll.value?.forEach {
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
        Log.d(TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite >  ======START")
        //Получили новые новости


        //полученные временные данные отправляем в ViewModel для последующей записи в БД
        Log.d(TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > newsItemTempYa.OBSERVE ======START")
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
        //??
        //else mainDbManager.clearAllDataInDb()

        //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItem value: ${vm.newsItem.value}")
        //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTempYa value: ${vm.newsItemTempYa.value}")
        Log.d(TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > newsItemTempYa.OBSERVE ------------END")
        vm.searchItemActive.value?.let { it1 -> loadSQLiteToViewModelActive(mainDbManager, it1) } //reload
        //loadSQLiteToViewModel(mainDbManager)
        Log.d(TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > ------------END")
    }

    //поиск новостей через строку поиска ("НЕсохраненный поиск"(запрос/подписк))
    fun searchNews(search: String, context: Context){
        //читаем сохраненный список SearchItem
        val searchItemArrayList = readSearchItemArrayList(context)

        //если строка поиска не пустая. Иначе ничего не делать
        if (search != "") {
            //проверка интернета //??
            //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()

            //запускаем парсинг новостных сайтов/сайта
            val resultParse = parserSites.parse(search)


            if (resultParse.statusEthernet == false.toString()) {
                //если парсинг не удался
                val messageLoadWebsite = context.resources.getString(com.example.tracknews.R.string.loadWebsiteFail)
                Toast.makeText(context, messageLoadWebsite, Toast.LENGTH_SHORT).show()
            }
            else {
                //если парсинг OK
                //все старые элементы делаем не активными
                searchItemArrayList.list.forEach {
                    it.searchItem.active = false
                }

                //проверка "нового поиска" на совпадение с ранее "сохранеными поисками" в searchItemArrayList
                //если совпадения есть, то меняем активный searchItem на этот и обновляем БД. Иначе показываем все новости без записи в БД
                searchItemArrayList.list.forEach {
                    if (it.searchItem.search == search) {
                        //меняем активный searchItem на этот. Обновляем vm
                        vm.searchItemActive.value = it.searchItem.search
                        it.searchItem.active = true

                        //Записываем обновленный список "сохраненных поисков" обратно в JSON
                        writeSearchItemArrayList(searchItemArrayList, context)

                        //полученные новости отправляем в ViewModel для последующей проверки и записи в БД
                        vm.newsItemTempArrayInBd.value = null
                        vm.newsItemTempArrayInBd.value = resultParse.list
                    }
                    else {

                        //показываем все новости без записи в БД
                        vm.newsItemArrayAll.value = null
                        vm.newsItemArrayAll.value = resultParse.list

                        //меняем активный searchItem на этот. Обновляем vm
                        vm.searchItemActive.value = it.searchItem.search
                    }
                }



            }


        }
    }

    //сохраняем поисковой запрос
    fun saveSearch(search: String, context: Context) {
        //если запрос не пустой
        if (search != "") {
            //читаем сохраненный список SearchItem
            val searchItemArrayList = readSearchItemArrayList(context)

            //все старые элементы делаем не активными
            searchItemArrayList.list.forEach {
                it.searchItem.active = false
            }

            //создаем новый элемент, добавляем в список и делаем активным
            val searchItemWorker = SearchItemWorker(SearchItem(search, 0, 0, true))
            if (searchItemArrayList.list.size != 0) {
                searchItemArrayList.list.add(searchItemWorker)
            }
            //Записываем обновленный список "сохраненных поисков" обратно в JSON
            writeSearchItemArrayList(searchItemArrayList, context)

            //сортируем
            sortSearchItemArrayList(context)

            //обновляем список SearchItem в Recycler View
            readSearchItemListToRcView(context)

            //запускаем парсинг

            //сообщение с подверждением
            val messageLoadWebsite = context.resources.getString(com.example.tracknews.R.string.searchItemSave)
            Toast.makeText(context, messageLoadWebsite, Toast.LENGTH_SHORT).show()

            //перезапуск worker(задачи) на поиск новостей
            WorkerFindNewsFun().workerFindNewsFirst(context)
        }
    }

    //обновляем список SearchItem в Recycler View
    fun readSearchItemListToRcView(context: Context){
        //читаем сохраненный список SearchItem
        val searchItemArrayList = readSearchItemArrayList(context)
        //Log.d(TAG, "ViewModelFunctions >f readSearchItemListToRcView > searchItemArrayList: $searchItemArrayList")
        val searchItemList = ArrayList<SearchItem>()
        searchItemArrayList.list.forEach {
            searchItemList.add(it.searchItem)
        }
        vm.searchItemList.value = searchItemList

        /*//через SharedPreferences
        val sharedPrefsRcView = getSharedPreferences("init", Context.MODE_PRIVATE)
        var stringItem = sharedPrefsRcView.getString(MainActivity.SEARCH_ITEM, "") //читаем сохраненную ранее строку с searchItem*/
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > stringItem: $stringItem")
        /*val stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin" //delete
        //Log.d(TAG, "Main Activity >f readRcViewListSearchItem > stringItem: $stringItem")
        FilesWorker().readJSONSearchItemArrayList(FILE_SEARCH_ITEM, context)


        val searchItemList = mutableListOf<SearchItem>() //готовим список searchItem
        val arrayItem = stringItem.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
        arrayItem.forEach {
            //каждый элемент массива записываем в список как объекты SearchItem
            val searchItem = SearchItem(it)
            searchItemList.add(searchItem)
        }
        vm.searchItemList.value = searchItemList
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > searchItemList: ${vm.searchItemList.value}")*/
    }

    //в процессе пеереключения между "сохраненными поисками" мы не сохраняем это в JSON. Поэтому при onPause->onDestroy мы можем сохранить
    //сохраняем активный SearchItem на время паузы приложения
    fun saveSearchItemActive(context: Context){
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)
        //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > searchItemArrayList: $searchItemArrayList")
        //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > forEach > active: ${vm.searchItemActive.value}")
        searchItemArrayList.list.forEach {
            //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > forEach > it: ${it.searchItem.search}, ${it.searchItem.active}")
            it.searchItem.active = it.searchItem.search == vm.searchItemActive.value
        }
        //searchItemArrayList.list[2].searchItem.active = true
        //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > forEach > searchItemArrayList: $searchItemArrayList")
        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)
    }

    //сбрасываем активный SearchItem на позицую 0
    fun resetSearchItemActive(context: Context){
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)
        //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > searchItemArrayList: $searchItemArrayList")

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
        //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > forEach > [0]: ${searchItemArrayList.list[0].searchItem.search}")

        //Log.d(TAG, "ViewModelFunctions >f saveSearchItemActive > searchItemArrayList: $searchItemArrayList")
        //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        writeSearchItemArrayList(searchItemArrayList, context)
    }

    //выбираем активный SearchItem
    fun selectSearchItemActive(context: Context){
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayList = readSearchItemArrayList(context)
        Log.d(TAG, "ViewModelFunctions >f selectSearchItemActive > searchItemArrayList: $searchItemArrayList")

        //если список не пустой
        if (searchItemArrayList.list.size != 0) {
            //проверяем есть ли активный "сохраненный поиск"
            var checkActiveSearchItem = false
            //если есть записываем в VM
            searchItemArrayList.list.forEach {
                //Log.d(TAG, "ViewModelFunctions >f selectSearchItemActive > forEach > it: ${it.searchItem.search}, ${it.searchItem.active}")
                if (it.searchItem.active) {
                    //Log.d(TAG, "ViewModelFunctions >f selectSearchItemActive > forEach > IF > it: ${it.searchItem.search}, ${it.searchItem.active}")
                    vm.searchItemActive.value = it.searchItem.search
                    checkActiveSearchItem = true
                }
                //Log.d(TAG, "ViewModelFunctions >f selectSearchItemActive > forEach > active: ${vm.searchItemActive.value}")
            }
            //если нет, то перезагрузка списка
            if (!checkActiveSearchItem) {
                resetSearchItemActive(context)
            }
        } else vm.searchItemActive.value = ""
        //?? пока не сохраняем состояние
        //resetSearchItemActive(context)
    }

    //сортировка списка searchItem
    fun sortSearchItemArrayList(context: Context){
        val searchItemArrayList = readSearchItemArrayList(context)
        Log.d(TAG, "MainActivity >f sortSearchItemArrayList > searchItemArrayListOld: $searchItemArrayList")
        searchItemArrayList.list.sortBy { it.searchItem.search }
        Log.d(TAG, "MainActivity >f sortSearchItemArrayList > searchItemArrayListOld: $searchItemArrayList")

        writeSearchItemArrayList(searchItemArrayList, context)
        //readSearchItemListToRcView(context)
        selectSearchItemActive(context)
    }

    //обрабатываем нажатия на searchItems >
    //при клике на элемент searchItem в recycler view -> он становится активным -> из БД загружаются все новости с этим именем
    fun clickOnSearchItem(searchItem: SearchItem) {
        //Log.d(TAG, "MainActivity >f clickOnSearchItem > searchItem.search: ${searchItem.search}")
        //выделяем элемент, меняя его состояние (active)
        vm.searchItemList.value?.forEach {
            it.active = false
            if (it.search == searchItem.search) {
                it.active = true
            }
            //Log.d(TAG, "MainActivity >f clickOnSearchItem > it Active: ${it.search}")
            //Log.d(TAG, "MainActivity >f clickOnSearchItem > it: ${it.active}")
        }
        //обновляем searchItemList, чтобы LiveData подхватила изменения и отобразила измененные кнопки
        vm.searchItemList.value = vm.searchItemList.value
        //searchItem.active = true
        //Log.d(TAG, "MainActivity >f clickOnSearchItem > searchItemList.size: ${vm.searchItemList.value}")

        //Log.d(TAG, "MainActivity >f clickOnSearchItem > searchItemList.size: ${vm.searchItemList.value?.size}")
        vm.searchItemActive.value = searchItem.search
    }

    //длинное нажатие для выделения этого searchItem и его удаления
    fun selectSearchItem(searchItem: SearchItem) {
        var a = vm.searchItemDeleteCount.value
        if (a != null) {
            a++
        }
        vm.searchItemDeleteCount.value = a

        vm.searchItemDeleteArrayList.value?.add(searchItem.search)

        //Log.d(TAG, "Main Activity >f selectSearchItem >  Count: ${vm.searchItemDeleteCount.value}")
        Log.d(MainActivity.TAG, "MainActivity >f selectSearchItem >  Delete: ${vm.searchItemDeleteArrayList.value}")
    }

    //длинное нажатие на выделенном элементе для отмены выделения
    fun unSelectSearchItem(searchItem: SearchItem) {
        var a = vm.searchItemDeleteCount.value
        if (a != null) {
            a--
        }
        vm.searchItemDeleteCount.value = a

        //val idUnselect = vm.searchItemDeleteArrayList.value?.indexOf(searchItem.search)
        vm.searchItemDeleteArrayList.value?.remove(searchItem.search)

        //Log.d(TAG, "Main Activity >f unSelectSearchItem >  Count: ${vm.searchItemDeleteCount.value}")
        Log.d(MainActivity.TAG, "MainActivity >f unSelectSearchItem >  Delete: ${vm.searchItemDeleteArrayList.value}")
    }

    //удалить выбранные "сохраненные поиски" и удалить из БД все новости с ними связанные
    fun deleteSelectSearchItemAndNews(mainDbManager: MainDbManager, searchItemAdapter: SearchItemAdapter, context: Context){
        //загружаем список "сохраненных поисков" (SearchItem)
        val searchItemArrayListOld = readSearchItemArrayList(context)
        val dataListWorker = ArrayList<SearchItemWorker>()
        //val searchItemArrayListDelete = vm.searchItemDeleteArrayList.value
        var checkDeleteActiveSearchItem = false

        Log.d(TAG, "MainActivity >f deleteSelectSearchItemAndNews > Start\n" +
                "-- searchItemArrayListOld: $searchItemArrayListOld\n" +
                "-- dataListWorker: $dataListWorker\n" +
                "-- searchItemArrayListDelete: ${vm.searchItemDeleteArrayList.value}")
        searchItemArrayListOld.list.forEach { itSearch ->
            Log.d(TAG, "MainActivity >f deleteSelectSearchItemAndNews > forEach > itSearch: ${itSearch.searchItem.search}")
            //проверяем совпадение
            var checkSame = false
            var count = 0
            vm.searchItemDeleteArrayList.value?.forEach { itSearchDelete ->
                count++
                Log.d(TAG, "MainActivity >f deleteSelectSearchItemAndNews $count > forEach > itSearchDelete: $itSearchDelete")
                if (itSearch.searchItem.search == itSearchDelete) {
                    //если совпадение есть
                    checkSame = true
                    Log.d(TAG, "MainActivity >f deleteSelectSearchItemAndNews> IF > ${searchItemArrayListOld.list.indexOf(itSearch)}")
                }
            }
            //если совпадений нет -> добавляем элемент в новый список "сохраненных поисков"
            if (!checkSame) {
                Log.d(TAG, "MainActivity >f deleteSelectSearchItemAndNews > IF NEW> ${searchItemArrayListOld.list.indexOf(itSearch)}")
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

        Log.d(TAG, "MainActivity >f deleteSelectSearchItemAndNews > End\n" +
                "-- searchItemArrayListNew: $searchItemArrayListNew\n" +
                "-- dataListWorker: $dataListWorker\n" +
                "-- searchItemArrayListDelete: ${vm.searchItemDeleteArrayList.value}")

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
    }

    //отменить выделение "сохраненных поисков"
    fun cancelSelectSearchItem(searchItemAdapter: SearchItemAdapter){
        vm.searchItemDeleteCount.value = 0 //скрываем кнопки
        vm.searchItemDeleteArrayList.value = ArrayList() //очищаем список на удаление
        searchItemAdapter.notifyDataSetChanged() //обновляем RcView с SearchItem
    }
    //обрабатываем нажатия на searchItems ^

    //загружаем список "сохраненных поисков" (SearchItem) <- читаем данные из JSON
    private fun readSearchItemArrayList(context: Context) = FilesWorker().readJSONSearchItemArrayList(FILE_SEARCH_ITEM, context)

    //Записываем обновленный список "сохраненных поисков" обратно в JSON
    private fun writeSearchItemArrayList(searchItemArrayList: SearchItemArrayList, context: Context){
        FilesWorker().writeJSON(searchItemArrayList, FILE_SEARCH_ITEM, context)
    }
}

//BACKUP
//тоже самое что и -> it.searchItem.active = it.searchItem.search == vm.searchItemActive.value
/*if (it.searchItem.search == vm.searchItemActive.value) {
    it.searchItem.active = true
}
else {
    it.searchItem.active = false
}*/

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