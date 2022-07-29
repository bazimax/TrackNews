package com.example.tracknews.db

import android.provider.BaseColumns

object MainDbNameObject : BaseColumns {
    const val TABLE_NAME = "news_table"
    const val COLUMN_NAME_TITLE = "title"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "NewsDB.db"

    const val SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "$COLUMN_NAME_TITLE TEXT)"

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}

/*
object MainDbNameObject : BaseColumns {
    const val TABLE_NAME = "news_table"
    const val COLUMN_NAME_DATE = "date"
    const val COLUMN_NAME_TITLE = "title"
    const val COLUMN_NAME_CONTENT = "content"
    const val COLUMN_NAME_LINK = "link"

    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "NewsDB.db"

    const val SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                "$COLUMN_NAME_DATE TEXT," +
                "$COLUMN_NAME_TITLE TEXT," +
                "$COLUMN_NAME_CONTENT TEXT," +
                "$COLUMN_NAME_LINK TEXT)"

    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}*/
