package com.example.tracknews.parseSite

import android.content.Context
import android.util.Log
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.FilesWorker
import com.example.tracknews.classes.NewsItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val logNameClass = "ParserSites" //для логов

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
        const val TAG_DATA_IF = Constants.TAG_DATA_IF //переменные и данные в циклах
    }

    private val newsItemList = ArrayList<NewsItem>()
    private var statusEthernet = ""
    data class ResultParse(val list: ArrayList<NewsItem>, val statusEthernet: String)

    fun parse(search: String, context: Context): ResultParse{
        Log.d(TAG_DEBUG, "$logNameClass >f parse === START")

        parseP(search, context)

        Log.d(TAG_DEBUG, "$logNameClass >f parse ----- END")
        Log.d(TAG_DATA_BIG, "$logNameClass >f parse > statusEthernet: $statusEthernet\n- newsItemList: $newsItemList")
        return ResultParse(newsItemList, statusEthernet)
    }


    private fun initFromParseHTML(url: String): String {
        Log.d(TAG_DEBUG, "$logNameClass >f initFromParseHTML === START")
        Log.d(TAG_DEBUG, "$logNameClass >f initFromParseHTML // открываем интернет страницу")
        Log.d(TAG_DATA, "$logNameClass >f initFromParseHTML > url: $url")
        var siteTemp = ""
        //coroutines
        CoroutineScope(Dispatchers.IO).launch { // запуск новой сопрограммы в фоне

            val urlConnection = URL(url).openConnection() as HttpsURLConnection

            try {
                urlConnection.connect()

                if(urlConnection.responseCode == HttpURLConnection.HTTP_OK) {

                    Log.d(TAG, "$logNameClass >f initFromParseHTML > Connection - Good")
                    val br = BufferedReader(
                        InputStreamReader(
                            urlConnection.inputStream,
                            "windows-1251" //кодировка сайта у Пикабу
                            //StandardCharsets.UTF_8
                        )
                    )
                    br.lineSequence().forEach {
                        siteTemp += it
                    }
                    br.close()
                }
                else Log.d(TAG, "$logNameClass >f initFromParseHTML > Connection - Failed")
            }catch (e: IOException) {
                e.printStackTrace()
            }finally {
                urlConnection.disconnect()
            }
        }

        Thread.sleep(2000L)
        if (siteTemp == "") {
            Log.d(TAG, "$logNameClass >f initFromParseHTML > Connection - Wait More")
            Thread.sleep(6000L)
        }
        return siteTemp
    }

    private fun parseP(search: String, context: Context):String {
        Log.d(TAG_DEBUG, "$logNameClass >f parseP === START")
        Log.d(TAG_DATA, "$logNameClass >f parseP > search: $search")
        val correctSearch = search.replace(" ", "%20", true)
        Log.d(TAG_DATA, "$logNameClass >f parseP > correctSearch: $correctSearch")

        val url = "https://pikabu.ru/search?q=$correctSearch&r=4" //рейтинг от 100
        //https://pikabu.ru/search?q=witcher&d=5347&D=5378 - по дате
        //https://pikabu.ru/search?q=witcher&st=2&d=5347&D=5378 - по рейтингу
        //https://pikabu.ru/search?q=witcher&st=3&d=5347&D=5378 - по релевантности (по умолчанию)
        //https://pikabu.ru/search?q=witcher&r=4 - рейтинг от 100


        val siteTemp = initFromParseHTML(url)

        //записываем полученный сайт в файл (для тестов)
        FilesWorker().writeToFile(siteTemp, Constants.FILE_TEST_LOAD_SITE, context)
        Log.d(TAG_DATA, "$logNameClass >f parseP > siteTemp: $siteTemp")

        statusEthernet = true.toString() //"good"
        if (siteTemp == "") {
            statusEthernet = false.toString() //"bad"
        }
        Log.d(TAG_DATA, "$logNameClass >f parseP > statusEthernet: $statusEthernet")

        val doc = Jsoup.parse(siteTemp)
        val item = doc.select("article") //начали парсить
        newsItemList.clear()

        Log.d(TAG_DATA_BIG, "$logNameClass >f parseP > item: $item")
        Log.d(TAG_DATA, "$logNameClass >f parseP > item size: ${item.size}")
        item.forEach {
            //для каждого новостного элемента
            val id = 0
            val img = ""
            val date = it.select("time").attr("datetime") ?: "Date Error" //time (full)
            val title = it.select("a.story__title-link").text() ?: "Title Error" //title
            var content = ""
            if (it.select("div.story-block_type_text").size > 0) {
                content = it.select("div.story-block_type_text").text() ?: "Content Error" // content
            }
            val link = it.select("header.story__header").select("a").attr("href") ?: "Link Error" //link

            val statusSaved = false.toString()

            val newsItem = NewsItem(id, search ,img, date, title, content, link, statusSaved)

            //если есть ссылка
            if (newsItem.link.contains("https://")) {
                //если есть дата
                if (date != "") {
                    newsItemList.add(newsItem)

                    Log.d(TAG_DATA_IF, "$logNameClass >f ParseP > item.forEach:\n" +
                            "- id: $id\n" +
                            "- search: $search\n" +
                            "- img: $img\n" +
                            "- date: $date\n" +
                            "- title: $title\n" +
                            "- content: $content\n" +
                            "- link: $link\n" +
                            "- statusSaved: $statusSaved")
                }
            }
        }
        Log.d(TAG, "$logNameClass >f parseP END ==================================================================")
        return statusEthernet
    }
}