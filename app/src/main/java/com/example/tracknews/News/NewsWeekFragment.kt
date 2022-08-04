package com.example.tracknews.News

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.ViewModel
import com.example.tracknews.databinding.FragmentNewsWeekBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.*
import org.jsoup.select.Elements


class NewsWeekFragment : Fragment() {

    lateinit var binding: FragmentNewsWeekBinding

    var url = "https://yandex.ru/"
    var urlYaRu = "https://ya.ru/"
    val urlGoogleRu = "https://www.google.ru/"
    val urlWitcher = "https://newssearch.yandex.ru/news/search?text=%D0%B2%D0%B5%D0%B4%D1%8C%D0%BC%D0%B0%D0%BA+4"
    val urlWitcher2 = "https://newssearch.yandex.ru/news/search?text=ведьмак+4"


    var line: String = ""
    //val itemList: MutableList<String> = ArrayList()

    lateinit var docYaRu: Document
    var i = 1245
    var n = 0


    private val vm: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsWeekBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news_week, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.webSiteData.observe(activity as LifecycleOwner) {
            binding.fragNewsWeekText.text = it
        }

        vm.webSiteData.value = "0000"

        //dataModel.url.value = "124"

        //binding.fragNewsWeekWebview.loadUrl(url)
        //Log.d("TAG1", "Week Website = ${binding.fragNewsWeekWebview}")

        Log.d("TAG1", "--------------------------")



