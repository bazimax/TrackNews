package com.example.tracknews.classes

data class SearchItemArrayList(
    val list: ArrayList<SearchItemWorker>
)

data class SearchItemWorker (val searchItem: SearchItem,
                             var counterNewNews: Int = 0)

data class SearchItem (val search: String = "")

