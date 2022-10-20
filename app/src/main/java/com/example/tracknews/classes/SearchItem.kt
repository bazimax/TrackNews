package com.example.tracknews.classes

data class SearchItemArrayList(
    val list: ArrayList<SearchItemWorker>
)

data class SearchItemWorker (val searchItem: SearchItem,
                             var counterNewNewsBackup: Int = 0)

data class SearchItem (val search: String = "",
                       var counterNewNews: Int = 0,
                       var counterAllNews: Int = 0,
                       var active: Boolean = false)

