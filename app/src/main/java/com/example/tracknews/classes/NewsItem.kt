package com.example.tracknews.classes

import androidx.activity.viewModels
import com.example.tracknews.ViewModel

data class NewsItem(val id: Int = 0,
                    val search: String = "",
                    val img: String = "",
                    val date: String = "base",
                    val title: String = "default title",
                    val content: String = "default content",
                    val link: String = "0000@default.com",
                    val statusSaved: String = "false")