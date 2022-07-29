package com.example.tracknews.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class MainDbHelper(context: Context) : SQLiteOpenHelper(context, MainDbNameObject.DATABASE_NAME, null, MainDbNameObject.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(MainDbNameObject.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(MainDbNameObject.SQL_DELETE_TABLE)
        onCreate(db)
    }
}
/*class MainDbHelper(context: Context) : SQLiteOpenHelper(context, MainDbNameObject.DATABASE_NAME, null, MainDbNameObject.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(MainDbNameObject.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(MainDbNameObject.SQL_DELETE_TABLE)
        onCreate(db)
    }
}*/
/*class MainDbHelper(fragment: TestFragment1) : SQLiteOpenHelper(fragment.requireContext(), MainDbNameObject.DATABASE_NAME, null, MainDbNameObject.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(MainDbNameObject.SQL_CREATE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(MainDbNameObject.SQL_DELETE_TABLE)
        onCreate(db)
    }
}*/
