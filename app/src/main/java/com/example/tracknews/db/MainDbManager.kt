package com.example.tracknews.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.NewsItem

class MainDbManager(context: Context) {
    private val logNameClass = "MainDbManager" //для логов

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_IF = Constants.TAG_DATA_IF //переменные и данные в циклах
    }

    private val mainDbHelper = MainDbHelper(context) //может открывать БД и ид
    private var db : SQLiteDatabase? = null //через mainDbHelper работает с БД

    //открываем-запускаем Базу Данных
    fun openDb(){
        Log.d(TAG_DEBUG, "$logNameClass >f openDb === START")
        db = mainDbHelper.writableDatabase
        Log.d(TAG_DEBUG, "$logNameClass >f openDb ----- END > OK")
    }

    //добавляем данные в Базу Данных
    fun insertToDb(search: String, img: String, date: String, title: String, content: String, link: String, statusSaved: String) {
        Log.d(TAG_DEBUG, "$logNameClass >f insertToDB === START")
        Log.d(TAG_DATA, "$logNameClass > insertToDB > SQL title...: $title, $content, $link")
        val values = ContentValues().apply {
            put(MainDbNameObject.COLUMN_NAME_SEARCH, search)
            put(MainDbNameObject.COLUMN_NAME_IMG, img)
            put(MainDbNameObject.COLUMN_NAME_DATE, date)
            put(MainDbNameObject.COLUMN_NAME_TITLE, title)
            put(MainDbNameObject.COLUMN_NAME_CONTENT, content)
            put(MainDbNameObject.COLUMN_NAME_LINK, link)
            put(MainDbNameObject.COLUMN_NAME_STATUS_SAVED, statusSaved)
        }
        db?.insert(MainDbNameObject.TABLE_NAME, null, values)

        Log.d(TAG_DEBUG, "$logNameClass >f insertToDB ----- END")
    }

    //закрытие Базы Данных
    fun closeDb(){
        mainDbHelper.close()
        Log.d(TAG_DEBUG, "$logNameClass >f openDb === START / END")
    }

    //ищем совпадения в БД
    fun findItemInDb(columnSearch: String, search: String): ArrayList<NewsItem>{
        Log.d(TAG_DEBUG, "$logNameClass >f findItemInDb === START")
        Log.d(TAG_DEBUG, "$logNameClass >f findItemInDb // ищем совпадения в БД")

        val tableName = MainDbNameObject.TABLE_NAME
        val selection =  "$columnSearch LIKE '%' || ? || '%'"
        //V2
        //val selectQuery5 = "SELECT * FROM $tableName WHERE $column LIKE '%' || :string || '%'"
        val selectionArgs = arrayOf(search)//arrayOf("9422089")

        val dataList = ArrayList<NewsItem>()
        val cursor = db?.query(tableName, null, selection, selectionArgs, null, null, null)

        while (cursor?.moveToNext()!!){

            val searchNew = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_SEARCH)).toString()
            val img = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_IMG)).toString()
            val date = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_DATE)).toString()
            val title = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE)).toString()
            val content = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_CONTENT)).toString()
            val link = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_LINK)).toString()

            val statusSaved = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_STATUS_SAVED)).toString()

            val id: Int = cursor.position //записываем id //??!! неверный id


            val newsItem = NewsItem(id, searchNew, img, date, title, content, link, statusSaved)
            dataList.add(newsItem)
        }
        cursor.close()

        //сортировка dataList
        sortByDate(dataList)

        Log.d(TAG_DEBUG, "$logNameClass >f findItemInDb ----- END")
        return dataList
    }

    //обновляем элемент/строку из Базы Данных (статус сохранения новости)
    fun updateDbElementStatusSaved(statusSaved: String, link: String){
        Log.d(TAG_DEBUG, "$logNameClass >f updateDbElementStatusSaved === START")

        val values = ContentValues().apply {

            if (statusSaved == true.toString()) {
                put(MainDbNameObject.COLUMN_NAME_STATUS_SAVED, "${false}")
                Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > statusSaved True > FALSE")
            }
            else {
                put(MainDbNameObject.COLUMN_NAME_STATUS_SAVED, "${true}")
                Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > statusSaved False > TRUE")
            }
        }

        Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > values: $values")

        db?.update(MainDbNameObject.TABLE_NAME, values, "${MainDbNameObject.COLUMN_NAME_LINK} = ?", arrayOf(link))

        Log.d(TAG_DEBUG, "$logNameClass >f updateDbElementStatusSaved ----- END")
    }

    //удаляем элемент/строку из БД по столбцу "search"
    fun deleteDbElement(search: String){
        db?.delete(MainDbNameObject.TABLE_NAME,"search=?", arrayOf(search))
        Log.d(TAG_DEBUG, "$logNameClass >f deleteDbElement === START / END")
        Log.d(TAG_DATA, "$logNameClass >f deleteDbElement > search: $search")
    }

    //сортируем массив по дате
    private fun sortByDate(dataList: ArrayList<NewsItem>): ArrayList<NewsItem>{

        dataList.forEach {
            Log.d(TAG_DATA_IF, "$logNameClass >f sortByDate > START > dataList.forEach > date: ${it.date}")
            Log.d(TAG_DATA_IF, "$logNameClass >f sortByDate > START > dataList.forEach > link: ${it.link}")
        }
        dataList.sortByDescending{it.date}

        dataList.forEach {
            Log.d(TAG_DATA_IF, "$logNameClass >f sortByDate > END > dataList.forEach > date: ${it.date}")
            Log.d(TAG_DATA_IF, "$logNameClass >f sortByDate > END > dataList.forEach > link: ${it.link}")
        }
        return dataList
    }
}