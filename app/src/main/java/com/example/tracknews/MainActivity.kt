package com.example.tracknews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.tracknews.News.NewsTodayFragment
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.databinding.*
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.parseSite.ParserSites


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    var testCount1 = 0

    //lateinit var binding2: FragmentMainBinding
    //lateinit var bindingNews: FragmentNewsBinding
    //lateinit var bindingToolbar: FragmentToolbarBinding
    lateinit var bindingTest1: FragmentTest1Binding
    private val vm: ViewModel by viewModels()
    val aram = ParserSites("https://habr.com/ru/post/538534/")

    private val mainDbManager = MainDbManager(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TAG1", "Activity created ==============")
        super.onCreate(savedInstanceState)
        vm.url.value = "0000"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // ^ привязка

        mainDbManager.openDb() //создаем базу данных SQLite
        viewModelToSQLite() // подключаем observe
        loadSQLiteToViewModel() // загружаем БД во viewModel

        loadFragment(R.id.frameLayoutToolbar, ToolbarFragment.newInstance())
        loadFragment(R.id.frameLayoutActivityMain, MainFragment.newInstance())
        loadFragment(R.id.frameLayoutMainFragment, NewsFragment.newInstance())
        loadFragment(R.id.fragNewsPlaceHolder, NewsTodayFragment.newInstance())

        if (vm.statusLandscape.value == "true") {
            loadFragment(R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
        }



        binding.actMainDrLayoutButton1.setOnClickListener {
            /*mainDbManager.testInsertToDb(
                binding.actMainDrLayoutEditText.text.toString())
            testReadDbToTextView()
            Log.d("TAG1", "end Button1 add ==============")*/
        }
        binding.actMainDrLayoutButton2.setOnClickListener {
            mainDbManager.clearAllDataInDb()
            //testReadDbToTextView()
            readDbToTextView()
            loadSQLiteToViewModel()
            Log.d("TAG1", "end Button2 clear ==============")
        }
        binding.actMainDrLayoutButton3.setOnClickListener {
            /*vm.testViewToSQLite.value = binding.actMainDrLayoutEditText.text.toString()
            Log.d("TAG1", "Button3 value: ${vm.testViewToSQLite.value}")
            Log.d("TAG1", "Button3 text: ${binding.actMainDrLayoutEditText.text}")

            Log.d("TAG1", "end Button3 outView > InSQL ==============")*/
        }

        binding.actMainButtonDrawer.setOnClickListener {
            //выдвигаем меню настроек
            Log.d("TAG1", "mDrawer Button Click")
            binding.actMainDrawer.openDrawer(GravityCompat.START)

            testReadDbToTextView()
            readDbToTextView()
            Log.d("TAG1", "end mDrawer Button ==============")
        }
        /*dataModel.messagePortrait.observe(this) {

        }*/

        Log.d("TAG1", "Close program --------")
    }

    override fun onDestroy() {
        Log.d("TAG1", "Destroy program --------")
        super.onDestroy()
        mainDbManager.closeDb()

    }

    private fun loadFragment(idFrameLayoutFragment: Int, fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(idFrameLayoutFragment, fragment)
            .commit()
    }
    private fun testReadDbToTextView(){
        //читаем базу данных и записываем в TextView
        binding.actMainDrLayoutText.text = ""
        val dataList = mainDbManager.testReadDbData() //создаем лист данных для тестового отображения
        //Log.d("TAG1", "dataList: $dataList")
        for (item in dataList){
            binding.actMainDrLayoutText.append(item)
            binding.actMainDrLayoutText.append("\n")
        }
    }

    private fun viewModelToSQLite(){
        Log.d("TAG1", "Activity >f viewModelToSQLite >  ======START")
        //следим за изменениями в DataModel(ViewModel) и передаем их в SQLite
        //mainDbManager.openDb()
        vm.testViewToSQLite.observe(this) {
            Log.d("TAG1", "Activity >f viewModelToSQLite > testViewToSQLite.OBSERVE ======START")
            mainDbManager.testInsertToDb(
                binding.actMainDrLayoutEditText.text.toString())
            //binding.actMainDrLayoutText.text = it
            //readDbToTextView()
        }
        vm.newsItemTemp.observe(this){
            Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp.OBSERVE ======START")
            val title= vm.newsItemTemp.value?.title.toString()
            val content = vm.newsItemTemp.value?.content.toString()
            val link = vm.newsItemTemp.value?.link.toString()
            mainDbManager.insertToDb(title, content, link)
            loadSQLiteToViewModel()

            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItem value: ${vm.newsItem.value}")
            //Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp value: ${vm.newsItemTemp.value}")
            Log.d("TAG1", "Activity >f viewModelToSQLite > newsItemTemp.OBSERVE ------------END")
        }

        /*vm.newsItem.observe(this){
            vm.newsItem.value = mainDbManager.readDbData()
        }*/
        /*dataModel.testViewToSQLite.observe(this, {
            binding.actMainDrLayoutText.text = it
        })*/
        Log.d("TAG1", "Activity >f viewModelToSQLite > ------------END")
    }
    private fun readDbToTextView(){
        //читаем базу данных и записываем в TextView

        val dataList = mainDbManager.readDbData() //создаем лист данных для тестового отображения
        //val dataList2 = mainDbManager.testReadDbData() //создаем лист данных для тестового отображения
        //Log.d("TAG1", "dataList: $dataList")
        binding.actMainDrLayoutText.text = ""
        for (item in dataList){
            binding.actMainDrLayoutText.append("${item.title} + ${item.content} + ${item.link}")
            binding.actMainDrLayoutText.append("\n")
        }
        //return dataList
    }
    private fun loadSQLiteToViewModel(){
        Log.d("TAG1", "Activity >f loadSQLiteToViewModel > testCount1: $testCount1 ======START")
        vm.newsItem.value = mainDbManager.readDbData()
        testCount1++
        Log.d("TAG1", "Activity >f loadSQLiteToViewModel > ------------END")
    }

}

//loadFragment(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
/*supportFragmentManager
    .beginTransaction()
    .replace(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
    .addToBackStack("main")
    .commit()*/