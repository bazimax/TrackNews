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
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, активити и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
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
        //Log.d("TAG1", "SQL values: $values")
        db?.insert(MainDbNameObject.TABLE_NAME, null, values)
        //Log.d("TAG1", "SQL db: $db, ${db.toString()}")
        Log.d(TAG_DEBUG, "$logNameClass >f insertToDB ----- END")
    }

    fun testReadDbData() : ArrayList<String>{
        val dataList = ArrayList<String>()
        //Log.d("TAG1", "SQL create dataList: $dataList")
        val cursor = db?.query(MainDbNameObject.TABLE_NAME, null, null, null, null, null, null)

        while (cursor?.moveToNext()!!){
            val dataText = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE))
            //Log.d("TAG1", "SQL dataText: $dataText")
            dataList.add(dataText.toString())
        }
        cursor.close()
        return dataList
    }

    //чтение Базы Данных
    fun readDbData() : ArrayList<NewsItem>{
        Log.d(TAG_DEBUG, "$logNameClass >f readDbData === START")
        val dataList = ArrayList<NewsItem>()
        val cursor = db?.query(MainDbNameObject.TABLE_NAME, null, null, null, null, null, null)
        //Log.d("TAG1", "readDbData > SQL cursor: $cursor")
        //Log.d("TAG1", "readDbData > cursor columnCount: ${cursor?.columnCount}")
        //Log.d("TAG1", "readDbData > cursor count: ${cursor?.count}")
        while (cursor?.moveToNext()!!){

            //Log.d("TAG1", "readDbData > Start while")
            val search = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_SEARCH)).toString()
            //Log.d("TAG1", "readDbData > val 1")
            val img = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_IMG)).toString()
            //Log.d("TAG1", "readDbData > val 2")
            val date = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_DATE)).toString()
            //Log.d("TAG1", "readDbData > val 3")
            val title = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE)).toString()
            //Log.d("TAG1", "readDbData > val 4")
            val content = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_CONTENT)).toString()
            //Log.d("TAG1", "readDbData > val 5")
            val link = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_LINK)).toString()

            val statusSaved = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_STATUS_SAVED)).toString()

            //val id = cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_STATUS_SAVED).toString()
            val id: Int = cursor.position //записываем id
            //Log.d("TAG1", "readDbData > INDEX > ${cursor.position}")

            val newsItem = NewsItem(id, search, img, date, title, content, link, statusSaved)
            dataList.add(newsItem)
            //Log.d("TAG1", "readDbData > while ------------END ??")
        }

        cursor.close()
        Log.d(TAG_DEBUG, "$logNameClass >f readDbData ----- END")
        return dataList
    }

    //закрытие Базы Данных
    fun closeDb(){
        mainDbHelper.close()
        Log.d(TAG_DEBUG, "$logNameClass >f openDb === START / END")
    }

    //очистка всей Базы Данных
    fun clearAllDataInDb(){
        db?.delete(MainDbNameObject.TABLE_NAME, null, null)
        Log.d(TAG_DEBUG, "$logNameClass >f clearAllDataInDb === START / END")
    }

    //удаляем элемент/строку из БД
    fun deleteDbElement(whereClause: String, whereArgs: String){
        Log.d(TAG_DEBUG, "$logNameClass >f deleteDbElement === START")
        val deleteDbElement = db?.delete(MainDbNameObject.TABLE_NAME, "$whereClause = ?", arrayOf(whereArgs))
        Log.d(TAG_DATA, "$logNameClass >f deleteDbElement > deleteDbElement: $deleteDbElement")
        Log.d(TAG_DEBUG, "$logNameClass >f deleteDbElement ----- END")
    }

    //ищем совпадения в БД
    fun findItemInDb(columnSearch: String, search: String): ArrayList<NewsItem>{
        Log.d(TAG_DEBUG, "$logNameClass >f findItemInDb === START")
        Log.d(TAG_DEBUG, "$logNameClass >f findItemInDb // ищем совпадения в БД")

        val tableName = MainDbNameObject.TABLE_NAME
        //val columns = arrayOf(MainDbNameObject.COLUMN_NAME_SEARCH, MainDbNameObject.COLUMN_NAME_LINK)
        val selection =  "$columnSearch LIKE '%' || ? || '%'"
        val selectionArgs = arrayOf(search)//arrayOf("9422089")

        val dataList = ArrayList<NewsItem>()
        val cursor = db?.query(tableName, null, selection, selectionArgs, null, null, null)
        //Log.d("TAG1", "MainDbManager >f findItemInDb > cursor: ${cursor?.position}")
        //Log.d("TAG1", "MainDbManager >f findItemInDb > cursor2: $cursor2")
        while (cursor?.moveToNext()!!){

            //Log.d("TAG1", "readDbData > Start while")
            val search = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_SEARCH)).toString()
            //Log.d("TAG1", "MainDbManager >f findItemInDb > val 1: $search")
            val img = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_IMG)).toString()
            val date = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_DATE)).toString()
            val title = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE)).toString()
            val content = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_CONTENT)).toString()
            val link = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_LINK)).toString()
            //Log.d("TAG1", "MainDbManager >f findItemInDb > link: $link")

            val statusSaved = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_STATUS_SAVED)).toString()

            //val id = cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_STATUS_SAVED).toString()
            val id: Int = cursor.position //записываем id //??!! неверный id
            //Log.d("TAG1", "MainDbManager >f findItemInDb > id: $id")
            //Log.d("TAG1", "MainDbManager >f findItemInDb > INDEX > ${cursor.position}")

            val newsItem = NewsItem(id, search, img, date, title, content, link, statusSaved)
            dataList.add(newsItem)
            //Log.d("TAG1", "MainDbManager >f findItemInDb > while ------------END")
        }
        cursor.close()
        //Log.d("TAG1", "MainDbManager >f findItemInDb > dataList: ${dataList.toString()}")
        Log.d(TAG_DEBUG, "$logNameClass >f findItemInDb ----- END")
        return dataList
    }

    //обновляем элемент/строку из Базы Данных (статус сохранения новости)
    fun updateDbElementStatusSaved(statusSaved: String, link: String){
        Log.d(TAG_DEBUG, "$logNameClass >f updateDbElementStatusSaved === START")
        val values = ContentValues().apply {
            //put(MainDbNameObject.COLUMN_NAME_LINK, "-1")
            if (statusSaved == true.toString()) {
                //put(MainDbNameObject.COLUMN_NAME_LINK, "${false}")
                put(MainDbNameObject.COLUMN_NAME_STATUS_SAVED, "${false}")
                Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > statusSaved True > FALSE")
            }
            else {
                //put(MainDbNameObject.COLUMN_NAME_LINK, "${true}")
                put(MainDbNameObject.COLUMN_NAME_STATUS_SAVED, "${true}")
                Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > statusSaved False > TRUE")
            }
        }

        //val idCorrect = (id + 1).toString()
        Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > values: $values")
        //Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > id: $id")
        //Log.d(TAG_DATA, "$logNameClass >f updateDbElementStatusSaved > idCorrect: $idCorrect")
        //db?.update(MainDbNameObject.TABLE_NAME, values, "_ID = ?", arrayOf(idCorrect))
        db?.update(MainDbNameObject.TABLE_NAME, values, "${MainDbNameObject.COLUMN_NAME_LINK} = ?", arrayOf(link))

        Log.d(TAG_DEBUG, "$logNameClass >f updateDbElementStatusSaved ----- END")
        //db?.delete(MainDbNameObject.TABLE_NAME,"name=?", arrayOf(link))
    }

    //удаляем элемент/строку из БД по столбцу "search"
    fun deleteDbElement(search: String){
        db?.delete(MainDbNameObject.TABLE_NAME,"search=?", arrayOf(search))
        Log.d(TAG_DEBUG, "$logNameClass >f deleteDbElement === START / END")
        Log.d(TAG_DATA, "$logNameClass >f deleteDbElement > search: $search")
    }
}


//BACKUP >
/*//тоже работает
        val selectQuery5 = "SELECT * FROM $tableName WHERE $column LIKE '%' || :string || '%'"
        val selectQuery1 = "SELECT  * FROM $tableName WHERE $column LIKE ?" //"SELECT  * FROM $tableName WHERE $column = ?"
        val cursor4 = db?.rawQuery(selectQuery1, selectionArgs)
        val cursor5 = db?.rawQuery(selectQuery5, selectionArgs)
        //id
        val cursor3 = db?.query(tableName,null,"_ID<=?", arrayOf("3"),null,null,null)
        val cursor1 = db?.query(tableName,null,"link LIKE 'pi%'", null,null,null,null)

        val selectQuery6 = "SELECT * FROM $tableName WHERE $column LIKE '%' || ? || '%'"
        val cursor6 = db?.rawQuery(selectQuery6, selectionArgs)*/