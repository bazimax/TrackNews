package com.example.tracknews

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.classes.*
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.services.MainServices
import com.example.tracknews.services.WorkerFindNews
import com.example.tracknews.services.WorkerFindNewsFun
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), SearchItemAdapter.Listener {
    private val logNameClass = "MainActivity" //для логов

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, активити и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные

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
        const val SHARED_INSTRUCTION = Constants.SHARED_INSTRUCTION//"instruction"


        //Имена файлов
        const val FILE_SEARCH_ITEM = Constants.FILE_SEARCH_ITEM//"searchItems.json"

        //savedInstanceState
        const val STATE_SEARCH_ITEM_ACTIVE = Constants.STATE_SEARCH_ITEM_ACTIVE
    }

    private lateinit var binding: ActivityMainBinding
    private val vm: ViewModel by viewModels()
    private val searchItemAdapter = SearchItemAdapter(this) //список сохраненных поисков
    private val mainDbManager = MainDbManager(this) //База Данных (БД)
    private val sharedPrefs by lazy {getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)}

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TrackNews) //убираем сплешскрин - меняем тему установленную в манифесте на нужную до super.onCreate
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // ^ привязка

        Log.d(TAG_DEBUG, "$logNameClass ======================== >\n======\n======\n======\n====== LAUNCH ")

        /*//криво определяем повернут телефон или нет
        if (vm.statusLandscape.value == "true") {
            loadFragment(R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
        }*/

        vm.searchItemActive.value = savedInstanceState?.getString(STATE_SEARCH_ITEM_ACTIVE)

        init() //стартовые функции, запуск БД и ViewModel, отслеживание изменений в них
        initThemeListener() //работаем с темой
        initTheme() //работаем с темой
        mainDbManager.openDb() //создаем/открываем Базу Данных (БД) SQLite
        observeVM() //observeVM(vmFunctions) // подключаем observe

        //Загружаем фрагменты
        //loadFragment(R.id.frameLayoutToolbar, ToolbarFragment.newInstance())
        loadFragment(R.id.frameLayoutActivityMain, MainFragment.newInstance())
        loadFragment(R.id.frameLayoutMainFragment, NewsFragment.newInstance())
        loadFragment(R.id.fragNewsPlaceHolder, NewsTodayFragment.newInstance())



        val buttonSearch = binding.fabButtonSearch
        val buttonSave = binding.buttonSave
        val buttonSaved = binding.buttonSaved
        val buttonGo = binding.buttonGO
        val cardView = binding.cardView
        val searchText = binding.editTextSearch
        val btnSavedSearches = binding.buttonSavedSearches
        val frameLayoutSavedSearches = binding.frameLayoutSavedSearches
        val animationView = AnimationView()

        cardView.alpha = 0F
        cardViewVisibility(cardView, buttonSearch) //отслеживаем видимость cardView

        //раскрываем меню поиска
        buttonSearch.setOnClickListener {
            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonSearch === START")

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

            //инструкция шаг 0 > 1
            if (vm.statusInstruction.value !=null && vm.statusInstruction.value != "-1") {
                vm.statusInstruction.value = "step1"
            }
            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonSearch ----- END")
        }

        //GO - поиск новостей
        buttonGo.setOnClickListener {
            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonGo === START")

            //опказываем прогрессбар (прогрессбар пропадет уже после выполнения поиска)
            vm.statusProgressBar.value = true

            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonGo > Swap 1")

            //запускаем поиск
            val search = binding.editTextSearch.text.toString()
            GlobalScope.launch {
                ViewModelFunctions(vm).findNews(search, this@MainActivity)
                /*runOnUiThread {
                    buttonGo.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }*/
            }

            //инструкция шаг 2 > 3
            if (vm.statusInstruction.value == "step2") {
                vm.statusInstruction.value = "step3"
            }
            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonGo ----- END")
        }

        //save - сохраняем поисковой запрос (из строки поиска)
        buttonSave.setOnClickListener {
            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonSave === START")

            val search = binding.editTextSearch.text.toString()
            Log.d(TAG, "$logNameClass >f CLICK_buttonSave >  search: $search")
            ViewModelFunctions(vm).saveSearch(search, this)
            buttonSaved.visibility = View.VISIBLE
            buttonSave.visibility = View.INVISIBLE

            //инструкция шаг 3 > 4
            if (vm.statusInstruction.value == "step3") {
                vm.statusInstruction.value = "step4"
            }
            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonSave ----- END")
        }

        searchText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val checkSame = ViewModelFunctions(vm).findSameSearchItem(p0.toString())
                if (checkSame) {
                    buttonSaved.visibility = View.VISIBLE
                    buttonSave.visibility = View.INVISIBLE
                }
                else {
                    buttonSave.visibility = View.VISIBLE
                    buttonSaved.visibility = View.INVISIBLE
                }

                //инструкция шаг 1 > 2
                if (vm.statusInstruction.value == "step1") {
                    vm.statusInstruction.value = "step2"
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

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
        binding.testButton.setOnClickListener {
            Log.d(TAG, "$logNameClass >f CLICK_1 > =============================== >>>")
            testButton1()
        }

        binding.testButton2.setOnClickListener {
            Log.d(TAG, "$logNameClass >f CLICK_2 > =============================== >>>")
            testButton2()
        }
        //временные кнопки ^

        //выдвигаем меню настроек
        binding.buttonHamburger.setOnClickListener {
            //Log.d("TAG1", "mDrawer Button Click")
            binding.actMainDrawer.openDrawer(GravityCompat.START)
        }

        //раскрываем список "сохранненых поисков (подписок)"
        btnSavedSearches.setOnClickListener {
            frameLayoutSavedSearches.layoutParams = frameLayoutSavedSearches.layoutParams
            //binding.frameLayoutSavedSearches.layoutParams = binding.frameLayoutSavedSearches.layoutParams

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

            //инструкция - финал
            /*if (vm.statusInstruction.value == "step4") {
                vm.statusInstruction.value = "device"
                sharedPrefs.edit().putString(SHARED_INSTRUCTION, "device").apply() //инструкция больше не будет показываться
            }*/
        }

        //Log.d("TAG1", "Close program --------")
    }

    override fun onPause() {
        super.onPause()
        //Log.d(TAG_DEBUG, "Pause program ------------------------------------------------------Start")
        //сохраняем активный SearchItem на время паузы приложения
        //ViewModelFunctions(vm).saveSearchItemActive(this)

        //сбрасываем активный SearchItem на позицую 0
        //ViewModelFunctions(vm).resetSearchItemActive(this)
        Log.d(TAG_DEBUG, "Pause program ------------------------------------------------------End")
    }
    override fun onDestroy() {
        //Log.d(TAG_DEBUG, "Destroy program ------------------------------=======================Start")
        mainDbManager.closeDb() //закрываем БД (доступ к БД?)

        //сбрасываем активный SearchItem на позицую 0
        //ViewModelFunctions(vm).resetSearchItemActive(this)

        Log.d(TAG_DEBUG, "Destroy program ------------------------------=======================End")
        super.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //сохраняем активный SearchItem на время паузы приложения
        ViewModelFunctions(vm).saveSearchItemActive(this)

        outState.putString(STATE_SEARCH_ITEM_ACTIVE, vm.searchItemActive.value)
    }

    //кнопка назад
    //Back button
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
        Log.d(TAG_DEBUG, "$logNameClass >f init === START")

        //первый запуск - изменение статуса
        //Delete??
        FilesWorker().checkStatusFirstLaunch(this) //проверка - первый ли это запуск. Для инструкции

        //инструкция
        instruction()


        val rcView = binding.actMainRecyclerViewSavedSearches

        Log.d(TAG_DEBUG, "$logNameClass >f init > startRecyclerViewActMain === START")
        ViewModelFunctions(vm).readSearchItemListToRcView(this, false)



        Log.d(TAG_DEBUG, "$logNameClass >f init > searchItemActive: ${vm.searchItemActive.value}")
        //если список <сохраненных поисков> не пустой
        if (vm.searchItemActive.value == null) {
            //выбираем активный SearchItem
            ViewModelFunctions(vm).selectSearchItemActive(this)
        }

        //Worker - работа в фоне и отправка уведомлений
        worker()
        //WorkerFindNewsFun().workerFindNewsFirst(this)

        //Worker - работа в фоне и отправка уведомлений //?? PENDING
        //если задача не запущена - то запустить
        /*val workerStatus = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER).get()[0].state
        if (workerStatus != WorkInfo.State.ENQUEUED) {
            WorkerFindNewsFun().workerFindNewsFirst(this)
        }*/

        //подключаем RecyclerView и отображаем данные из SQLite
        binding.apply {
            //fragTest2RecyclerView.setHasFixedSize(true) //для оптимизации?
            //actMainRecyclerViewSavedSearches.layoutManager = LinearLayoutManager(view.context) //проверить
            actMainRecyclerViewSavedSearches.layoutManager = GridLayoutManager(rcView.context, 3) //проверить
            actMainRecyclerViewSavedSearches.adapter = searchItemAdapter
        }

        Log.d(TAG_DEBUG, "$logNameClass >f init ----- END")
    }

    //отслеживание изменений в ViewModel
    //private fun observeVM(vmFunctions: ViewModelFunctions){
    private fun observeVM(){
        //основная логика приложения завязанная на ViewModel
        ViewModelFunctions(vm).observeVM(mainDbManager, searchItemAdapter, this)

        //View логика MainActivity завязанная на ViewModel
        //searchItemList для RcView
        vm.searchItemList.value?.let { searchItemAdapter.addAllSearch(it) }
        vm.searchItemList.observe(this) {
            Log.d(TAG, "MainActivity >f searchItemList.OBSERVE ${vm.searchItemList.value}")
            vm.searchItemList.value?.let { it1 -> searchItemAdapter.addAllSearch(it1) }
            searchItemAdapter.notifyDataSetChanged() //??!! //обновляем RcView с SearchItem
        }

        //кнопка GO и прогрессбар
        vm.statusProgressBar.observe(this){
            FragmentFunction(vm).progressBarSwap(binding.buttonGO, binding.progressBar)
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
    //load fragment
    private fun loadFragment(idFrameLayoutFragment: Int, fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(idFrameLayoutFragment, fragment)
            .commit()
    }

    //Recycler View >
    //при клике на элемент searchItem в recycler view -> он становится активным -> из БД загружаются все новости с этим именем
    override fun clickOnSearchItem(searchItem: SearchItem) {
        ViewModelFunctions(vm).clickOnSearchItem(searchItem, this)
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
        Log.d(TAG_DEBUG, "$logNameClass >f initThemeListener === START")
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

    //Инструкции
    private fun instruction(){


        val sharedInstruction = sharedPrefs.getString(SHARED_INSTRUCTION, "")

        //если это первый запуск, то показываем инструкцию
        if (sharedInstruction == "") {
            vm.statusInstruction.value = "step0"
        }
        else {
            vm.statusInstruction.value = "-1"
        }
        //инструкции
        //observe
        vm.statusInstruction.observe(this) {
            val statusInstruction = vm.statusInstruction.value

            binding.cardViewInstructionStart.visibility = View.GONE
            binding.cardViewInstructionStartEditText.visibility = View.GONE
            binding.cardViewInstructionStartGo.visibility = View.GONE
            binding.cardViewInstructionSaveSearch.visibility = View.GONE
            binding.cardViewInstructionFinish.visibility = View.GONE
            binding.cardViewInstructionDevice.visibility = View.GONE //важное уведомление
            when (statusInstruction) {
                "step0" -> binding.cardViewInstructionStart.visibility = View.VISIBLE
                "step1" -> binding.cardViewInstructionStartEditText.visibility = View.VISIBLE
                "step2" -> binding.cardViewInstructionStartGo.visibility = View.VISIBLE
                "step3" -> binding.cardViewInstructionSaveSearch.visibility = View.VISIBLE
                "step4" -> binding.cardViewInstructionFinish.visibility = View.VISIBLE
                "device" -> binding.cardViewInstructionDevice.visibility = View.VISIBLE //важное уведомление
            }
        }

        //кнопки
        //закончить начальное обучение
        binding.textViewInstructionFinish.setOnClickListener {
            // Device model
            val phoneModel = Build.MODEL
            val checkModelList = listOf("xiaomi", "mi", "redmi", "huawei", "oppo", "one+", "lenovo", "nokia")

            vm.statusInstruction.value = "device"
            sharedPrefs.edit().putString(SHARED_INSTRUCTION, "device").apply() //инструкция больше не будет показываться
        }

        //важное уведомление больше не будет показываться
        binding.buttonInstructionDevice.setOnClickListener {
            vm.statusInstruction.value = "-1"
            sharedPrefs.edit().putString(SHARED_INSTRUCTION, "-1").apply() //инструкция больше не будет показываться
        }

        //Пройти обучение заново
        binding.buttonInstruction.setOnClickListener {
            vm.statusInstruction.value = "step0"
        }

        //отменить начальное обучение
        binding.textViewInstructionCancel.setOnClickListener {
            vm.statusInstruction.value = "-1"
            sharedPrefs.edit().putString(SHARED_INSTRUCTION, "-1").apply() //инструкция больше не будет показываться
        }
    }

    //Worker - работа в фоне и отправка уведомлений
    private fun worker(){
        WorkerFindNewsFun().workerFindNewsFirst(this)

        //разные проверки
        val outputData2 = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER).get().forEach {
            //Log.d(TAG, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: ${outputData.state}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > id: ${it.id}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > state: ${it.state}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: ${it.outputData}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData.key: ${it.outputData.keyValueMap}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData.size: ${it.outputData.size()}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > progress: ${it.progress}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > tags: ${it.tags}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > runAttemptCount: ${it.runAttemptCount}")
        }

        val outputData = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER)
        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: $outputData")
        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData2: $outputData2")
    }


    //BACKUP >
    private fun testButton1(){
        val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
            .addTag(WorkerFindNews.WORKER_TAG_PARSER)
            .build()

        MainServices().notification(true, this)
        //MainServices().notificationNew(true, this)

        //val outputData = WorkManager.getInstance(this).enqueueUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.KEEP, myWorkRequest)//enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.KEEP , photoCheckWork)
        //val outputData2 = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER).get()[0]

        val outputData2 = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER).get().forEach {
            //Log.d(TAG, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: ${outputData.state}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > id: ${it.id}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > state: ${it.state}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: ${it.outputData}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData.key: ${it.outputData.keyValueMap}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData.size: ${it.outputData.size()}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > progress: ${it.progress}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > tags: ${it.tags}")
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f CLICK_1 > testButton1 > worker > runAttemptCount: ${it.runAttemptCount}")
        }

        val outputData = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER)
        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: $outputData")
        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData2: $outputData2")

        /*fun getResult(context: Context, owner: LifecycleOwner, id: UUID) {
            WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(id)
                .observe(owner, Observer {
                    if (it.state == WorkInfo.State.SUCCEEDED) {
                        val result = it.outputData.getInt(WORKER_RESULT_INT, 0)
                        // do something with result
                    }
                })
        }*/

        /*val testSiteString = filesWorker.readFromFile("testSite.txt", this)
        Log.d(WorkerFindNews.TAG, "WorkerFindNews >f doWork > try > testSiteString: $testSiteString")
        Log.d(WorkerFindNews.TAG, "WorkerFindNews >f doWork > try > testSiteString: ${vm.testSiteString.value.toString()}")*/

        //...MainServices().notification(true, this) //...
        //.. ViewModelFunctions(vm).testNameThisFun()

        //Log.d(TAG_DATA, "$logNameClass >f CLICK_5 > ${vm.newsItemArrayAll.value}")


        //Log.d(TAG, "$logNameClass >C MainDbManager > clearAllDataInDb: ${mainDbManager.clearAllDataInDb()}")
        //ViewModelFunctions(vm).updateNewsCountForEachSearchItem(mainDbManager, this)

        //mainDbManager.clearAllDataInDb()
        /*val site = FilesWorker().readFromFile(Constants.FILE_TEST_LOAD_SITE, this) //(siteTemp, Constants.FILE_TEST_LOAD_SITE, context)
        ParserSites().testParse("ведьмак", site, this)*/

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

    private fun testButton2(){
        WorkerFindNewsFun().workerFindNewsFirst(this)

        // Device model
        val phoneModel = Build.MODEL
        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_2 > testButton2 > PhoneModel: $phoneModel")

        // Android version
        val androidVersion = Build.VERSION.RELEASE
        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_2 > testButton2 > AndroidVersion: $androidVersion")

        var s = "Debug-info:"
        s += """OS Version: ${System.getProperty("os.version")}(${Build.VERSION.INCREMENTAL})"""
        s += """OS API Level: ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT})"""
        s += """Device: ${Build.DEVICE}"""
        s += """Model (and Product): ${Build.MODEL} (${Build.PRODUCT})"""

        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_2 > testButton2 > s: $s")

        var d = "Debug-info:"
        d += "\n OS Version: " + System.getProperty("os.version") + "(" + Build.VERSION.INCREMENTAL + ")"
        d += "\n OS API Level: " + Build.VERSION.RELEASE + "(" + Build.VERSION.SDK_INT + ")"
        d += "\n Device: " + Build.DEVICE
        d += "\n Model (and Product): " + Build.MODEL + " (" + Build.PRODUCT + ")"

        Log.d(Constants.TAG_DATA, "$logNameClass >f CLICK_2 > testButton2 > d: $d")



        //Log.d(TAG, "$logNameClass >C MainDbManager > readDbData: ${mainDbManager.readDbData()}")
        /*val list = vm.searchItemActive.value?.let { it1 ->
            mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, it1)
        }

        list?.forEach {
            Log.d(Constants.TAG_DATA_IF, "$logNameClass >C MainDbManager >\n" +
                    "- it.search: ${it.search}\n" +
                    "- it.id: ${it.id}\n" +
                    "- it.statusSaved: ${it.statusSaved}\n" +
                    "- it.title: ${it.title}\n")
        }*/
    }

    fun generateSearchItemArrayList(){
        //записываем данные
        Log.d(TAG, "Main Activity >f writeJSON ======START")
        val stringItem = "name%20:witcher%20:Moscow%20:Columbia%20:Washington%20:Bali%20:Kin"
        val dataListWorker = ArrayList<SearchItemWorker>()
        val arrayItem = stringItem.split("%20:").toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
        arrayItem.forEach {
            //каждый элемент массива записываем в список как объекты SearchItem
            val searchItem = SearchItem(it)
            val searchItemWorker = SearchItemWorker(searchItem)
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

        FilesWorker().writeJSON(data, FILE_SEARCH_ITEM, this)
        Log.d(TAG, "Main Activity >f writeJSON > -------------------")

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

    //WorkManager.getInstance(this).cancelAllWorkByTag(WorkerFindNews.WORKER_TAG_PARSER)
    //WorkManager.getInstance(this).cancelUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER)
    //WorkManager.getInstance(this).cancelAllWork()

    /*
        var screenDisplayWidth = binding.frameLayoutActivityMain.layoutParams.width
        fun logWidth(){
            Log.d("TAG1", "L width: ${cardView.layoutParams.width}")
            Log.d("TAG1", "width: ${cardView.width}")
        }
        //Log.d("TAG1", "screenDisplayWidth: $screenDisplayWidth")*/
    //BACKUP ^
}