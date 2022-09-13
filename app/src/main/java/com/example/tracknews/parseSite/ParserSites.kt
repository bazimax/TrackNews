package com.example.tracknews.parseSite

import android.util.Log
import com.example.tracknews.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class ParserSites {
    private val newsItemList = ArrayList<NewsItem>()

    fun parse(search: String): ArrayList<NewsItem>{
        parseYa(search)
        //parseGoo(search)
        return newsItemList
    }

    fun test(str: String){
        //Log.d("TAG1", "$str")
    }

    private fun initFromParseHTML(url: String): String {
        //Log.d("TAG1", "Start - initFromParseHTML")
        var siteTemp = ""
        //coroutines
        GlobalScope.launch { // запуск новой сопрограммы в фоне
            //delay(1000L) // неблокирующая задержка на 1 секунду

            val urlConnection = URL(url).openConnection() as HttpsURLConnection

            try {
                urlConnection.connect()

                if(urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d("TAG1", "Connection - Good")
                    val br = BufferedReader(
                        InputStreamReader(
                            urlConnection.inputStream,
                            StandardCharsets.UTF_8
                        )
                    )

                    br.lineSequence().forEach {
                        siteTemp += it
                    }
                    br.close()
                }
                else Log.d("TAG1", "Connection - Failed")
            }catch (e: IOException) {
                e.printStackTrace()
                //Log.d("TAG1", "$e")
            }finally {
                urlConnection.disconnect()
                //Log.d("TAG1", "Disconnect")
            }
            //Log.d("TAG1", "!!Global:: : $siteTemp")
        }
        Thread.sleep(2000L)
        //Log.d("TAG1", "!!Return:: : $siteTemp")
        return siteTemp
    }
    private fun parseYa(search: String) {
        val correctSearch = search.replace(" ", "+", true)
        //Log.d("TAG1", "Start parseYa")
        val url = "https://newssearch.yandex.ru/news/search?text=$correctSearch&flat=1&sortby=date"
        //Log.d("TAG1", "parseYa url: $url")
        val siteTemp = initFromParseHTML(url)

        val doc = Jsoup.parse(siteTemp)
        val item = doc.select("article")
        newsItemList.clear()
        var testCount = 0
        //Log.d("TAG1", ":::: item: ${doc.select("article").first()}")
        //Log.d("TAG1", "item size: ${item.size}")
        item.forEach {
            //для каждого новостного элемента
            //Log.d("TAG1", "forEach val 1")
            val img = it.select("span[class=mg-snippet-source-info__agency-name]").text() ?: "Img Error"
            //Log.d("TAG1", "forEach val 2")
            val date = it.select("span[class=mg-snippet-source-info__time]").text() ?: "Date Error"
            //Log.d("TAG1", "forEach val 3")
            val title = it.select("span[role=text]")[0].text() ?: "Title Error"
            //Log.d("TAG1", "forEach val 4")
            var content = ""
            if (it.select("span[role=text]").size > 1) {
                content = it.select("span[role=text]")[1].text() ?: "Content Error"
            }
            //else val content = ""
            //Log.d("TAG1", "forEach val 5")
            val link = it.select("h3, a").attr("href") ?: "Link Error"
            val statusSaved = "false"

            val id = 0


            val newsItem = NewsItem(id, search ,img, date, title, content, link, statusSaved)
            //Log.d("TAG1", "forEach newsItem: $newsItem")
            //Log.d("TAG1", "forEach newsItemList: $newsItemList")
            newsItemList.add(newsItem)
            //Log.d("TAG1", "forEach newsItemList: $newsItemList")
            //Log.d("TAG1", "forEach Count: $testCount")
            testCount++
        }
    }
    private fun parseGoo(search: String){
        val url = "https://newssearch.yandex.ru/news/search?text=$search&flat=1" //......
        val siteTemp = initFromParseHTML(url)

        val doc = Jsoup.parse(siteTemp)
        val item = doc.select("article") //......
        //Log.d("TAG1", ":::: item: ${doc.select("article").first()}")

        // ....
    }
}

/*
//Log.d("TAG1", "!! it0: ${doc2.select("div[class=\"mg-favorites-dot__image mg-snippet__src\"]")[0]} \n") //картинки подгружаются в процессе, скорее всего поэтому тут их нет
Log.d("TAG1", "!! it2: ${it.select("span[class=mg-snippet-source-info__time]").text()} \n") //время
Log.d("TAG1", "!! it2: ${it.select("span[class=mg-snippet-source-info__agency-name]").text()} \n") //источник
Log.d("TAG1", "!! it2: ${it.select("span[role=text]")[0].text()} \n") //title - тема - краткое описание
Log.d("TAG1", "!! it2-1: ${it.select("span[role=text]")[1].text()} \n") //content - полное описание
//Log.d("TAG1", "!! it1: ${it.select("h3, span").first().attr("aria-label")} \n") //источник, запасной
Log.d("TAG1", "!! it4: ${it.select("h3, a").attr("href")} \n") //link - ссылка
//Log.d("TAG1", "!! it3: ${it.select("span[role=\"text\"]").size} \n")*/
