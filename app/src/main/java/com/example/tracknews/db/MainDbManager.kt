package com.example.tracknews.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log

class MainDbManager(context: Context) {

    private val mainDbHelper = MainDbHelper(context) //может открывать БД и ид
    private var db : SQLiteDatabase? = null //через mainDbHelper работает с БД

    fun openDb(){
        db = mainDbHelper.writableDatabase
    }
    //fun insertToDb(date: String, title: String, content: String, link: String)
    fun insertToDb(title: String) {
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
    fun readDbData() : ArrayList<String>{
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
        while (cursor?.moveToNext()!!){
            val dataText = cursor.getString(cursor.getColumnIndex(MainDbNameObject.COLUMN_NAME_TITLE))
            //Log.d("TAG1", "SQL dataText: $dataText")
            dataList.add(dataText.toString())
        }
        cursor.close()
        //Log.d("TAG1", "SQL close cursor, dataList: $dataList")
        return dataList
    }
    fun closeDb(){
        mainDbHelper.close()
    }

    fun clearAllDataInDb(){
        db?.delete(MainDbNameObject.TABLE_NAME, null, null);
    }
}