package com.example.tracknews

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.Services.WorkerFindNews
import com.example.tracknews.Services.WorkerFindNewsFun
import com.example.tracknews.classes.*
import com.example.tracknews.classes.FilesWorker
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME


class MainActivity : AppCompatActivity(), SearchItemAdapter.Listener {

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG

        //notification
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "channelID"

        //theme
        const val PREFS_NAME = "theme_prefs"
        const val KEY_THEME = "prefs.theme"
        const val THEME_UNDEFINED = -1
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
        const val THEME_BATTERY = 3

        //SharedPreferences
        const val SEARCH_ITEM = "search"

        //Имена файлов
        const val FILE_SEARCH_ITEM = "searchItems.json"
    }

    //важные переменные
    private lateinit var binding: ActivityMainBinding
    //var testCount1 = 0
    private val vm: ViewModel by viewModels()
    private val searchItemAdapter = SearchItemAdapter(this) //Список сохраненных поисков
    private val mainDbManager = MainDbManager(this) //База Данных (БД)
    private var parserSites = ParserSites() //парсинг
    private val sharedPrefs by lazy {getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)}
    private val filesWorker = FilesWorker(this) //работа с файлами (чтение и запись)


    //второстепенные переменные



    //@SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TrackNews) //убираем сплешскрин - меняем тему установленную в манифесте на нужную до super.onCreate
        //Log.d("TAG1", "Activity created ==============")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // ^ привязка

        //Загружаем фрагменты
        //loadFragment(R.id.frameLayoutToolbar, ToolbarFragment.newInstance())
        loadFragment(R.id.frameLayoutActivityMain, MainFragment.newInstance())
        loadFragment(R.id.frameLayoutMainFragment, NewsFragment.newInstance())
        loadFragment(R.id.fragNewsPlaceHolder, NewsTodayFragment.newInstance())

        //temp >
        /*Log.d("TAG1", "activityMain > vm.tempUrl: ${vm.tempWebsiteLink.value}")
        vm.tempWebsiteLink.observe(this){
            Log.d("TAG1", "activityMain > observe > vm.tempUrl: ${vm.tempWebsiteLink.value}")
        }*/
        //temp ^

        /*//криво определяем повернут телефон или нет
        if (vm.statusLandscape.value == "true") {
            loadFragment(R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
        }*/

        init() //стартовые функции, запуск БД и ViewModel, отслеживание изменений в них
        initThemeListener() //работаем с темой
        initTheme() //работаем с темой

        //Log.d("TAG1", "Activity >f mainDbManager.openDb")
        mainDbManager.openDb() //создаем/открываем Базу Данных (БД) SQLite
        //Log.d("TAG1", "Activity >f viewModelToSQLite")
        viewModelToSQLite() // подключаем observe
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel")
        loadSQLiteToViewModel() // загружаем БД во viewModel
        updateElementOfSQLite() // отслеживаем и удаляем элементы БД

        val buttonSearch = binding.fabButtonSearch
        val buttonSave = binding.buttonSave
        val buttonGo1 = binding.fabButtonGO
        val buttonGo = binding.buttonGO
        val cardView = binding.cardView
        val searchText = binding.editTextSearch
        val btnSavedSearches = binding.buttonSavedSearches
        val frameLayoutSavedSearches = binding.frameLayoutSavedSearches
        val animationView = AnimationView()
        var testIndex = 1

        cardView.alpha = 0F

        cardViewVisibility(cardView, buttonSearch) //отслеживаем видимость cardView


        /*
        var screenDisplayWidth = binding.frameLayoutActivityMain.layoutParams.width
        fun logWidth(){
            Log.d("TAG1", "L width: ${cardView.layoutParams.width}")
            Log.d("TAG1", "width: ${cardView.width}")
        }
        //Log.d("TAG1", "screenDisplayWidth: $screenDisplayWidth")*/

        //Delete
        binding.testFabButtonSearch2.setOnClickListener {
            if (vm.sizeFAButton == -99) {
                vm.sizeFAButton = buttonSearch.width
            }

            //val animationView = AnimationView()
            if (testIndex == 0) {
                //hide
                animationView.lateHide(cardView)
                animationView.scaleWidth(cardView, vm.sizeFAButton)
                //animationView.testUnHide(binding.testCardView)
                testIndex = 1
                Log.d("TAG1", "IF ----")
            }
            else {
                //unHide
                cardView.alpha = 0f
                cardView.animate().alpha(1F).withEndAction(Runnable {
                    //binding.testCardView.visibility = View.GONE
                    animationView.scaleWidth(cardView, -1)
                })

                //animationView.scaleWidth(binding.testCardView, -1)
                //animationView.testUnHide(binding.testCardView)
                testIndex = 0
                Log.d("TAG1", "ELSE")
            }

        }

        //Раскрываем меню поиска
        buttonSearch.setOnClickListener {
            //delete >
            vm.newsItemTempYa.value = null
            //проверка интернета
            //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()
            //запускаем парсинг новостных сайтов/сайта
            //..val newsItem = parserSites.parse("witcher")
            //Delete >>
            val newsItem = parserSites.testParse("witcher", vm.testSiteString.value.toString())
            vm.testSiteString.value = newsItem.statusEthernet
            //Delete^^
            vm.newsItemTempYa.value = newsItem.list
            //..vm.testParserSitesString.value = newsItem.statusEthernet

            //Log.d("TAG1", "Main Activity > buttonSearch > newsItem.statusEthernet ${newsItem.statusEthernet}")
            //delete^


            //Log.d("TAG1", "buttonSearch - START -------")
            //val animationView = AnimationView()
            if (vm.sizeFAButton == -99) {
                vm.sizeFAButton = buttonSearch.width
            }

            cardView.alpha = 0f
            cardView.animate().alpha(1F).withEndAction(Runnable {
                animationView.scaleWidth(cardView, -1) //раскрываем на всю ширину
                animationView.swapButton(buttonSearch, cardView, 700, 200) //меняем кнопки
                //animationView.swapButton(buttonSearch, buttonGo, 200, 200,true, -120F)
            })
            vm.statusSearchMutable.value = true.toString()
            //Log.d("TAG1", "buttonSearch - END -------")
        }

        //GO - Поиск новостей
        buttonGo.setOnClickListener {
            //Log.d("TAG1", "buttonGo - START -------")

            //Log.d("TAG1", "vm.newsItemTempYa: ${vm.newsItemTempYa.value}")
            /*if (binding.editTextSearch.text.toString() != "") {
                vm.newsItemTempYa.value = null
                //запускаем парсинг новостных сайтов/сайта
                val newsItem = parserSites.parse(binding.editTextSearch.text.toString()).list
                //полученные данные отправляем в ViewModel
                vm.newsItemTempYa.value = newsItem
            }*/

            if (binding.editTextSearch.text.toString() != "") {
                vm.newsItemTempYa.value = null
                //проверка интернета
                //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()
                //запускаем парсинг новостных сайтов/сайта
                val newsItem = parserSites.parse(binding.editTextSearch.text.toString())
                //полученные данные отправляем в ViewModel
                vm.newsItemTempYa.value = newsItem.list
                //если парсинг не удался
                if (newsItem.statusEthernet == false.toString()) {
                    val messageLoadWebsite = resources.getString(com.example.tracknews.R.string.loadWebsiteFail)
                    Toast.makeText(this, messageLoadWebsite, Toast.LENGTH_SHORT).show()
                }

            }

            /*//animation
            animationView.lateHide(cardView, 1)
            animationView.scaleWidth(cardView, vm.sizeFAButton)
            animationView.swapButton(cardView, buttonSearch, 700, 200)*/


            //animationView.swapButton(buttonGo, buttonSearch, 700, 400, true, -360F)
            //Log.d("TAG1", "buttonGo - END -------")
        }

        //временные кнопки >
        binding.actMainDrLayoutButtonSettings.setOnClickListener {

            WorkerFindNewsFun().timeDiff()
            //Worker - работа в фоне и отправка уведомлений
            workerFindNews()
        }
        binding.actMainDrLayoutButton2.setOnClickListener {
            Log.d(TAG, "Main Activity > button 2 > -------------------")
            workerJSON()
            //!!!! очистить всю БД
            /*mainDbManager.clearAllDataInDb()
            //readDbToTextView()
            loadSQLiteToViewModel()*/
            //Log.d("TAG1", "end Button2 clear ==============")
        }
        binding.actMainDrLayoutButton3.setOnClickListener {
            Log.d(TAG, "Main Activity >f button3 - Clear all Work by tag ---------------")
            WorkManager.getInstance(this).cancelAllWorkByTag(WorkerFindNews.WORKER_TAG_PARSER)
            WorkManager.getInstance(this).cancelUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER)
            //WorkManager.getInstance(this).cancelAllWork()

            /*val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            if (!vm.statusChannelNotification) {
                Log.d("TAG1", "create Channel")
                //создаем канал для уведомлений (если еще не создан)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        CHANNEL_ID, "My channel",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    channel.description = "My channel description"
                    channel.enableLights(true)
                    channel.lightColor = Color.RED
                    channel.enableVibration(false)
                    notificationManager.createNotificationChannel(channel)
                    vm.statusChannelNotification = true
                }
            }

            // Создаём уведомление
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_dark_mode)
                .setContentTitle("Напоминание")
                .setContentText("Пора покормить кота")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            //val notificationManager = NotificationManagerCompat.from(this)
            //notificationManager.notify(NOTIFICATION_ID, builder.build())

            // или
            with(NotificationManagerCompat.from(this)) {
                notify(NOTIFICATION_ID, builder.build()) // посылаем уведомление
            }*/

            /*Log.d("TAG1", "setThemeDark ---------------------------")
            //setTheme(AppCompatDelegate.MODE_NIGHT_YES, THEME_DARK)
            Log.d("TAG1", "setTheme")*/
        }
        binding.actMainDrLayoutButton4.setOnClickListener {
            //записываем данные
            Log.d(TAG, "Main Activity >f writeJSON ======START")
            var stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin"
            val dataListWorker = ArrayList<SearchItemWorker>()
            val arrayItem = stringItem.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
            arrayItem.forEach {
                //каждый элемент массива записываем в список как объекты SearchItem
                val searchItem = SearchItem(it)
                val searchItemWorker = SearchItemWorker(searchItem, 0)
                dataListWorker.add(searchItemWorker)
            }
            val data = SearchItemArrayList(dataListWorker)
            Log.d(TAG, "Main Activity >f writeJSON > dataList: $dataListWorker")

            //сериализация
            //val gson = Gson()
            //val newsItemArrayList = SearchItemArrayList(dataListWorker)
            /*val json4 = gson.toJson(newsItemArrayList)

            filesWorker.writeToFile(json4, FILE_SEARCH_ITEM, this)

            Log.d(TAG, "Main Activity >f writeJSON > json4: $json4")*/

            filesWorker.writeJSON(data, FILE_SEARCH_ITEM, this)
            Log.d(TAG, "Main Activity >f writeJSON > -------------------")
            //writeJSON()
        }
        binding.actMainDrLayoutButton5.setOnClickListener {
            //читаем данные из JSON
            /*val data = filesWorker.readJSONSearchItemArrayList(FILE_SEARCH_ITEM, this)

            data.list.forEach {
                //Log.d(TAG, "Main Activity >f writeJSON > json4: ${it.counterNewNews}")
                Log.d(TAG, "Main Activity >f writeJSON > json4: ${it.searchItem.search}")
            }
            Log.d(TAG, "Main Activity >f writeJSON > json4: ${data.list}")*/
            val findNewsItem = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, "someb") //Ищем напрямую в БД
            Log.d(TAG, "Main Activity > witcher: $findNewsItem")
        }
        //временные кнопки ^

        //выдвигаем меню настроек
        binding.buttonHamburger.setOnClickListener {

            //Log.d("TAG1", "mDrawer Button Click")
            binding.actMainDrawer.openDrawer(GravityCompat.START)
            //Log.d("TAG1", "end mDrawer Button ==============")
        }

        //раскрываем сохранненые результаты поисков
        btnSavedSearches.setOnClickListener {
            frameLayoutSavedSearches.layoutParams = frameLayoutSavedSearches.layoutParams
            //binding.frameLayoutSavedSearches.layoutParams = binding.frameLayoutSavedSearches.layoutParams
            //Log.d("TAG1", "save: ----- ${frameLayoutSavedSearches.layoutTransition} ")
            //val animationView = AnimationView()
            //(frameLayoutSavedSearches as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            //frameLayoutSavedSearches.layoutTransition.getAnimator(0)
            //Log.d("TAG1", "save: 1 ")
            if (vm.statusSavedSearchesView) {
                //startService(Intent(this, FindNews::class.java)) //запуск службы
                //Log.d("TAG1", "save: 2 ")
                //binding.frameLayoutSavedSearches.visibility = View.GONE
                //animationView.scaleHeight(binding.frameLayoutSavedSearches, 0)
                //(view as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                binding.frameLayoutSavedSearches.layoutParams.height = 0
                //Log.d("TAG1", "save: 3 ")
                btnSavedSearches.animate().rotation(0F).duration = 200
                vm.statusSavedSearchesView = false
                //Log.d("TAG1", "save: 4 ")
            }
            else {
                vm.searchItemDeleteCount.value = 0 //обновляем RcView с SearchItem
                searchItemAdapter.notifyDataSetChanged() //обновляем RcView с SearchItem
                //Log.d("TAG1", "save: 5 ")
                // -1 math_parent
                // -2 wrap_content
                binding.frameLayoutSavedSearches.layoutParams.height = -2
                btnSavedSearches.animate().rotation(180F).duration = 200
                vm.statusSavedSearchesView = true
                //Log.d("TAG1", "save: 6 ")
            }
        }

        //Log.d("TAG1", "Close program --------")
    }

    override fun onPause() {
        super.onPause()
        Log.d("TAG1", "Pause program -----------------------------------------------------------")
    }
    override fun onDestroy() {
        super.onDestroy()
        mainDbManager.closeDb() //закрываем БД (доступ к БД?)
    }

    private var backPressed: Long = 0
    override fun onBackPressed() {
        //Кнопка назад
        if (supportFragmentManager.backStackEntryCount == 0) {
            //Повтороный запрос на выход из приложения.
            val messageExitApp = resources.getString(R.string.exitApp)

            if (backPressed + 2000 > System.currentTimeMillis()) super.onBackPressed() else Toast.makeText(
                baseContext, messageExitApp,
                Toast.LENGTH_SHORT
            ).show()
            backPressed = System.currentTimeMillis()
        }
        else super.onBackPressed()
    }

    private fun init() {

        val rcView = binding.actMainRecyclerViewSavedSearches
        startRecyclerViewActMain(rcView)

        observeVM()

        //сохранение сайта (backUp)
        val sharedPrefsInit = getSharedPreferences("init", Context.MODE_PRIVATE)
        //Log.d("TAG1", "Main Activity > Init > site: ${vm.testSiteString.value.toString()}")
        //Log.d("TAG1", "Main Activity > Init > shared: ${sharedPrefsInit.getString("testSite", "Error")}")
        if (sharedPrefsInit.getString("testSite", "Error") != "true") {
            vm.testSiteString.value = sharedPrefsInit.getString("testSite", "Error")
        }
        vm.testSiteString.observe(this){
            sharedPrefsInit.edit().putString("testSite", vm.testSiteString.value.toString()).apply()
        }

        /*//скрываем/показываем кнопку поиска при возвращении с сайта
        if (binding.fabButtonSearch.visibility == View.GONE) {
            binding.fabButtonSearch.visibility = View.VISIBLE
        }*/
    }

    //searchItemList для RcView
    private fun observeVM(){
        vm.searchItemList.value?.let { searchItemAdapter.addAllSearch(it) }
        vm.searchItemList.observe(this) {
            vm.searchItemList.value?.let { it1 -> searchItemAdapter.addAllSearch(it1) }
            searchItemAdapter.notifyDataSetChanged()
        }
        //searchItemCount для кнопок удалить выделенные searchItem
        vm.searchItemDeleteCount.observe(this){
            if(vm.searchItemDeleteCount.value == 0) {
                binding.actMainSearchItemDelete.visibility = View.GONE
            }
            else binding.actMainSearchItemDelete.visibility = View.VISIBLE
        }
    }

    //Загружаем фрагмент
    private fun loadFragment(idFrameLayoutFragment: Int, fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(idFrameLayoutFragment, fragment)
            .commit()
    }

    //Worker - работа в фоне и отправка уведомлений
    private fun workerFindNews() {
        //Log.d(TAG, "Main Activity >f workerFindNews ======START")

        //Критерии
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
            .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - только WiFi
            .build()
        //Log.d(TAG, "Main Activity >f workerFindNews > constraints")

        //writeToFile(vm.testSiteString.value.toString(), "testSite.txt", this)

        //подготавливаем данные
        val dataForWorker = Data.Builder()
            .putBoolean(WorkerFindNews.WORKER_PUT_STATUS_UPDATE, false) //статус - есть ли новые новости - новых нет
            .build()

        //определяем время запуска
        val timeDiff =  WorkerFindNewsFun().timeDiff()



        //Сборка Задачи
        val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
            .addTag(WorkerFindNews.WORKER_TAG_PARSER)
            .setInitialDelay(timeDiff)
            .setConstraints(constraints)
            .setInputData(dataForWorker)
            .build()
        //Log.d(TAG, "Main Activity >f workerFindNews > myWorkRequest")

        //Запускаем новую Задачу
        WorkManager.getInstance(this)
            .enqueueUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest) //для единоразового запуска
        //WorkManager.getInstance(this).enqueue(myWorkRequest) //почему-то запускается несколько раз
        //Log.d(TAG, "Main Activity >f workerFindNews ------------END")
    }

    private fun workerFindNews1() {
        Log.d(TAG, "Main Activity >f workerFindNews ======START")
        //запускаем worker >
        var arrayStringLinkSQL = emptyArray<String>()

        vm.newsItemArray.value?.forEach {
            Log.d(TAG, "Main Activity >f workerFindNews > it: $it")
            arrayStringLinkSQL += it.link
        }

        /*val constraintsZapas = Constraints.Builder()
            .setRequiresCharging(true) //критерий: зарядное устройство должно быть подключено
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
            .setRequiredNetworkType(NetworkType.UNMETERED) //наличие интернета //только WiFi
            .setRequiresDeviceIdle(true) //девайс не используется какое-то время и ушел “в спячку”
            .setRequiresStorageNotLow(true) //на девайсе должно быть свободное место
            .build()*/

        //Критерии
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
            .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - только WiFi
            .build()
        Log.d(TAG, "Main Activity >f workerFindNews > constraints")


        writeToFile(vm.testSiteString.value.toString(), "testSite.txt", this)

        //подготавливаем старые данные из БД (link)
        val dataForWorker = Data.Builder()
            .putStringArray(WorkerFindNews.WORKER_PUT_LINK_SQL, arrayStringLinkSQL)
            .putBoolean(WorkerFindNews.WORKER_PUT_STATUS_UPDATE, vm.statusUpdateWorker)
            .build()

        //.putString(WorkerFindNews.TEST_WORKER_PUT_SITE, vm.testSiteString.value.toString()) //слишком много весит (>10240 bytes)
        Log.d(TAG, "Main Activity >f workerFindNews > dataForWorker")

        /*val myWorkRequestOne = OneTimeWorkRequest.Builder(WorkerFindNews::class.java)
            .setConstraints(constraints)
            .build()*/

        //Сборка Задачи
        val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
            .addTag(WorkerFindNews.WORKER_TAG_PARSER)
            .setConstraints(constraints)
            .setInputData(dataForWorker)
            .build()
        Log.d(TAG, "Main Activity >f workerFindNews > myWorkRequest")

        //val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>().build()
        //val  myWorkRequest = PeriodicWorkRequestBuilder<WorkerFindNews>(30, TimeUnit.MINUTES, 25, TimeUnit.MINUTES).build()

        //val myWorkRequest = OneTimeWorkRequest.Builder(WorkerFindNews::class.java).build()

        //Запускаем новую Задачу
        WorkManager.getInstance(this)
            .enqueueUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest) //для единоразового запуска
        //WorkManager.getInstance(this).enqueue(myWorkRequest) //почему-то запускается несколько раз
        //запускаем worker ^
        Log.d(TAG, "Main Activity >f workerFindNews ------------END")
    }

    private fun writeJSON() {
        //записывем данные в JSON
        Log.d(TAG, "Main Activity >f writeJSON ======START")
        var stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin"
        var searchItemList = emptyArray<SearchItemWorker>()//mutableListOf<SearchItemWorker>() //готовим список searchItem
        var searchItemListTest = emptyArray<SearchItem>()
        val dataList = ArrayList<SearchItem>()
        val dataListWorker = ArrayList<SearchItemWorker>()
        val arrayItem = stringItem.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
        arrayItem.forEach {
            //каждый элемент массива записываем в список как объекты SearchItem
            val searchItem = SearchItem(it)
            val searchItemWorker = SearchItemWorker(searchItem, 0)
            searchItemList += searchItemWorker
            searchItemListTest += SearchItem(it)
            dataList.add(searchItem)
            dataListWorker.add(searchItemWorker)
        }


        Log.d(TAG, "Main Activity >f writeJSON > searchItemList: $searchItemList")
        Log.d(TAG, "Main Activity >f writeJSON > searchItemListTest: $searchItemListTest")
        Log.d(TAG, "Main Activity >f writeJSON > dataList: $dataList")
        Log.d(TAG, "Main Activity >f writeJSON > dataList: $dataListWorker")

        //сериализация
        val gson = Gson()
        val json = gson.toJson(dataList)
        val json1 = gson.toJson(searchItemList)//gson.getAdapter(SearchItemArrayList::class.java)//gson.toJson(dataList)
        val json2 = gson.toJson(dataListWorker)
        val json3 = gson.toJson(searchItemListTest)

        val newsItemArrayList = SearchItemArrayList(dataListWorker)
        val json4 = gson.toJson(newsItemArrayList)
        //val newsItemArrayList = NewsItemArrayList(vm.newsItemArray.value)
        Log.d(TAG, "Main Activity >f writeJSON > json: $json")
        Log.d(TAG, "Main Activity >f writeJSON > json1: $json1")
        Log.d(TAG, "Main Activity >f writeJSON > json2: $json2")
        Log.d(TAG, "Main Activity >f writeJSON > json3: $json3")
        Log.d(TAG, "Main Activity >f writeJSON > json4: $json4")
        Log.d(TAG, "Main Activity >f writeJSON > -------------------")

        //десериализация
        try {
            Log.d(TAG, "Main Activity >f writeJSON > try-------")
            val users = Gson().fromJson(json4, SearchItemArrayList::class.java)
            //Log.d(TAG, "Main Activity >f workerJSON > users: $users")
            //Log.d(TAG, "Main Activity >f workerJSON > list: ${users.list}")
            users.list.forEach {
                val dateParse = it.searchItem
                Log.d(TAG, "Main Activity >f workerJSON > it: ${dateParse.search}")
            }



            val homeDateList: List<SearchItem> = gson.fromJson(json, Array<SearchItem>::class.java).toList()
            val homeDateList1: List<SearchItemWorker> = gson.fromJson(json1, Array<SearchItemWorker>::class.java).toList()
            val homeDateList2: List<SearchItemWorker> = gson.fromJson(json2, Array<SearchItemWorker>::class.java).toList()
            val homeDateList3: List<SearchItemWorker> = gson.fromJson(json3, Array<SearchItemWorker>::class.java).toList()
            Log.d(TAG, "Main Activity >f workerJSON > it: $homeDateList")
            Log.d(TAG, "Main Activity >f workerJSON > it: $homeDateList1")
            Log.d(TAG, "Main Activity >f workerJSON > it: $homeDateList2")
            Log.d(TAG, "Main Activity >f workerJSON > it: $homeDateList3")
            //val users = Gson().fromJson(json, SearchItemArrayList::class.java)
            //Log.d(TAG, "Main Activity >f workerJSON > users: $users")
            //Log.d(TAG, "Main Activity >f workerJSON > list: ${users.list}")
            homeDateList.forEach {
                val dateParse = it.search
                Log.d(TAG, "Main Activity >f workerJSON > it: $dateParse")
            }
        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: Main Activity >f workerJSON > JSONException: $e")
        }

        //Записываем текст в файл
        //writeToFile(json, FILE_SEARCH_ITEM, this)
    }

    private fun readJSON() {
        //Читаем данные из JSON
        //Читаем файл
        val searchItemListJSON = readFromFile(FILE_SEARCH_ITEM, this)

        //десериализация
        try {
            val users = Gson().fromJson(searchItemListJSON, SearchItemArrayList::class.java)

            //Log.d(TAG, "Main Activity >f workerJSON > users: $users")
            //Log.d(TAG, "Main Activity >f workerJSON > list: ${users.list}")
            users.list.forEach {
                val dateParse = it.searchItem.search
                Log.d(TAG, "Main Activity >f readJSON > it: $dateParse")
            }

        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: Main Activity >f readJSON > JSONException: $e")
        }
        return
    }

    private fun testWorkerJSON() {
        //сериализация
        val newsItemArrayList = vm.newsItemArray.value?.let { NewsItemArrayList(it) }
        //val newsItemArrayList = NewsItemArrayList(vm.newsItemArray.value)
        val gson = Gson()
        val json = gson.toJson(newsItemArrayList)

        Log.d(TAG, "Main Activity >f workerJSON > newsItemArrayList: $newsItemArrayList")

        Log.d(TAG, "Main Activity >f workerJSON > json: $json")
        Log.d(TAG, "Main Activity >f workerJSON > -------------------")

        //десериализация
        try {
            val users = Gson().fromJson(json, NewsItemArrayList::class.java)
            //Log.d(TAG, "Main Activity >f workerJSON > users: $users")
            //Log.d(TAG, "Main Activity >f workerJSON > list: ${users.list}")
            users.list.forEach {
                val dateParse = LocalDate.parse(it.date, ISO_OFFSET_DATE_TIME)
                Log.d(TAG, "Main Activity >f workerJSON > it: ${dateParse.month}")
            }
        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: Main Activity >f workerJSON > JSONException: $e")
        }
    }

    private fun workerJSON() {
        //сериализация
        val newsItemArrayList = vm.newsItemArray.value?.let { NewsItemArrayList(it) }
        val gson = Gson()
        //val testNewsItemList = vm.newsItemArray.value
        val json = gson.toJson(newsItemArrayList)

        Log.d(TAG, "Main Activity >f workerJSON > newsItemArrayList: $newsItemArrayList")

        Log.d(TAG, "Main Activity >f workerJSON > json: $json")
        Log.d(TAG, "Main Activity >f workerJSON > -------------------")


        //var newsItemList: ArrayList<NewsItem> = ArrayList()

        //десериализация
        try {
            //val obj = JSONObject(json)
            //Log.d(TAG, "Main Activity >f workerJSON > obj: $obj")
            //val userArray = obj.getJSONArray("list")

            val users = Gson().fromJson(json, NewsItemArrayList::class.java)
            //Log.d(TAG, "Main Activity >f workerJSON > users: $users")
            //Log.d(TAG, "Main Activity >f workerJSON > list: ${users.list}")
            users.list.forEach {
                val dateParse = LocalDate.parse(it.date, ISO_OFFSET_DATE_TIME)
                Log.d(TAG, "Main Activity >f workerJSON > it: ${dateParse.month}")
            }

           /* Log.d(TAG, "Main Activity >f workerJSON > userArray: $userArray")
            for (i in 0 until userArray.length()) {
                val user = userArray.getJSONObject(i)
                Log.d(TAG, "Main Activity >f workerJSON > user: user ------------")
                val date = user.getString("date")
                val dateParse = LocalDate.parse(date, ISO_OFFSET_DATE_TIME)

                val title = user.getString("title")
                val content = user.getString("content")
                Log.d(TAG, "Main Activity >f workerJSON > date: $date, timeParse: ${dateParse.month}}") //ISO_OFFSET_DATE_TIME
                Log.d(TAG, "Main Activity >f workerJSON > content: $content")
                Log.d(TAG, "Main Activity >f workerJSON > title: $title")
            }*/

        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: Main Activity >f workerJSON > JSONException: $e")
        }

        //десериализация
        //val newsItemFromJSON = gson.fromJson<ArrayList<NewsItem>>(json, ArrayList<NewsItem>())
        //Log.d(TAG, "Main Activity >f workerJSON > newsItemFromJSON: $newsItemFromJSON")
    }

    private fun writeToFile(data: String, nameFile: String,  context: Context) {
        //Записываем текст в файл
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(nameFile, MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "ERROR: Main Activity >f writeToFile > File write failed: $e")
            Log.d(TAG, "ERROR: Main Activity >f writeToFile > File write failed: $e")
        }
    }

    private fun readFromFile(nameFile: String, context: Context): String {
        //Читаем текст из файла
        var ret = ""
        try {
            val inputStream: InputStream? = context.openFileInput(nameFile)
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String? = ""
                val stringBuilder = StringBuilder()
                while (bufferedReader.readLine().also { receiveString = it } != null) {
                    stringBuilder.append("\n").append(receiveString)
                }
                inputStream.close()
                ret = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "ERROR: Main Activity >f readFromFile > File not found: $e")
            Log.d(TAG, "ERROR: Main Activity >f readFromFile > File not found: $e")
        } catch (e: IOException) {
            Log.e(TAG, "ERROR: Main Activity >f readFromFile > Can not read file: $e")
            Log.d(TAG, "ERROR: Main Activity >f readFromFile > Can not read file: $e")
        }
        return ret
    }

    //Recycler View >
    private fun uploadRcViewListSearchItem(savedSearches: String){
        //добавляем новое значение SearchItem
        val sharedPrefsRcView = getSharedPreferences("init", Context.MODE_PRIVATE)
        val stringItem = sharedPrefsRcView.getString(SEARCH_ITEM, "") //читаем сохраненную ранее строку с searchItem
        Log.d("TAG1", "Main Activity >f uploadRcViewListSearchItem > stringItem: $stringItem")
        val stringItemUpdate = "$stringItem%20:$savedSearches" //добавляем новое значение
        Log.d("TAG1", "Main Activity >f uploadRcViewListSearchItem > stringItem_Update: $stringItemUpdate")
        sharedPrefsRcView.edit().putString(SEARCH_ITEM, stringItemUpdate).apply() //обновляем сохраненную ранее строку searchItem с новым значением


        //обновляем vm чтобы подхватила RcView
        val searchItemList = mutableListOf<SearchItem>() //готовим список searchItem
        val arrayItem = stringItemUpdate.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
        arrayItem.forEach {
            //каждый элемент массива записываем в список как объекты SearchItem
            val searchItem = SearchItem(it)
            searchItemList.add(searchItem)
        }
        vm.searchItemList.value = searchItemList
    }

    private fun readRcViewListSearchItem(){
        //читаем сохраненный список SearchItem
        val sharedPrefsRcView = getSharedPreferences("init", Context.MODE_PRIVATE)

        var stringItem = sharedPrefsRcView.getString(SEARCH_ITEM, "") //читаем сохраненную ранее строку с searchItem
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > stringItem: $stringItem")
        stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin" //delete
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > stringItem: $stringItem")

        val searchItemList = mutableListOf<SearchItem>() //готовим список searchItem
        val arrayItem = stringItem.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
        arrayItem.forEach {
            //каждый элемент массива записываем в список как объекты SearchItem
            val searchItem = SearchItem(it)
            searchItemList.add(searchItem)
        }
        vm.searchItemList.value = searchItemList
        //Log.d("TAG1", "Main Activity >f readRcViewListSearchItem > searchItemList: ${vm.searchItemList.value}")
    }

    private fun startRecyclerViewActMain(view: View){
        Log.d("TAG1", "Main Activity >f startRecyclerViewActMain ======START")
        readRcViewListSearchItem()
        //Подключаем RecyclerView и отображаем данные из SQLite
        binding.apply {
            //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
            //actMainRecyclerViewSavedSearches.layoutManager = LinearLayoutManager(view.context) //проверить
            actMainRecyclerViewSavedSearches.layoutManager = GridLayoutManager(view.context, 3) //проверить
            actMainRecyclerViewSavedSearches.adapter = searchItemAdapter
        }
    }

    override fun clickOnSearchItem(searchItem: SearchItem) {
        //при клике на элемент recycler view из БД загружаются все новости с этим именем
    }

    override fun selectSearchItem(searchItem: SearchItem) {
        var a = vm.searchItemDeleteCount.value
        if (a != null) {
            a++
        }
        vm.searchItemDeleteCount.value = a
        Log.d("TAG1", "Main Activity >f selectSearchItem >  Count: ${vm.searchItemDeleteCount.value}")
    }

    override fun unSelectSearchItem(searchItem: SearchItem) {
        var a = vm.searchItemDeleteCount.value
        if (a != null) {
            a--
        }
        vm.searchItemDeleteCount.value = a
        Log.d("TAG1", "Main Activity >f unSelectSearchItem >  Count: ${vm.searchItemDeleteCount.value}")
    }
    //Recycler View ^

    // //Функции далее - работа с Базой Данных
    private fun viewModelToSQLite(){
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
        //Log.d("TAG1", "Activity >f viewModelToSQLite >  ======START")
        vm.newsItemTemp.observe(this){
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp.OBSERVE ======START")
            val search = vm.newsItemTemp.value?.search.toString()
            val img = vm.newsItemTemp.value?.img.toString()
            val date = vm.newsItemTemp.value?.date.toString()
            val title= vm.newsItemTemp.value?.title.toString()
            val content = vm.newsItemTemp.value?.content.toString()
            val link = vm.newsItemTemp.value?.link.toString()
            val statusSaved = vm.newsItemTemp.value?.statusSaved.toString()
            mainDbManager.insertToDb(search ,img, date, title, content, link, statusSaved)
            loadSQLiteToViewModel()

            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItem value: ${vm.newsItem.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp value: ${vm.newsItemTemp.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp.OBSERVE ------------END")
        }
        vm.newsItemTempYa.observe(this){
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTempYa.OBSERVE ======START")
            if (vm.newsItemTempYa.value != null) {
                vm.newsItemTempYa.value!!.forEach {
                    val search = it.search
                    val img = it.img
                    val date = it.date
                    val title = it.title
                    val content = it.content
                    val link = it.link
                    val statusSaved = it.statusSaved
                    mainDbManager.insertToDb(search ,img, date, title, content, link, statusSaved)
                }
            }
            else mainDbManager.clearAllDataInDb()

            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItem value: ${vm.newsItem.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTempYa value: ${vm.newsItemTempYa.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTempYa.OBSERVE ------------END")
            loadSQLiteToViewModel()
        }
        //Log.d("TAG1", "Activity >f viewModelToSQLite > ------------END")
    }

    /*private fun readDbToTextView(){
        //читаем базу данных и записываем в TextView
        val dataList = mainDbManager.readDbData() //создаем лист данных для тестового отображения
    }*/

    private fun loadSQLiteToViewModel(){
        //читаем Базу Данных
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > testCount1: $testCount1 ======START")
        vm.newsItemArray.value = mainDbManager.readDbData()
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > vm.newsItemArray: ${vm.newsItemArray.value}")
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel ------------END")
    }

    private fun updateElementOfSQLite(){
        //обновляем статус у новости (сохранено или нет) //удалем элемент/строку из Базы Данных
        vm.newsItemUpdateItem.observe(this) {
            val statusSaved = vm.newsItemUpdateItem.value?.statusSaved
            val id = vm.newsItemUpdateItem.value?.id
            if (statusSaved != null) {
                //mainDbManager.deleteDbElement(link, "link")
                if (id != null) {
                    mainDbManager.updateDbElementStatusSaved(statusSaved, id)
                }
            }
            Toast.makeText(this, statusSaved, Toast.LENGTH_SHORT).show()
            loadSQLiteToViewModel() //reload
        }
    }
    // //Функции выше ^ - работа с Базой Данных

    private fun cardViewVisibility(cardView: View, view: View) {
        //vm.statusSearchMutable.value = false.toString()

        val animationView = AnimationView()

        vm.statusSearchMutable.observe(this) {
            if (vm.statusSearchMutable.value == false.toString()) {
                //animation
                animationView.lateHide(cardView, 1)
                animationView.scaleWidth(cardView, vm.sizeFAButton)
                animationView.swapButton(cardView, view, 700, 200)

                //Toast.makeText(applicationContext, "Move", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun anime(view: View) {

        fun unHide(item: View){
            //анимация: показатать/проявить
            item.alpha = 0f
            item.visibility = View.VISIBLE
            item.animate().alpha(1f).duration = 200
        }
        fun hide(item: View) {
            //анимация: скрыть
            item.alpha = 1f
            item.animate().alpha(0f).duration = 100
            item.visibility = View.INVISIBLE
        }

        fun scaleWidth(item: View, sourceItem: View){
            //анимация: изменение ширины
            //item.animate().scaleXBy(1F).duration = 300
            item.layoutParams = item.layoutParams
            (item as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            //item.layoutParams.width = buttonSearch.width
            item.layoutParams.width = sourceItem.width
        }
    }


    // //Функции далее - тема (светлая и темная). Чужой код
    private fun initThemeListener(){
        //Log.d("TAG1", "MainActivity >f initThemeListener ======START")
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.themeLight -> setTheme(AppCompatDelegate.MODE_NIGHT_NO, THEME_LIGHT)
                R.id.themeDark -> setTheme(AppCompatDelegate.MODE_NIGHT_YES, THEME_DARK)
                R.id.themeBattery -> setTheme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, THEME_BATTERY)
                R.id.themeSystem -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, THEME_SYSTEM)
            }
        }
        //Log.d("TAG1", "MainActivity >f initThemeListener - OK")
    }

    private fun setTheme(themeMode: Int, prefsMode: Int) {
        //Log.d("TAG1", "MainActivity >f setTheme ======START")
        AppCompatDelegate.setDefaultNightMode(themeMode)
        //Log.d("TAG1", "1")
        saveTheme(prefsMode)
        //Log.d("TAG1", "MainActivity >f setTheme - OK")
    }

    private fun initTheme() {
        //Log.d("TAG1", "MainActivity >f initTheme ======START")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            binding.themeSystem.visibility = View.VISIBLE
        } else {
            binding.themeSystem.visibility = View.GONE
        }
        when (getSavedTheme()) {
            THEME_LIGHT -> binding.themeLight.isChecked = true
            THEME_DARK -> binding.themeDark.isChecked = true
            THEME_SYSTEM -> binding.themeSystem.isChecked = true
            THEME_BATTERY -> binding.themeBattery.isChecked = true
            THEME_UNDEFINED -> {
                when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_NO -> binding.themeLight.isChecked = true
                    Configuration.UI_MODE_NIGHT_YES -> binding.themeDark.isChecked = true
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> binding.themeLight.isChecked = true
                }
            }
        }
        //Log.d("TAG1", "MainActivity >f initTheme - OK")
    }

    /*private fun saveTheme(theme: Int) {
        Log.d("TAG1", "MainActivity >f saveTheme ======START")
        sharedPrefs.edit().putInt(KEY_THEME, theme).apply()
        Log.d("TAG1", "MainActivity >f saveTheme - OK")
    }*/
    private fun saveTheme(theme: Int) = sharedPrefs.edit().putInt(KEY_THEME, theme).apply()

    private fun getSavedTheme() = sharedPrefs.getInt(KEY_THEME, THEME_UNDEFINED)
    // //Функции Выше ^ - тема (светлая и темная). Чужой код
}

//loadFragment(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
/*supportFragmentManager
    .beginTransaction()
    .replace(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
    .addToBackStack("main")
    .commit()*/