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
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.classes.AnimationView
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.parseSite.ParserSites
import okhttp3.Request

const val PREFS_NAME = "theme_prefs"
const val KEY_THEME = "prefs.theme"
const val THEME_UNDEFINED = -1
const val THEME_LIGHT = 0
const val THEME_DARK = 1
const val THEME_SYSTEM = 2
const val THEME_BATTERY = 3

class MainActivity : AppCompatActivity() {

    //важные переменные
    private lateinit var binding: ActivityMainBinding
    //var testCount1 = 0
    private val vm: ViewModel by viewModels()
    private val mainDbManager = MainDbManager(this)
    private var parserSites = ParserSites()
    private val sharedPrefs by lazy {getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)}

    //второстепенные переменные



    //@SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TrackNews) //убираем сплешскрин - меняем тему установленную в манифесте на нужную до super.onCreate
        //Log.d("TAG1", "Activity created ==============")
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // ^ привязка

        //init() //стартовые функции, запуск БД и ViewModel, отслеживание изменений в них

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

        initThemeListener() //работаем с темой
        initTheme() //работаем с темой

        //Log.d("TAG1", "Activity >f mainDbManager.openDb")
        mainDbManager.openDb() //создаем/открываем Базу Данных (БД) SQLite
        //Log.d("TAG1", "Activity >f viewModelToSQLite")
        viewModelToSQLite() // подключаем observe
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel")
        loadSQLiteToViewModel() // загружаем БД во viewModel
        deleteElementOfSQLite() // отслеживаем и удаляем элементы БД

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
            val newsItem = parserSites.parse("witcher")
            vm.newsItemTempYa.value = newsItem.list
            vm.testParserSitesString.value = newsItem.statusEthernet
            Log.d("TAG1", "Main Activity > buttonSearch > newsItem.statusEthernet ${newsItem.statusEthernet}")
            //delete^


            //Log.d("TAG1", "buttonSearch - START -------")
            //val animationView = AnimationView()
            if (vm.sizeFAButton == -99) {
                vm.sizeFAButton = buttonSearch.width
            }

            cardView.alpha = 0f
            cardView.animate().alpha(1F).withEndAction(Runnable {
                animationView.scaleWidth(cardView, -1)
                animationView.swapButton(buttonSearch, cardView, 700, 200)
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
        }
        binding.actMainDrLayoutButton2.setOnClickListener {
            mainDbManager.clearAllDataInDb()
            //readDbToTextView()
            loadSQLiteToViewModel()
            //Log.d("TAG1", "end Button2 clear ==============")
        }
        binding.actMainDrLayoutButton3.setOnClickListener {
            Log.d("TAG1", "setThemeDark ---------------------------")
            //setTheme(AppCompatDelegate.MODE_NIGHT_YES, THEME_DARK)
            Log.d("TAG1", "setTheme")
        }
        //временные кнопки ^

        //выдвигаем меню настроек
        binding.buttonHamburger.setOnClickListener {

            //Log.d("TAG1", "mDrawer Button Click")
            binding.actMainDrawer.openDrawer(GravityCompat.START)
            //Log.d("TAG1", "end mDrawer Button ==============")
        }

        //раскрываем сохраннеые результаты поисков
        btnSavedSearches.setOnClickListener {
            frameLayoutSavedSearches.layoutParams = frameLayoutSavedSearches.layoutParams
            //binding.frameLayoutSavedSearches.layoutParams = binding.frameLayoutSavedSearches.layoutParams
            //Log.d("TAG1", "save: ----- ${frameLayoutSavedSearches.layoutTransition} ")
            //val animationView = AnimationView()
            //(frameLayoutSavedSearches as ViewGroup).layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            //frameLayoutSavedSearches.layoutTransition.getAnimator(0)
            //Log.d("TAG1", "save: 1 ")
            if (vm.statusSavedSearchesView) {
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
        mainDbManager.closeDb()
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
        /*//скрываем/показываем кнопку поиска при возвращении с сайта
        if (binding.fabButtonSearch.visibility == View.GONE) {
            binding.fabButtonSearch.visibility = View.VISIBLE
        }*/
    }

    private fun loadFragment(idFrameLayoutFragment: Int, fragment: Fragment){
        //Загружаем фрагмент
        supportFragmentManager
            .beginTransaction()
            .replace(idFrameLayoutFragment, fragment)
            .commit()
    }




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
            val statusSaved = vm.newsItemTemp.value?.link.toString()
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
        //Log.d("TAG1", "Activity >f loadSQLiteToViewModel > ------------END")
    }

    private fun deleteElementOfSQLite(){
        //удалем элемент/строку из Базы Данных
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
            loadSQLiteToViewModel()
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