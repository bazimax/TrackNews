package com.example.tracknews.News

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MotionEventCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tracknews.ViewModel
import com.example.tracknews.WebsiteFragment
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentNewsSavedBinding
import okhttp3.*
import java.io.IOException


class NewsSavedFragment : Fragment(), NewsItemAdapter.Listener {

    lateinit var binding: FragmentNewsSavedBinding
    private val vm: ViewModel by activityViewModels()
    private val newsItemAdapter = NewsItemAdapter(this)
    //private var parserSites = ParserSites()

    var okHttpClient: OkHttpClient = OkHttpClient()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { /*View?*/
        binding = FragmentNewsSavedBinding.inflate(inflater)
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

        Log.d("TAG1", "NewsSavedFragment > test: ${vm.testParserSitesString.value}")
        vm.testParserSitesString.observe(activity as LifecycleOwner) {
            binding.testFragNewsSavedTextView.text = vm.testParserSitesString.value
        }

        //binding.fragNewsSavedRecyclerView.setOnTouchListener { _, event -> onTouch(event)}

        /*binding.testFragNewsSavedGO.setOnClickListener {
            //Log.d("TAG1", "Click - Button")
            vm.newsItemTempYa.value = null
            vm.newsItemTempYa.value = parserSites.parse(binding.testFragNewsSavedEditText.text.toString())
            //Log.d("TAG1", "vm.newsItemTempYa: ${binding.testFragNewsSavedEditText.text}")
            //Log.d("TAG1", "vm.newsItemTempYa: ${vm.newsItemTempYa.value}")
            //Log.d("TAG1", "End button click -----")
        }*/
    }

    companion object {

        @JvmStatic
        fun newInstance() = NewsSavedFragment()
    }

    private fun init(){
        //запуск фрагмента

        //отслеживаем изменения в данных для RecyclerView (SQLite > ViewModel)
        vm.newsItemArray.value?.let { newsItemAdapter.addAllNews(it) }
        vm.newsItemArray.observe(activity as LifecycleOwner) {
            vm.newsItemArray.value?.let { it1 -> newsItemAdapter.addAllNews(it1) }
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

        //Отслеживаем движение по экрану, чтобы скрывать поиск
        /*view.setOnTouchListener { _, event ->
            return@setOnTouchListener when (MotionEventCompat.getActionMasked(event)) {
                //MotionEventCompat.getActionMasked(event)
                MotionEvent.ACTION_DOWN -> {

                    // Make a Toast when movements captured on the sub-class
                    //Toast.makeText(context, "Move", Toast.LENGTH_SHORT).show()
                    if (vm.statusSearchMutable.value == true.toString()) {
                        vm.statusSearchMutable.value = false.toString()
                    }
                    true
                }
                else -> false
            }
        }*/
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

    /*private fun onTouchZapas(event: MotionEvent): Boolean {
        var x = event.x//event.getX()
        var y = event.y//event.getY()

        var sDown = ""
        var sMove = ""
        var sUp = ""

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                sDown = "Down: $x,$y"
                sMove = ""
                sUp = ""
                if (vm.statusSearchMutable.value == true.toString()) {
                    vm.statusSearchMutable.value = false.toString()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (vm.statusSearchMutable.value == true.toString()) {
                    vm.statusSearchMutable.value = false.toString()
                }
                //sMove = "Move: $x,$y";
            }
            MotionEvent.ACTION_UP -> {}
            MotionEvent.ACTION_CANCEL -> {
                sMove = "";
                sUp = "Up: $x,$y";
            }
        }
        //binding.testFragNewsSavedTextView.text = sDown + "\n" + sMove + "\n" + sUp
        return true
    }*/

    override fun runWebsite(newsItem: NewsItem) {
        //При клике на элемент > загрузка интеренет страницы
        //vm.tempWebsiteLink.value = newsItem.link
        //loadProgressBar(newsItem.link) //прогрессбар
        loadWebsite(newsItem.link) //загрузка интеренет страницы
        //Toast.makeText(view?.context, "test \n ${newsItem.title}", Toast.LENGTH_SHORT).show()
    }

    override fun changeStatusSaved(newsItem: NewsItem) {
        //сохраняем новость (отмечаем звездочкой)
        // //пока тестово удаляем
        // //пока тестово меняем ссылку(link)

        /*vm.newsItemWorkId.value = newsItem.id
        vm.newsItemWork.value = newsItem.statusSaved*/
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > updateItem: ${vm.newsItemUpdateItem.value}")
        //var a = NewsItem()
        //a.id = newsItem.id
        vm.newsItemUpdateItem.value = newsItem


        //vm.newsItemUpdateItem.value?.id = newsItem.id
        //vm.newsItemUpdateItem.value?.statusSaved = newsItem.statusSaved
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > updateItem: ${vm.newsItemUpdateItem.value}")

        //передаем статус
        /*if (newsItem.statusSaved == false.toString()) {
            vm.newsItemWork.value = true.toString()
            Log.d("TAG1", "fragNewsSaved >f loadWebsite > url: ${vm.newsItemWork.value}")
        }
        else {
            vm.newsItemWork.value = false.toString()
            Log.d("TAG1", "fragNewsSaved >f loadWebsite > url: ${vm.newsItemWork.value}")
        }*/
        //vm.newsItemDeleted.value = newsItem.statusSaved
        //Toast.makeText(view?.context, newsItem.statusSaved, Toast.LENGTH_SHORT).show()
    }
    override fun expandContent(newsItem: NewsItem) {
        //показываем полный текст
        //"в работе"
        Toast.makeText(view?.context, newsItem.content, Toast.LENGTH_SHORT).show()
    }

    private fun loadWebsite(url: String) {
        vm.tempWebsiteLink.value = url
        vm.url2 = url
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