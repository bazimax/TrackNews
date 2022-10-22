package com.example.tracknews.News

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracknews.ViewModel
import com.example.tracknews.WebsiteFragment
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentNewsAllBinding
import okhttp3.*
import java.io.IOException

class NewsAllFragment : Fragment(), NewsItemAdapter.Listener {

    lateinit var binding: FragmentNewsAllBinding
    private val vm: ViewModel by activityViewModels()
    private val newsItemAdapter = NewsItemAdapter(this)
    var okHttpClient: OkHttpClient = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsAllBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rcView = binding.fragNewsSavedRecyclerView
        startRecyclerView(rcView)

        /*Log.d("TAG1", "NewsSavedFragment > test: ${vm.testParserSitesString.value}")
        vm.testParserSitesString.observe(activity as LifecycleOwner) {
            binding.testFragNewsSavedTextView.text = vm.testParserSitesString.value
        }*/
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsAllFragment()
    }

    private fun init(){
        //запуск фрагмента

        //отслеживаем изменения в данных для RecyclerView (SQLite > ViewModel)
        vm.newsItemArrayAll.value?.let { newsItemAdapter.addAllNews(it) }
        vm.newsItemArrayAll.observe(activity as LifecycleOwner) {
            //Log.d(MainActivity.TAG, "NewsAllFragment >f newsItemArray.OBSERVE > value: ${vm.newsItemArray.value}")
            vm.newsItemArrayAll.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
        }
    }

    @SuppressLint("ClickableViewAccessibility") //для setOnTouchListener
    private fun startRecyclerView(view: View){
        //Подключаем RecyclerView и отображаем данные из SQLite
        binding.apply {
            //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
            fragNewsSavedRecyclerView.layoutManager = LinearLayoutManager(view.context) //проверить
            fragNewsSavedRecyclerView.adapter = newsItemAdapter
        }

        //Отслеживаем движение по экрану, чтобы скрывать форму поиска
        view.setOnTouchListener { _, event -> onTouch(event)}
    }

    //Отслеживаем движение по экрану, чтобы скрывать поиск
    private fun onTouch(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {}
            MotionEvent.ACTION_MOVE -> {
                if (vm.statusSearchMutable.value == true.toString()) {
                    vm.statusSearchMutable.value = false.toString()
                }
            }
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_CANCEL -> {}
        }
        return false //если поставить true, то скролл перестает работать
    }

    override fun runWebsite(newsItem: NewsItem) {
        //При клике на элемент > загрузка интеренет страницы
        //vm.tempWebsiteLink.value = newsItem.link
        //loadProgressBar(newsItem.link) //прогрессбар
        loadWebsite(newsItem.link) //загрузка интеренет страницы
        //Toast.makeText(view?.context, "test \n ${newsItem.title}", Toast.LENGTH_SHORT).show()
    }

    override fun changeStatusSaved(newsItem: NewsItem) {
        //сохраняем новость (отмечаем звездочкой)
        vm.newsItemUpdateItem.value = newsItem
    }
    override fun expandContent(newsItem: NewsItem) {
        //показываем полный текст
        //"в работе"
        //Toast.makeText(view?.context, newsItem.content, Toast.LENGTH_SHORT).show()
    }

    private fun loadWebsite(url: String) {
        vm.tempWebsiteLink.value = url
        //vm.url2 = url
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > url: $url")
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > vm.tempUrl: ${vm.tempWebsiteLink.value}")

        if (vm.statusLandscape.value == "true") {
            //загрузка интернет страницы
            //2й вариант - замена одного фрагмента на другой - WebSiteFragment
            activity!!.supportFragmentManager
                .beginTransaction()
                .replace(com.example.tracknews.R.id.frameLayoutSuperMain, WebsiteFragment.newInstance())
                .addToBackStack("main")
                .commit()
        }
        else {
            //bindingWeb.fragWebsiteWebView.loadUrl("https://www.google.ru/")
            activity!!.supportFragmentManager
                .beginTransaction()
                .replace(com.example.tracknews.R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
                .commit()
        }
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        //прогрессбар
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    private fun loadProgressBar(url: String) {
        //прогрессбар, продолжение
        val mess = resources.getString(com.example.tracknews.R.string.loadWebsiteFail)

        runOnUiThread {
            binding.fragNewsSavedProgressBar.visibility = View.VISIBLE
        }

        val request: Request = Request.Builder().url(url).build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                vm.messageLoadWebsite.value = mess
                //vm.messageFact.value = "Fail"
            }

            override fun onResponse(call: Call?, response: Response?) {
                runOnUiThread {
                    binding.fragNewsSavedProgressBar.visibility = View.GONE
                    //dataModel.messageFact.value = Html.fromHtml(txt).toString()
                    vm.messageLoadWebsite.value = "Good"
                }
            }
        })
    }
}