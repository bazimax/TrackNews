package com.example.tracknews

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.services.WorkerFindNews
import com.example.tracknews.services.WorkerFindNewsFun
import com.example.tracknews.classes.*
import com.example.tracknews.classes.FilesWorker
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.parseSite.ParserSites
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), SearchItemAdapter.Listener {

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG

        //notification
        const val NOTIFICATION_ID = Constants.NOTIFICATION_ID//101
        const val CHANNEL_ID = Constants.CHANNEL_ID//"channelID"

        //theme
        const val PREFS_NAME = Constants.PREFS_NAME//"theme_prefs"
        const val KEY_THEME = Constants.KEY_THEME//"prefs.theme"
        const val THEME_UNDEFINED = Constants.THEME_UNDEFINED//-1
        const val THEME_LIGHT = Constants.THEME_LIGHT//0
        const val THEME_DARK = Constants.THEME_DARK//1
        const val THEME_SYSTEM = Constants.THEME_SYSTEM//2
        const val THEME_BATTERY = Constants.THEME_BATTERY//3

        //SharedPreferences
        const val SEARCH_ITEM = Constants.SEARCH_ITEM//"search"

        //Имена файлов
        const val FILE_SEARCH_ITEM = Constants.FILE_SEARCH_ITEM//"searchItems.json"
    }

    //важные переменные
    private lateinit var binding: ActivityMainBinding

    private val vm: ViewModel by viewModels()
    private val searchItemAdapter = SearchItemAdapter(this) //список сохраненных поисков
    private val mainDbManager = MainDbManager(this) //База Данных (БД)
    private var parserSites = ParserSites() //парсинг
    private val sharedPrefs by lazy {getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)}
    private val filesWorker = FilesWorker() //работа с файлами (чтение и запись)

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
        //viewModelToSQLite() // подключаем observe

        observeVM() //observeVM(vmFunctions) // подключаем observe

        //Delete??
        //FilesWorker().checkStatusFirstLaunch(this) //проверка - первый ли это запуск. Для инструкции


        val buttonSearch = binding.fabButtonSearch
        val buttonSave = binding.buttonSave
        val buttonGo1 = binding.fabButtonGO
        val buttonGo = binding.buttonGO
        val cardView = binding.cardView
        val searchText = binding.editTextSearch
        val btnSavedSearches = binding.buttonSavedSearches
        val frameLayoutSavedSearches = binding.frameLayoutSavedSearches
        val textViewSavedSearchActive = binding.textViewSavedSearchActive
        val animationView = AnimationView()
        var testIndex = 1

        cardView.alpha = 0F

        cardViewVisibility(cardView, buttonSearch) //отслеживаем видимость cardView


        //Delete
        /*
        var screenDisplayWidth = binding.frameLayoutActivityMain.layoutParams.width
        fun logWidth(){
            Log.d("TAG1", "L width: ${cardView.layoutParams.width}")
            Log.d("TAG1", "width: ${cardView.width}")
        }
        //Log.d("TAG1", "screenDisplayWidth: $screenDisplayWidth")*/

        //раскрываем меню поиска
        buttonSearch.setOnClickListener {
            //Log.d("TAG1", "buttonSearch - START -------")
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

        //GO - поиск новостей
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
            val search = binding.editTextSearch.text.toString()
            //??
            ViewModelFunctions(vm).searchNews(search, this)
            //vmFunctions.searchNews(search, this)

            /*//?? Test
            if (search != "") {
                //проверка интернета
                //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()
                //запускаем парсинг новостных сайтов/сайта
                val resultParse = parserSites.parse(binding.editTextSearch.text.toString())

                //если парсинг не удался
                if (resultParse.statusEthernet == false.toString()) {
                    val messageLoadWebsite = resources.getString(com.example.tracknews.R.string.loadWebsiteFail)
                    Toast.makeText(this, messageLoadWebsite, Toast.LENGTH_SHORT).show()
                }
                else {
                    vm.newsItemTempYa.value = null
                    //??
                    //полученные данные отправляем в ViewModel
                    vm.newsItemTempYa.value = resultParse.list
                }
            }*/

            /*//animation
            animationView.lateHide(cardView, 1)
            animationView.scaleWidth(cardView, vm.sizeFAButton)
            animationView.swapButton(cardView, buttonSearch, 700, 200)*/


            //animationView.swapButton(buttonGo, buttonSearch, 700, 400, true, -360F)
            //Log.d("TAG1", "buttonGo - END -------")
        }

        //save - сохраняем поисковой запрос (из строки поиска)
        buttonSave.setOnClickListener {
            val search = binding.editTextSearch.text.toString()
            ViewModelFunctions(vm).saveSearch(search, this)
        }

        //удалить выбранные "сохраненные поиски" и удалить из БД все новости с ними связанные
        binding.searchItemButtonDelete.setOnClickListener {
            ViewModelFunctions(vm).deleteSelectSearchItemAndNews(mainDbManager, searchItemAdapter,this)
            //searchItemAdapter.notifyDataSetChanged() //обновляем RcView с SearchItem
        }

        //отменить выделение "сохраненных поисков"
        binding.searchItemButtonCancel.setOnClickListener {
            ViewModelFunctions(vm).cancelSelectSearchItem(searchItemAdapter)
            /*vm.searchItemDeleteCount.value = 0 //скрываем кнопки
            vm.searchItemDeleteArrayList.value = ArrayList() //очищаем список на удаление
            searchItemAdapter.notifyDataSetChanged() //обновляем RcView с SearchItem*/
        }

        //временные кнопки >
        binding.actMainDrLayoutButtonSettings.setOnClickListener {
            ViewModelFunctions(vm).sortSearchItemArrayList(this)
            //??
            // WorkerFindNewsFun().timeDiff()
            //Worker - работа в фоне и отправка уведомлений
            //??
            //WorkerFindNewsFun().workerFindNewsFirst(this)
            //workerFindNews()

        }
        binding.actMainDrLayoutButton2.setOnClickListener {
            Log.d(TAG, "Main Activity > button 2 > -------------------")


            //delete > //обновление БД test
            val siteTest = FilesWorker().readFromFile("testSite.txt", this)
            vm.newsItemTempArrayInBd.value = null
            //проверка интернета
            //val testRequest: Request = Request.Builder().url("https://www.ya.ru/").build()
            //запускаем парсинг новостных сайтов/сайта
            //..val newsItem = parserSites.parse("witcher")
            //Delete >>
            Log.d(TAG, "MainActivity > buttonSearch > statusEthernet $siteTest")
            val resultParse = parserSites.testParse("witcher", siteTest)

            //vm.testSiteString.value = resultParse.statusEthernet
            //Delete^^
            vm.newsItemTempArrayInBd.value = resultParse.list
            Log.d(TAG, "MainActivity > buttonSearch > statusEthernet ${resultParse.statusEthernet}")
            Log.d(TAG, "MainActivity > buttonSearch > newsItemTempArrayInBd ${vm.newsItemTempArrayInBd.value}")
            //..vm.testParserSitesString.value = newsItem.statusEthernet

            //Log.d("TAG1", "Main Activity > buttonSearch > newsItem.statusEthernet ${newsItem.statusEthernet}")
            //delete^
            //workerJSON()
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
            //BACKUP >
            //записываем данные
            Log.d(TAG, "Main Activity >f writeJSON ======START")
            val stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin"
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
            //BACKUP ^
            //writeJSON()
        }
        binding.actMainDrLayoutButton5.setOnClickListener {
            val testSiteString = filesWorker.readFromFile("testSite.txt", this)
            Log.d(WorkerFindNews.TAG, "WorkerFindNews >f doWork > try > testSiteString: $testSiteString")
            Log.d(WorkerFindNews.TAG, "WorkerFindNews >f doWork > try > testSiteString: ${vm.testSiteString.value.toString()}")

            //читаем данные из JSON
            /*val data = filesWorker.readJSONSearchItemArrayList(FILE_SEARCH_ITEM, this)

            data.list.forEach {
                //Log.d(TAG, "Main Activity >f writeJSON > json4: ${it.counterNewNews}")
                Log.d(TAG, "Main Activity >f writeJSON > json4: ${it.searchItem.search}")
            }
            Log.d(TAG, "Main Activity >f writeJSON > json4: ${data.list}")*/
            //val findNewsItem = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, "someb") //Ищем напрямую в БД
            //Log.d(TAG, "Main Activity > witcher: $findNewsItem")
        }
        //временные кнопки ^

        //выдвигаем меню настроек
        binding.buttonHamburger.setOnClickListener {

            //Log.d("TAG1", "mDrawer Button Click")
            binding.actMainDrawer.openDrawer(GravityCompat.START)
            //Log.d("TAG1", "end mDrawer Button ==============")
        }

        //раскрываем список "сохранненых поисков (подписок)"
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
                vm.searchItemDeleteCount.value = 0 //скрываем кнопки
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
        Log.d("TAG1", "Pause program ------------------------------------------------------Start")
        //сохраняем активный SearchItem на время паузы приложения
        //ViewModelFunctions(vm).saveSearchItemActive(this)

        //сбрасываем активный SearchItem на позицую 0
        //ViewModelFunctions(vm).resetSearchItemActive(this)
        Log.d("TAG1", "Pause program ------------------------------------------------------End")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("TAG1", "Destroy program ------------------------------=======================Start")
        //сбрасываем активный SearchItem на позицую 0
        ViewModelFunctions(vm).resetSearchItemActive(this)


        //??при изменении темы происходит destroy и restart
        //сохраняем активный SearchItem на время паузы приложения
        //ViewModelFunctions(vm).saveSearchItemActive(this)

        mainDbManager.closeDb() //закрываем БД (доступ к БД?)
        Log.d("TAG1", "Destroy program ------------------------------=======================End")
    }

    /*override fun onResume() {
        super.onResume()
        //сбрасываем активный SearchItem на позицую 0
        ViewModelFunctions(vm).selectSearchItemActive(this)
        Log.d("TAG1", "Resume program ------------------------------=============>>>>")
        //ViewModelFunctions(vm).resetSearchItemActive(this)
    }*/

    /*override fun onRestart() {
        super.onRestart()
        //Log.d("TAG1", "Restart program ------------------------------=============>>>> Start")
        ViewModelFunctions(vm).selectSearchItemActive(this)
        //Log.d("TAG1", "Restart program ------------------------------=============>>>> End")
    }*/

    //кнопка назад
    private var backPressed: Long = 0
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            //повтороный запрос на выход из приложения.
            val messageExitApp = resources.getString(R.string.exitApp)

            if (backPressed + 2000 > System.currentTimeMillis()) super.onBackPressed() else Toast.makeText(
                baseContext, messageExitApp,
                Toast.LENGTH_SHORT
            ).show()
            backPressed = System.currentTimeMillis()
        }
        else super.onBackPressed()
    }

    //стартовые функции
    private fun init() {
        //BACKUP//generateSearchItemArrayList() //

        //первый запуск - изменение статуса


        val rcView = binding.actMainRecyclerViewSavedSearches

        Log.d(TAG_DEBUG, "Main Activity >f startRecyclerViewActMain ======START")
        ViewModelFunctions(vm).readSearchItemListToRcView(this)

        //подключаем RecyclerView и отображаем данные из SQLite
        binding.apply {
            //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
            //actMainRecyclerViewSavedSearches.layoutManager = LinearLayoutManager(view.context) //проверить
            actMainRecyclerViewSavedSearches.layoutManager = GridLayoutManager(rcView.context, 3) //проверить
            actMainRecyclerViewSavedSearches.adapter = searchItemAdapter
        }

        //выбираем активный SearchItem
        ViewModelFunctions(vm).selectSearchItemActive(this)
        /*//загружаем список "сохраненных поисков" (SearchItem) для поиска новых новстей -> читаем данные из JSON
        val searchItemArrayList = filesWorker.readJSONSearchItemArrayList(FILE_SEARCH_ITEM, this)
        searchItemArrayList.list.forEach {
            if (it.searchItem.active) {
                vm.searchItemActive.value = it.searchItem.search

            }
        }*/



        //Worker - работа в фоне и отправка уведомлений
        //??
        //WorkerFindNewsFun().workerFindNewsFirst(this)

        //Delete??
        /*//скрываем/показываем кнопку поиска при возвращении с сайта
        if (binding.fabButtonSearch.visibility == View.GONE) {
            binding.fabButtonSearch.visibility = View.VISIBLE
        }*/
    }

    //отслеживание изменений в ViewModel
    //private fun observeVM(vmFunctions: ViewModelFunctions){
    private fun observeVM(){
        //основная логика приложения завязанная на ViewModel
        ViewModelFunctions(vm).observeVM(mainDbManager, this, this)

        //View логика MainActivity завязанная на ViewModel
        //searchItemList для RcView
        vm.searchItemList.value?.let { searchItemAdapter.addAllSearch(it) }
        vm.searchItemList.observe(this) {
            Log.d(TAG, "MainActivity >f searchItemList.OBSERVE ${vm.searchItemList.value}")
            vm.searchItemList.value?.let { it1 -> searchItemAdapter.addAllSearch(it1) }
            searchItemAdapter.notifyDataSetChanged() //??!!
        }

        //скрытие кнопок "удалить выделенные searchItem"
        vm.searchItemDeleteCount.observe(this){
            if(vm.searchItemDeleteCount.value == 0) {
                binding.actMainSearchItemDelete.visibility = View.GONE
            }
            else binding.actMainSearchItemDelete.visibility = View.VISIBLE
        }

        //отображение имени активного "сохраненного поиска" - textViewSavedSearchActive
        vm.searchItemActive.observe(this){
            val textFormat = vm.searchItemActive.value.toString().replaceFirstChar { it.titlecase() }
            binding.textViewSavedSearchActive.text = textFormat
        }
    }

    //загружаем фрагмент
    private fun loadFragment(idFrameLayoutFragment: Int, fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(idFrameLayoutFragment, fragment)
            .commit()
    }

    //Worker - работа в фоне и отправка уведомлений
    //Delete
    /*private fun workerFindNews() {
        Log.d(TAG_DEBUG, "MainActivity >f workerFindNews ======START")

        //критерии
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

        //сборка Задачи
        val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
            .addTag(WorkerFindNews.WORKER_TAG_PARSER)
            .setInitialDelay(timeDiff)
            .setConstraints(constraints)
            .setInputData(dataForWorker)
            .build()
        //Log.d(TAG, "Main Activity >f workerFindNews > myWorkRequest")

        //запускаем новую Задачу
        WorkManager.getInstance(this)
            .enqueueUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest) //для единоразового запуска
        //WorkManager.getInstance(this).enqueue(myWorkRequest) //почему-то запускается несколько раз
        Log.d(TAG_DEBUG, "Main Activity >f workerFindNews ------------END")
    }*/


    //Recycler View >
    //при клике на элемент searchItem в recycler view -> он становится активным -> из БД загружаются все новости с этим именем
    override fun clickOnSearchItem(searchItem: SearchItem) {
        ViewModelFunctions(vm).clickOnSearchItem(searchItem)
    }

    //длинное нажатие для выделения этого searchItem и его удаления
    override fun selectSearchItem(searchItem: SearchItem) {
        ViewModelFunctions(vm).selectSearchItem(searchItem)
    }

    //длинное нажатие на выделенном элементе для отмены выделения
    override fun unSelectSearchItem(searchItem: SearchItem) {
        ViewModelFunctions(vm).unSelectSearchItem(searchItem)
    }
    //Recycler View ^

    private fun cardViewVisibility(cardView: View, view: View) {

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

    // //функции далее -> тема (светлая и темная). Чужой код
    private fun initThemeListener(){
        Log.d(TAG_DEBUG, "MainActivity >f initThemeListener ======START")
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
        Log.d(TAG_DEBUG, "MainActivity >f initTheme - OK")
    }

    private fun saveTheme(theme: Int) = sharedPrefs.edit().putInt(KEY_THEME, theme).apply()

    private fun getSavedTheme() = sharedPrefs.getInt(KEY_THEME, THEME_UNDEFINED)
    // //функции Выше ^ -> тема (светлая и темная). Чужой код

    //BACKUP
    fun generateSearchItemArrayList(){
        //BACKUP >
        //записываем данные
        Log.d(TAG, "Main Activity >f writeJSON ======START")
        val stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin"
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
        //BACKUP ^
    }

    private fun uploadRcViewListSearchItem(savedSearches: String){
        //добавляем новое значение SearchItem
        val sharedPrefsRcView = getSharedPreferences("init", Context.MODE_PRIVATE)
        val stringItem = sharedPrefsRcView.getString(SEARCH_ITEM, "") //читаем сохраненную ранее строку с searchItem
        //Log.d(TAG, "Main Activity >f uploadRcViewListSearchItem > stringItem: $stringItem")
        val stringItemUpdate = "$stringItem%20:$savedSearches" //добавляем новое значение
        //Log.d(TAG, "Main Activity >f uploadRcViewListSearchItem > stringItem_Update: $stringItemUpdate")
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

    //сохранение сайта (backUp)
    /*val sharedPrefsInit = getSharedPreferences("init", Context.MODE_PRIVATE)
    //Log.d("TAG1", "Main Activity > Init > site: ${vm.testSiteString.value.toString()}")
    //Log.d("TAG1", "Main Activity > Init > shared: ${sharedPrefsInit.getString("testSite", "Error")}")
    if (sharedPrefsInit.getString("testSite", "Error") != "true") {
        vm.testSiteString.value = sharedPrefsInit.getString("testSite", "Error")
    }
    vm.testSiteString.observe(this){
        sharedPrefsInit.edit().putString("testSite", vm.testSiteString.value.toString()).apply()
    }*/
}