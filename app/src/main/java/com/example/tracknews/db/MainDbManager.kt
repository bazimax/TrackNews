package com.example.tracknews.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.tracknews.classes.NewsItem

class MainDbManager(context: Context) {

    private val mainDbHelper = MainDbHelper(context) //может открывать БД и ид
    private var db : SQLiteDatabase? = null //через mainDbHelper работает с БД

    fun openDb(){
        db = mainDbHelper.writableDatabase
    }

    fun testInsertToDb(title: String) {
        //Log.d("TAG1", "SQL title: $title")
        val values = ContentValues().apply {
            //put(MainDbNameObject.COLUMN_NAME_DATE, date)
            put(MainDbNameObject.COLUMN_NAME_TITLE, title)
            //put(MainDbNameObject.COLUMN_NAME_CONTENT, content)
            //put(MainDbNameObject.COLUMN_NAME_LINK, link)
        }
        //Log.d("TAG1", "SQL values: $values")
        db?.insert(MainDbNameObject.TABLE_NAME, null, values)
        //Log.d("TAG1", "SQL db: $db, ${db.toString()}")
    }

    //fun insertToDb(date: String, title: String, content: String, link: String)
    fun insertToDb(title: String, content: String, link: String) {
        //Log.d("TAG1", "insertToDB > SQL title...: $title, $content, $link")
        val values = ContentValues().apply {
            //put(MainDbNameObject.COLUMN_NAME_DATE, date)
            put(MainDbNameObject.COLUMN_NAME_TITLE, title)
            put(MainDbNameObject.COLUMN_NAME_CONTENT, content)
            put(MainDbNameObject.COLUMN_NAME_LINK, link)
        }
        //Log.d("TAG1", "SQL values: $values")
        db?.insert(MainDbNameObject.TABLE_NAME, null, values)
        //Log.d("TAG1", "SQL db: $db, ${db.toString()}")
    }

    fun testReadDbData() : ArrayList<String>{
        val dataList = ArrayList<String>()
        //Log.d("TAG1", "SQL create dataList: $dataList")
        val cursor = db?.query(MainDbNameObject.TABLE_NAME, null, null, null, null, null, null)
        /*with(cursor){
            while (this?.moveToNext()!!){
                val dataText = cursor?.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE))
                dataList.add(dataText.toString())
            }
        }*/
        //Log.d("TAG1", "SQL cursor: $cursor")
        //Log.d("TAG1", "readDbData > cursor columnCount: ${cursor?.columnCount}")
        //Log.d("TAG1", "readDbData > cursor count: ${cursor?.count}")
        while (cursor?.moveToNext()!!){
            val dataText = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE))
            //Log.d("TAG1", "SQL dataText: $dataText")
            dataList.add(dataText.toString())
        }
        cursor.close()
        return dataList
    }

    fun readDbData() : ArrayList<NewsItem>{
        //Log.d("TAG1", "readDbData > ======START")
        val dataList = ArrayList<NewsItem>()
        val cursor = db?.query(MainDbNameObject.TABLE_NAME, null, null, null, null, null, null)
        //Log.d("TAG1", "readDbData > SQL cursor: $cursor")
        //Log.d("TAG1", "readDbData > cursor columnCount: ${cursor?.columnCount}")
        //Log.d("TAG1", "readDbData > cursor count: ${cursor?.count}")
        while (cursor?.moveToNext()!!){

            //Log.d("TAG1", "readDbData > Start while")
            val dataTitle = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE))
            val dataContent = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_CONTENT))
            val dataLink = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_LINK))

            val newsItem = NewsItem(dataTitle.toString(), dataContent.toString(), dataLink.toString())
            dataList.add(newsItem)
            //Log.d("TAG1", "readDbData > while ------------END ??")
        }

        cursor.close()
        //Log.d("TAG1", "readDbData >  ------------END")
        return dataList
    }

    fun closeDb(){
        mainDbHelper.close()
    }

    fun clearAllDataInDb(){
        db?.delete(MainDbNameObject.TABLE_NAME, null, null);
    }


}