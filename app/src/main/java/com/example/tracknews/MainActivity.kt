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
import androidx.work.WorkManager
import com.example.tracknews.news.NewsTodayFragment
import com.example.tracknews.atest.MyLog
import com.example.tracknews.classes.*
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.services.WorkerFindNewsFun
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), SearchItemAdapter.Listener {
    private val logNameClass = "MainActivity" //для логов

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные

        //theme
        const val PREFS_NAME = Constants.PREFS_NAME//"theme_prefs"
        const val KEY_THEME = Constants.KEY_THEME//"prefs.theme"
        const val THEME_UNDEFINED = Constants.THEME_UNDEFINED//-1
        const val THEME_LIGHT = Constants.THEME_LIGHT//0
        const val THEME_DARK = Constants.THEME_DARK//1
        const val THEME_SYSTEM = Constants.THEME_SYSTEM//2
        const val THEME_BATTERY = Constants.THEME_BATTERY//3

        //SharedPreferences
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
        setTheme(R.style.Theme_TrackNews) //убираем splashscreen - меняем тему установленную в манифесте на нужную до super.onCreate
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // ^ привязка

        Log.d(TAG_DEBUG, "$logNameClass ======================== >\n======\n======\n======\n====== LAUNCH ")

        vm.searchItemActive.value = savedInstanceState?.getString(STATE_SEARCH_ITEM_ACTIVE)

        init() //стартовые функции, запуск БД и ViewModel, отслеживание изменений в них
        initThemeListener() //работаем с темой
        initTheme() //работаем с темой
        mainDbManager.openDb() //создаем/открываем Базу Данных (БД) SQLite
        observeVM() //observeVM(vmFunctions) // подключаем observe

        //Загружаем фрагменты
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

            cardView.animate().alpha(1F).withEndAction {
                animationView.scaleWidth(cardView, -1) //раскрываем на всю ширину
                animationView.swapButton(buttonSearch, cardView, 700, 200) //меняем кнопки
            }
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

            //показываем прогресс-бар (прогресс-бар пропадет уже после выполнения поиска)
            vm.statusProgressBar.value = true

            Log.d(TAG_DEBUG, "$logNameClass >f CLICK_buttonGo > Swap 1")

            //запускаем поиск
            val search = binding.editTextSearch.text.toString()

            CoroutineScope(Dispatchers.IO).launch {
                ViewModelFunctions(vm).findNews(search, this@MainActivity)
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
        }

        //отменить выделение "сохраненных поисков"
        binding.searchItemButtonCancel.setOnClickListener {
            ViewModelFunctions(vm).cancelSelectSearchItem(searchItemAdapter)
        }

        //выдвигаем меню настроек
        binding.buttonHamburger.setOnClickListener {
            binding.actMainDrawer.openDrawer(GravityCompat.START)
        }

        //раскрываем список "сохраненных поисков (подписок)"
        btnSavedSearches.setOnClickListener {
            frameLayoutSavedSearches.layoutParams = frameLayoutSavedSearches.layoutParams

            if (vm.statusSavedSearchesView) {
                binding.frameLayoutSavedSearches.layoutParams.height = 0
                btnSavedSearches.animate().rotation(0F).duration = 200
                vm.statusSavedSearchesView = false
            }
            else {
                vm.searchItemDeleteCount.value = 0 //скрываем кнопки
                //searchItemAdapter.notifyDataSetChanged() //!! обновляем RcView с SearchItem //заблокировано на время
                // -1 math_parent
                // -2 wrap_content
                binding.frameLayoutSavedSearches.layoutParams.height = -2
                btnSavedSearches.animate().rotation(180F).duration = 200
                vm.statusSavedSearchesView = true
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG_DEBUG, "Pause program ------------------------------------------------------Start")
        Log.d(TAG_DEBUG, "Pause program ------------------------------------------------------End")
    }
    override fun onDestroy() {
        Log.d(TAG_DEBUG, "Destroy program ------------------------------=======================Start")
        mainDbManager.closeDb() //закрываем БД (доступ к БД?)

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
            //повторный запрос на выход из приложения.
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

        Log.d(TAG_DEBUG, "$logNameClass >f init === START")

        //первый запуск - изменение статуса
        FilesWorker().checkStatusFirstLaunch(this) //проверка - первый ли это запуск. Для инструкции

        //инструкция
        instruction()

        val rcView = binding.actMainRecyclerViewSavedSearches

        ViewModelFunctions(vm).readSearchItemListToRcView(this, false)

        Log.d(TAG_DEBUG, "$logNameClass >f init > searchItemActive: ${vm.searchItemActive.value}")
        //если список <сохраненных поисков> не пустой
        if (vm.searchItemActive.value == null) {
            //выбираем активный SearchItem
            ViewModelFunctions(vm).selectSearchItemActive(this)
        }

        //Worker - работа в фоне и отправка уведомлений
        worker()

        //подключаем RecyclerView и отображаем данные из SQLite
        binding.apply {
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
            //searchItemAdapter.notifyDataSetChanged() //!! //обновляем RcView с SearchItem //заблокировано на время
        }

        //кнопка GO и прогресс-бар
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
                animationView.lateHide(cardView, 1)
                animationView.scaleWidth(cardView, vm.sizeFAButton)
                animationView.swapButton(cardView, view, 700, 200)
            }
        }
    }

    //WORK - функции далее -> тема (светлая и темная). Чужой код
    private fun initThemeListener(){
        val log = MyLog(logNameClass, "initThemeListener")

        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.themeLight -> setTheme(AppCompatDelegate.MODE_NIGHT_NO, THEME_LIGHT)
                R.id.themeDark -> setTheme(AppCompatDelegate.MODE_NIGHT_YES, THEME_DARK)
                R.id.themeBattery -> setTheme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, THEME_BATTERY)
                R.id.themeSystem -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, THEME_SYSTEM)
            }
        }

        log.end()
    }

    private fun setTheme(themeMode: Int, prefsMode: Int) {

        AppCompatDelegate.setDefaultNightMode(themeMode)

        saveTheme(prefsMode)
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
        val logWorker = MyLog(logNameClass, "worker")

        WorkerFindNewsFun().workerFindNewsFirst(this)

        //разные проверки
        val outputData2 = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER).get().forEach {
            //Log.d(TAG, "$logNameClass >f CLICK_1 > testButton1 > worker > outputData: ${outputData.state}")

            logWorker.eachData(eachData = "id: ${it.id}")
            logWorker.eachData(eachData = "state: ${it.state}")
            logWorker.eachData(eachData = "outputData: ${it.outputData}")
            logWorker.eachData(eachData = "outputData.key: ${it.outputData.keyValueMap}")
            logWorker.eachData(eachData = "progress: ${it.progress}")
            logWorker.eachData(eachData = "tags: ${it.tags}")
            logWorker.eachData(eachData = "runAttemptCount: ${it.runAttemptCount}")
        }

        val outputData = WorkManager.getInstance(this).getWorkInfosForUniqueWork(Constants.WORKER_UNIQUE_NAME_PARSER)

        logWorker.data(data = "outputData: $outputData")
        logWorker.data(data = "outputData2: $outputData2")

        logWorker.end()
    }
}