        binding.fragNewsWeekButton.setOnClickListener{
            Log.d("TAG1", "Click - Button")
            //init()
            //initReactiveX()
            initFromParseHTML()
            Log.d("TAG1", "Start -----")
        }
        /*binding.fragNewsWeekButton.setOnClickListener {
            o.subscribe({
                dataModel.messageFact.value = if (it == "check_OK") "Ok" else "Failed"
                dataModel.webSiteData.value = "124"
            },{
                dataModel.messageFact.value = "Failed"
                dataModel.webSiteData.value = "125"
            })

            dataModel.webSiteData.value = line

            if (line != "") parseHTML()
        }*/
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsWeekFragment()
    }

    private fun getWeb(){
        //Jsoup ya.ru (Neco Ru)
        Log.d("TAG1", "Start - getWeb")

        try {
            Log.d("TAG1", "getWeb -> try")

            docYaRu = Jsoup.connect("$urlYaRu").get()
            var tables: Elements = docYaRu.getElementsByTag("home-link2 informers3__all home-link2_color_gray home-link2_hover_red")


        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG1", "$e")
        }
    }

    private fun parserJsoupHTML(){
        Log.d("TAG1", "Start - parserJsoupHTML")
        Log.d("TAG1", "Tables: GO")
        Log.d("TAG1", "Tables: ${docYaRu}")
        /*
        Log.d("TAG1", "Tables: ${docYaRu.title()}")
        Log.d("TAG1", "Tables: --------------")
        Log.d("TAG1", "Div: ${docYaRu.select("article.news-search-story").size}")*/
        /*Log.d("TAG1", "Div: ${docYaRu.select("div.div.div.div.div.article.div.div").first()}")
        Log.d("TAG1", "Div: ${docYaRu.select("div.mg-snippet__wrapper")[0].text()}")
        Log.d("TAG1", "Div: ${docYaRu.select("div.mg-snippet__wrapper")[0].`val`()}")*/
        //div[@class='mg-snippet__wrapper

        //google.ru
        /*Log.d("TAG1", "Div: ${docYaRu.select("a").first().text()}")
        Log.d("TAG1", "Div: ${docYaRu.select("input.RNmpXc").first().`val`()}")
        Log.d("TAG1", "Div: ${docYaRu.select("input.RNmpXc").first().firstElementSibling().`val`()}")*/
    }

    private fun init(){
        Log.d("TAG1", "Start - init")

        fun main() {

            //coroutines
            GlobalScope.launch { // запуск новой сопрограммы в фоне
                //delay(1000L) // неблокирующая задержка на 1 секунду
                binding.fragNewsWeekText.text = "i = $i | n = $n"
                n++
                getWeb()
                //Thread.sleep(2000L)
                parserJsoupHTML()
                //Log.d("TAG1", "World!")
                binding.fragNewsWeekText.text = "i = $i | n = $n"
                i++
                //println("World!") // вывод результата после задержки
            }
            //println("Hello,") // пока сопрограмма проводит вычисления, основной поток продолжает свою работу
            //Log.d("TAG1", "Hello,")
            //Thread.sleep(2000L) // блокировка основного потока на 2 секунды, чтобы сопрограмма успела произвести вычисления
        }
        main()
    }

    private fun initFromParseHTML(){
        Log.d("TAG1", "Start - initFromParseHTML")
        fun main() {
            //coroutines
            GlobalScope.launch { // запуск новой сопрограммы в фоне
                //delay(1000L) // неблокирующая задержка на 1 секунду

                val urlConnection = URL(urlYaRu).openConnection() as HttpsURLConnection

                try {
                    urlConnection.connect()

                    if(urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("TAG1", "Good")
                        val br = BufferedReader(
                            InputStreamReader(
                                urlConnection.inputStream,
                                StandardCharsets.UTF_8
                            )
                        )
                        Log.d("TAG1", "d = ${br}")

                        line = br.readLine()
                        br.close()

                    }
                    else Log.d("TAG1", "Test Failed")
                }finally {
                    urlConnection.disconnect()
                    //dataModel.webSiteData.value = "126"
                }
                parseHTML()

                //Thread.sleep(2000L)
                //println("World!") // вывод результата после задержки
            }
            //println("Hello,") // пока сопрограмма проводит вычисления, основной поток продолжает свою работу
            //Log.d("TAG1", "Hello,")
            //Thread.sleep(2000L) // блокировка основного потока на 2 секунды, чтобы сопрограмма успела произвести вычисления
        }
        main()
        Thread.sleep(500L)
        vm.webSiteData.value = line
        Log.d("TAG1", "Hello: ${vm.webSiteData.value}")

    }

    private fun initReactiveX(){
        Log.d("TAG1", "Start - initReactiveX")
        o.subscribe({
            vm.messageFact.value = if (it == "check_OK") "Ok" else "Failed"
            vm.webSiteData.value = "124"
        },{
            vm.messageFact.value = "Failed"
            vm.webSiteData.value = "125"
        })

        vm.webSiteData.value = line

        if (line != "") parseHTML()
    }

    val o = io.reactivex.Observable.create<String>{
        //reactiveX
        //net
        //проверка соединения
        Log.d("TAG1", "Start - io.reactivex.Observable")

        val urlConnection = URL(urlYaRu).openConnection() as HttpsURLConnection

        try {
            urlConnection.connect()

            if(urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("TAG1", "Good")
                val br = BufferedReader(
                    InputStreamReader(
                        urlConnection.inputStream,
                        StandardCharsets.UTF_8
                    )
                )
                Log.d("TAG1", "d = ${br}")

                line = br.readLine()
                br.close()

            }

            else Log.d("TAG1", "Test Failed")

        }finally {
            urlConnection.disconnect()
            //dataModel.webSiteData.value = "126"
        }

    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    private fun parseHTML() {
        //Jsoup + reactiveX testHTML
        Log.d("TAG1", "Start - parseHTML")
        Log.d("TAG1", "::")
        Log.d("TAG1", ":: : ${line}")


        /*val html = ("<html><head><title>Коты учатся кодить</title>"
                + "<body><p>Коты умеют <del>ш</del>кодить.<br> Они великие программисты." +
                "<p>А еще они умеют мяукать.</p>" +
                "<a href='http://developer.alexanderklimov.ru'>Подробности здесь</a>" +
                "</body></html>")

        val doc = Jsoup.parse(html)
        Log.d("TAG1", "::: ${doc.title()}")
        val link = doc.select("a").first()
        val linkHref = link.attr("href")
        Log.d("TAG1", ":::: $linkHref")
        val linkInnerH = link.html()
        Log.d("TAG1", ":: : $linkInnerH")
        val linkOuterH = link.outerHtml()
        Log.d("TAG1", ":: :: $linkOuterH")*/

        val doc2 = Jsoup.parse(line)
        Log.d("TAG1", "::: ${doc2.title()}")

        val item = doc2.select("a").first()
        Log.d("TAG1", ":::: $item")

        Log.d("TAG1", "----------------")
        Log.d("TAG1", ":::: ${doc2.select("a").first().text()}")
        Log.d("TAG1", ":::: ${doc2.select("a").first().attr("href")}")
        Log.d("TAG1", ":::: ${doc2.select("a").first().attr("data-search-href")}")
        Log.d("TAG1", ":::: ${doc2.select("a").first().attr("data-statlog")}")
        Log.d("TAG1", ":::: ${doc2.select("a").first().html()}")
        Log.d("TAG1", ":::: ${doc2.select("a").first().outerHtml()}")


        //dataModel.webSiteData.value = doc2.html().toString()
    }

}