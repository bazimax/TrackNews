package com.example.tracknews.parseSite

import android.util.Log
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.NewsItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ParserSites {
    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG
    }

    private val newsItemList = ArrayList<NewsItem>()
    private var statusEthernet = ""

    data class ResultParse(val list: ArrayList<NewsItem>, val statusEthernet: String)

    /*fun parse(search: String): ArrayList<NewsItem>{
        val statusEthernet = parseYa(search)
        //parseGoo(search)
        return newsItemList
    }*/
    fun parse(search: String): ResultParse{
        //parseYa(search)
        //parseGoo(search)
        parseP(search)
        return ResultParse(newsItemList, statusEthernet)
    }
    fun testParse(search: String, site: String): ResultParse{
        //parseYa(search)
        //parseGoo(search)
        //Log.d("TAG1", "ParserSites >f testParse > site: $site")
        if (site == "") {
            parseP(search)
        }
        else testParseP(site)
        return ResultParse(newsItemList, statusEthernet)
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
                    //val isr = InputStreamReader(conn.getInputStream(), "windows-1251")
                    //val br = BufferedReader(isr)
                    Log.d(TAG, "Connection - Good")
                    val br = BufferedReader(
                        InputStreamReader(
                            urlConnection.inputStream,
                            "windows-1251"
                            //StandardCharsets.UTF_8
                        )
                    )
                    br.lineSequence().forEach {
                        //Log.d("TAG1", "1+ $siteTemp")
                        siteTemp += it
                    }
                    br.close()
                }
                else Log.d(TAG, "Connection - Failed")
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
    private fun parseYa(search: String):String {
        val correctSearch = search.replace(" ", "+", true)
        //Log.d("TAG1", "Start parseYa")
        //новая ссылка на дзен https://dzen.ru/news/search?issue_tld=ru&text=*&flat=1&sortby=date
        //старая ссылка на яндекс-новости "https://newssearch.yandex.ru/news/search?text=$correctSearch&flat=1&sortby=date"
        //val url = "https://newssearch.yandex.ru/news/search?text=$correctSearch&flat=1&sortby=date"
        val url = "https://dzen.ru/news/search?issue_tld=ru&text=$correctSearch"
        Log.d(TAG, "parseYa url: $url")
        val siteTemp = initFromParseHTML(url)
        Log.d(TAG, "ParserSite >f parseYa > siteTemp: $siteTemp")

        statusEthernet = true.toString() //"good"
        if (siteTemp == "") {
            statusEthernet = false.toString() //"bad"
        }

        //delete>
        statusEthernet = siteTemp //test
        //delete^

        val doc = Jsoup.parse(siteTemp)
        val item = doc.select("article") //начали парсить
        newsItemList.clear()
        //var testCount = 0
        Log.d(TAG, ":::: item: ${doc.select("article").first()}")
        Log.d(TAG, "item size: ${item.size}")
        item.forEach {
            //для каждого новостного элемента
            //Log.d("TAG1", "forEach val 1")
            val img = it.select("span[class=mg-snippet-source-info__agency-name]").text() ?: "Img Error"
            //Log.d("TAG1", "forEach val 2: $img")
            val date = it.select("span[class=mg-snippet-source-info__time]").text() ?: "Date Error"
            //Log.d("TAG1", "forEach val 3")
            val title = it.select("span[role=text]")[0].text() ?: "Title Error"
            //Log.d("TAG1", "forEach val 4")
            var content = ""
            if (it.select("span[role=text]").size > 1) {
                content = it.select("span[role=text]")[1].text() ?: "Content Error"
            }
            //Log.d("TAG1", "forEach val 5")
            val link = it.select("h3, a").attr("href") ?: "Link Error"

            val statusSaved = false.toString()
            val id = 0

            val newsItem = NewsItem(id, search ,img, date, title, content, link, statusSaved)
            //Log.d("TAG1", "forEach newsItem: $newsItem")
            //Log.d("TAG1", "forEach newsItemList: $newsItemList")
            newsItemList.add(newsItem)
            //Log.d("TAG1", "forEach newsItemList: $newsItemList")
            //Log.d("TAG1", "forEach Count: $testCount")
            //testCount++
        }
        return statusEthernet
    }

    private fun parseGoo(search: String) {
        //https://news.google.com/search?q=witcher&hl=ru&gl=RU&ceid=RU%3Aru //Ru
        //https://news.google.com/search?q=witcher&hl=en-US&gl=US&ceid=US%3Aen //En USA
        //val url = "https://www.google.ru/search?q=$correctSearch" //......
        //https://www.google.ru/search?q=witcher
        //val url = "https://news.google.com/search?q=$correctSearch&hl=ru&gl=RU&ceid=RU%3Aru" //......
        //val urlEn = "https://news.google.com/search?q=$correctSearch&hl=en-US&gl=US&ceid=US%3Aen" //......
    }

    private fun parseP(search: String):String {
        val correctSearch = search.replace(" ", "%20", true)

        //https://pikabu.ru/search?q=witcher&st=3&d=5347&D=5378
        val url = "https://pikabu.ru/search?q=$correctSearch&st=3&d=5347&D=5378" //......


        //Log.d("TAG1", "parseP url: $url")
        val siteTemp = initFromParseHTML(url)
        //Log.d("TAG1", "ParserSite >f parseP > siteTemp: $siteTemp")

        statusEthernet = true.toString() //"good"
        if (siteTemp == "") {
            statusEthernet = false.toString() //"bad"
        }

        val doc = Jsoup.parse(siteTemp)
        val item = doc.select("article") //начали парсить
        newsItemList.clear()

        //delete>
        //statusEthernet = doc.toString() //test
        statusEthernet = siteTemp//doc.toString() //test
        Log.d(TAG, "ParserSites >f parseP > statusEthernet: $statusEthernet")
        //delete^

        //var testCount = 0
        //Log.d("TAG1", ":::: item 1: ${doc.select("story__main").first()}")
        //Log.d("TAG1", ":::: item 2: ${item}")
        Log.d(TAG, "ParserSites >f parseP > item size: ${item.size}")
        item.forEach {
            /*Log.d("TAG1", "!! it0 size: ${it.select("div.story__content-inner").size}") //
            Log.d("TAG1", "!! it0-1 size: ${it.select("div.story-block_type_text").size}") //
            */

            //для каждого новостного элемента
            //Log.d("TAG1", "forEach val 1")
            val img = ""//it.select("href").first().toString() ?: "Img Error"
            //Log.d("TAG1", "forEach val 2: $img")
            val date = it.select("time").attr("datetime") ?: "Date Error" //time (full)
            //Log.d("TAG1", "!! it1: ${it.select("time").text()}") //time text
            //Log.d("TAG1", "forEach val 3")
            val title = it.select("header.story__header").text() ?: "Title Error" //title
            //Log.d("TAG1", "forEach val 4")
            var content = ""
            if (it.select("div.story-block_type_text").size > 0) {
                content = it.select("div.story-block_type_text").text() ?: "Content Error" // content
            }
            //Log.d("TAG1", "forEach val 5")
            val link = it.select("header.story__header").select("a").attr("href") ?: "Link Error" //link

            val statusSaved = false.toString()
            val id = 0

            val newsItem = NewsItem(id, search ,img, date, title, content, link, statusSaved)
            //Log.d("TAG1", "forEach newsItem: $newsItem")
            //Log.d("TAG1", "forEach newsItemList: $newsItemList")
            newsItemList.add(newsItem)
            //Log.d("TAG1", "forEach newsItemList: $newsItemList")
            //Log.d("TAG1", "forEach Count: $testCount")
            //testCount++
        }
        Log.d(TAG, "==================================================================") //
        return statusEthernet
    }

    private fun baidu(){
        //https://www.baidu.com/s?ie=utf-8&f=3&rsv_bp=1&rsv_idx=1&tn=baidu&wd=witcher3
    }

    private fun testParseP(site: String):String {
        //чтобы при тестах лишний раз не парсить
        statusEthernet = true.toString() //"good"
        if (site == "") {
            statusEthernet = false.toString() //"bad"
        }
        statusEthernet = site

        val doc = Jsoup.parse(site)
        val item = doc.select("article") //начали парсить
        newsItemList.clear()

        //Log.d(TAG, "item size: ${item.size}")
        item.forEach {
            //для каждого новостного элемента
            val img = ""//it.select("href").first().toString() ?: "Img Error"
            val date = it.select("time").attr("datetime") ?: "Date Error"
            val title = it.select("header.story__header").text() ?: "Title Error"
            var content = ""
            if (it.select("div.story-block_type_text").size > 0) {
                content = it.select("div.story-block_type_text").text() ?: "Content Error"
            }
            val link = it.select("header.story__header").select("a").attr("href") ?: "Link Error"

            val statusSaved = false.toString()
            val id = 0
            val search = "123"


            val newsItem = NewsItem(id, search ,img, date, title, content, link, statusSaved)

            newsItemList.add(newsItem)

        }
        Log.d(TAG, "ParserSites >f testParseP ==================================================================") //
        return statusEthernet
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
