package com.example.tracknews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.News.NewsTodayFragment

import com.example.tracknews.databinding.*
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.parseSite.ParserSites
import kotlinx.coroutines.delay


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //lateinit var binding2: FragmentMainBinding
    //lateinit var bindingNews: FragmentNewsBinding
    //lateinit var bindingToolbar: FragmentToolbarBinding
    lateinit var bindingTest1: FragmentTest1Binding
    private val dataModel: DataModel by viewModels()
    val aram = ParserSites("https://habr.com/ru/post/538534/")

    private val mainDbManager = MainDbManager(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("TAG1", "Activity created ==============")
        super.onCreate(savedInstanceState)
        dataModel.url.value = "0000"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // ^ привязка

        mainDbManager.openDb() //создаем базу данных SQLite

        loadFragment(R.id.frameLayoutToolbar, ToolbarFragment.newInstance())
        loadFragment(R.id.frameLayoutActivityMain, MainFragment.newInstance())
        loadFragment(R.id.frameLayoutMainFragment, NewsFragment.newInstance())
        loadFragment(R.id.fragNewsPlaceHolder, NewsTodayFragment.newInstance())

        if (dataModel.statusLandscape.value == "true") {
            loadFragment(R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
        }

        binding.actMainDrLayoutButton1.setOnClickListener {
            mainDbManager.insertToDb(
                binding.actMainDrLayoutEditText.text.toString())
            readDbToTextView()
            Log.d("TAG1", "end Button1 add ==============")
        }
        binding.actMainDrLayoutButton2.setOnClickListener {
            mainDbManager.clearAllDataInDb()
            readDbToTextView()
            Log.d("TAG1", "end Button2 clear ==============")
        }
        binding.actMainDrLayoutButton3.setOnClickListener {
            dataModel.testViewToSQLite.value = binding.actMainDrLayoutEditText.text.toString()
            Log.d("TAG1", "Button3 value: ${dataModel.testViewToSQLite.value}")
            Log.d("TAG1", "Button3 text: ${binding.actMainDrLayoutEditText.text}")

            Log.d("TAG1", "end Button3 outView > InSQL ==============")
        }

        binding.actMainButtonDrawer.setOnClickListener {
            //выдвигаем меню настроек
            Log.d("TAG1", "mDrawer Button Click")
            binding.actMainDrawer.openDrawer(GravityCompat.START)

            readDbToTextView()
            Log.d("TAG1", "end mDrawer Button ==============")
        }
        /*dataModel.messagePortrait.observe(this) {

        }*/
        viewModelToSQLite()
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
    private fun readDbToTextView(){
        //читаем базу данных и записываем в TextView
        binding.actMainDrLayoutText.text = ""
        val dataList = mainDbManager.readDbData() //создаем лист данных для тестового отображения
        //Log.d("TAG1", "dataList: $dataList")
        for (item in dataList){
            binding.actMainDrLayoutText.append(item)
            binding.actMainDrLayoutText.append("\n")
        }
    }

    private fun viewModelToSQLite(){
        //mainDbManager.openDb()
        dataModel.testViewToSQLite.observe(this) {
            mainDbManager.insertToDb(
                binding.actMainDrLayoutEditText.text.toString())
            //binding.actMainDrLayoutText.text = it
            readDbToTextView()
        }
        /*dataModel.testViewToSQLite.observe(this, {
            binding.actMainDrLayoutText.text = it
        })*/

    }
}

//loadFragment(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
/*supportFragmentManager
    .beginTransaction()
    .replace(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
    .addToBackStack("main")
    .commit()*/