package com.example.tracknews.classes

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.example.tracknews.News.NewsAllFragment
import com.example.tracknews.News.NewsSavedFragment
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.News.NewsWeekFragment
import com.example.tracknews.R
import com.example.tracknews.classes.Constants.TAG_DEBUG

object Constants {
    //log
    const val TAG = "TAG_D_1" //разное
    const val TAG_DEBUG = "TAG_DEBUG" //запуск функция, активити и тд
    const val TAG_DATA = "TAG_DATA" //переменные и данные
    const val TAG_DATA_BIG = "TAG_BIG_DATA" //объемные данные
    const val TAG_DATA_IF = "TAG_DATA_IF" //переменные и данные в циклах

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
    const val SHARED_INSTRUCTION = "instruction"

        //Имена файлов
    const val FILE_SEARCH_ITEM = "searchItems.json"
    const val FILE_FIRST_LAUNCH = "firstLaunch.txt"
    const val FILE_TEST_LOAD_SITE = "test_load_site.txt"

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

    //savedInstanceState
    const val STATE_SEARCH_ITEM_ACTIVE = "stateSearchItemActive"
}




