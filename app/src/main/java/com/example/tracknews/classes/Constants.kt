package com.example.tracknews.classes

object Constants {
    //log
    const val TAG = "TAG1"
    const val TAG_DEBUG = "TAG_DEBUG"

    //notification
    const val NOTIFICATION_ID = 101
    const val CHANNEL_ID = "channelID"

    //theme
    const val PREFS_NAME = "theme_prefs"
    const val KEY_THEME = "prefs.theme"
    const val THEME_UNDEFINED = -1
    const val THEME_LIGHT = 0
    const val THEME_DARK = 1
    const val THEME_SYSTEM = 2
    const val THEME_BATTERY = 3

    //SharedPreferences
    const val SEARCH_ITEM = "search"
    const val SHARED_FIRST_LAUNCH = "firstLaunch"

    //Имена файлов
    const val FILE_SEARCH_ITEM = "searchItems.json"
    const val FILE_FIRST_LAUNCH = "firstLaunch.txt"

    //Worker
    const val WORKER_TAG_PARSER = "parser"
    const val WORKER_UNIQUE_NAME_PARSER = "uniqueParser"
    const val WORKER_PUT_ID = "id"
    const val WORKER_PUT_IMG = "img"
    const val WORKER_PUT_DATE = "date"
    const val WORKER_PUT_TITLE = "title"
    const val WORKER_PUT_CONTENT = "content"
    const val WORKER_PUT_LINK = "link"
    const val WORKER_PUT_LINK_SQL = "linkSQL"
    const val WORKER_PUT_STATUS_SAVED = "statusSaved"

    const val WORKER_PUT_STATUS_UPDATE = "statusUpdate"
}