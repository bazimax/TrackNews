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

//Основная логика приложения завязанная на ViewModel
class ViewModelFunctions(viewModel: ViewModel) {

    private val vm = viewModel
    fun loadSQLiteToViewModel(mainDbManager: MainDbManager){
        //читаем Базу Данных
        Log.d(MainActivity.TAG_DEBUG, "Activity >f loadSQLiteToViewModel ======START")
        vm.newsItemArray.value = mainDbManager.readDbData()
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        Log.d(MainActivity.TAG_DEBUG, "Activity >f loadSQLiteToViewModel ------------END")
    }

    fun loadSQLiteToViewModelActive(mainDbManager: MainDbManager, search: String){
        //читаем Базу Данных
        var search1 = search
        search1 = "123"
        Log.d(MainActivity.TAG_DEBUG, "Activity >f loadSQLiteToViewModel ======START")
        vm.newsItemArray.value = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH , search1) //search
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        Log.d(MainActivity.TAG_DEBUG, "Activity >f loadSQLiteToViewModel ------------END")
    }

    // отслеживаем и удаляем элементы БД
    fun updateElementOfSQLite(mainDbManager: MainDbManager, owner: LifecycleOwner, context: Context){
        //обновляем статус у новости (сохранено или нет) //удалем элемент/строку из Базы Данных
        vm.newsItemUpdateItem.observe(owner) {
            val statusSaved = vm.newsItemUpdateItem.value?.statusSaved
            val id = vm.newsItemUpdateItem.value?.id
            if (statusSaved != null) {
                //mainDbManager.deleteDbElement(link, "link")
                if (id != null) {
                    mainDbManager.updateDbElementStatusSaved(statusSaved, id)
                }
            }
            Toast.makeText(context, statusSaved, Toast.LENGTH_SHORT).show()
            vm.activeSearchItem.value?.let { it1 -> loadSQLiteToViewModelActive(mainDbManager, it1) } //reload
        }
    }

    //отслеживание изменений в ViewModel
    fun observeVM(mainDbManager: MainDbManager, owner: LifecycleOwner){
        //обновление отображаемых новостей для конкретного "поискового запроса"
        vm.activeSearchItem.observe(owner) {
            //Ищем все новости по данному запросу напрямую в БД и обновляем выводимый список новостей
            vm.newsItemArray.value = vm.activeSearchItem.value?.let { it1 ->
                mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, it1)
            }
        }
    }

    fun viewModelToSQLite(mainDbManager: MainDbManager, owner: LifecycleOwner){
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
        Log.d(MainActivity.TAG_DEBUG, "Activity >f viewModelToSQLite >  ======START")
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
            Log.d(MainActivity.TAG_DEBUG, "Activity >f viewModelToSQLite > newsItemTempYa.OBSERVE ======START")
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
            Log.d(MainActivity.TAG_DEBUG, "Activity >f viewModelToSQLite > newsItemTempYa.OBSERVE ------------END")
            loadSQLiteToViewModel(mainDbManager)
        }
        Log.d(MainActivity.TAG_DEBUG, "Activity >f viewModelToSQLite > ------------END")
    }
}