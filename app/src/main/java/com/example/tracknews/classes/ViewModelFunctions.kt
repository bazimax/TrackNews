package com.example.tracknews.classes

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.MainActivity
import com.example.tracknews.Services.WorkerFindNews
import com.example.tracknews.Services.WorkerFindNewsFun
import com.example.tracknews.ViewModel
import com.example.tracknews.db.MainDbHelper
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites

//Основная логика приложения завязанная на ViewModel
class ViewModelFunctions(viewModel: ViewModel) {
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG
    }

    private val vm = viewModel
    private var parserSites = ParserSites() //парсинг

    private fun loadSQLiteToViewModel(mainDbManager: MainDbManager){
        //читаем Базу Данных
        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ======START")

        vm.newsItemArray.value = mainDbManager.readDbData()
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ------------END")
    }

    private fun loadSQLiteToViewModelActive(mainDbManager: MainDbManager, search: String){
        //читаем Базу Данных
        Log.d(MainActivity.TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ======START")

        var search1 = search
        search1 = "witcher" //Delete
        vm.newsItemArray.value = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, search1) //search
        val counter = vm.newsItemArray.value!!.size
        //Log.d(TAG, "ViewModelFunctions >f loadSQLiteToViewModel > vm.newsItemArray > Size: ${vm.newsItemArray.value!!.size}")
        //Log.d(TAG, "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        Log.d(TAG_DEBUG, "ViewModelFunctions >f loadSQLiteToViewModel ------------END")
    }

    // отслеживаем и удаляем элементы БД
    private fun updateElementOfSQLite(mainDbManager: MainDbManager, owner: LifecycleOwner, context: Context){
        //обновляем статус у новости (сохранено или нет) //удалем элемент/строку из Базы Данных
        vm.newsItemUpdateItem.observe(owner) {
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
            vm.activeSearchItem.value?.let { it1 -> loadSQLiteToViewModelActive(mainDbManager, it1) } //reload
        }
    }

    //отслеживание изменений в ViewModel
    fun observeVM(mainDbManager: MainDbManager, owner: LifecycleOwner, context: Context){
        //?? подключаем observe
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
        viewModelToSQLite(mainDbManager, owner)

        // загружаем БД во viewModel
        vm.activeSearchItem.value?.let { loadSQLiteToViewModelActive(mainDbManager, it) }

        // отслеживаем и удаляем элементы БД
        updateElementOfSQLite(mainDbManager, owner, context)

        //обновление отображаемых новостей для конкретного "поискового запроса"
        vm.activeSearchItem.observe(owner) {
            //Ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей
            vm.newsItemArray.value = vm.activeSearchItem.value?.let { it1 ->
                mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, it1)
            }
        }
    }

    private fun viewModelToSQLite(mainDbManager: MainDbManager, owner: LifecycleOwner){
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
        Log.d(MainActivity.TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite >  ======START")
        //Получили новые новости
        vm.newsItemTemp.observe(owner){
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
        }
        vm.newsItemTempYa.observe(owner){
            Log.d(MainActivity.TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > newsItemTempYa.OBSERVE ======START")
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
            Log.d(MainActivity.TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > newsItemTempYa.OBSERVE ------------END")
            loadSQLiteToViewModel(mainDbManager)
        }
        Log.d(MainActivity.TAG_DEBUG, "ViewModelFunctions >f viewModelToSQLite > ------------END")
    }

    //поиск новостей по "НЕсохраненному поиску"(запросу/подписке)
    fun searchNews(search: String, context: Context){
        if (search != "") {
            //проверка интернета
            //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()
            //запускаем парсинг новостных сайтов/сайта
            val resultParse = parserSites.parse(search)

            //если парсинг не удался
            if (resultParse.statusEthernet == false.toString()) {
                val messageLoadWebsite = context.resources.getString(com.example.tracknews.R.string.loadWebsiteFail)
                Toast.makeText(context, messageLoadWebsite, Toast.LENGTH_SHORT).show()
            }
            else {
                vm.newsItemTempYa.value = null
                //??
                //полученные данные отправляем в ViewModel
                vm.newsItemTempYa.value = resultParse.list
            }
        }
    }

    fun readSearchItemListToRcView(context: Context){
        //читаем сохраненный список SearchItem
        /*//через SharedPreferences
        val sharedPrefsRcView = getSharedPreferences("init", Context.MODE_PRIVATE)
        var stringItem = sharedPrefsRcView.getString(MainActivity.SEARCH_ITEM, "") //читаем сохраненную ранее строку с searchItem*/
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > stringItem: $stringItem")
        var stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin" //delete
        //Log.d(TAG, "Main Activity >f readRcViewListSearchItem > stringItem: $stringItem")
        FilesWorker().readJSONSearchItemArrayList(Constants.FILE_SEARCH_ITEM, context)

        val searchItemList = mutableListOf<SearchItem>() //готовим список searchItem
        val arrayItem = stringItem.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
        arrayItem.forEach {
            //каждый элемент массива записываем в список как объекты SearchItem
            val searchItem = SearchItem(it)
            searchItemList.add(searchItem)
        }
        vm.searchItemList.value = searchItemList
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > searchItemList: ${vm.searchItemList.value}")
    }
}