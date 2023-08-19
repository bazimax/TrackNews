package com.example.tracknews.classes

data class NewsItem(
    var id: Int = 0,
    val search: String = "",
    val img: String = "",
    val date: String = "base",
    val title: String = "default title",
    val content: String = "default content",
    val link: String = "0000@default.com",
    var statusSaved: String = "false")

fun init(){
}