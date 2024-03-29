package com.example.tracknews.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class MainDbHelper(context: Context) : SQLiteOpenHelper(context, MainDbNameObject.DATABASE_NAME, null, MainDbNameObject.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        Log.d("TAG1", "MainDbHelper >f onCreate")
        db?.execSQL(MainDbNameObject.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("TAG1", "MainDbHelper >f onUpgrade")
        db?.execSQL(MainDbNameObject.SQL_DELETE_TABLE)
        onCreate(db)
    }
}
