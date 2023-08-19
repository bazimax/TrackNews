package com.example.tracknews.classes

object Constants {
    //log
    const val TAG = "TAG_D_1" //разное
    const val TAG_DEBUG = "TAG_DEBUG" //запуск функция, activity и тд
    const val TAG_DATA = "TAG_DATA" //переменные и данные
    const val TAG_DATA_BIG = "TAG_BIG_DATA" //объемные данные
    const val TAG_DATA_IF = "TAG_DATA_IF" //переменные и данные в циклах
    const val TAG_ERROR = "TAG_ERROR" //ошибка

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
    const val SHARED_FIRST_LAUNCH = "firstLaunch"
    const val SHARED_INSTRUCTION = "instruction"

        //Имена файлов
    const val FILE_SEARCH_ITEM = "searchItems.json"
    const val FILE_TEST_LOAD_SITE = "test_load_site.txt"

    //Worker
    const val WORKER_TAG_PARSER = "parser"
    const val WORKER_UNIQUE_NAME_PARSER = "uniqueParser"
    const val WORKER_PUT_STATUS_UPDATE = "statusUpdate"

    //savedInstanceState
    const val STATE_SEARCH_ITEM_ACTIVE = "stateSearchItemActive"
}




