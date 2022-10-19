package com.example.tracknews.classes

data class SearchItemArrayList(
    val list: ArrayList<SearchItemWorker>
)

data class SearchItemWorker (val searchItem: SearchItem,
                             var counterNewNews2: Int = 0)

data class SearchItem (val search: String = "",
                       var counterNewNews: Int = 0)

