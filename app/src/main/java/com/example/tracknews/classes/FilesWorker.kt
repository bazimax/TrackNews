package com.example.tracknews.classes

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tracknews.MainActivity
import com.example.tracknews.services.WorkerFindNewsFun
import com.google.gson.Gson
import org.json.JSONException
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FilesWorker () {
    private val logNameClass = "FilesWorker" //для логов

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, активити и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
        const val TAG_DATA_IF = Constants.TAG_DATA_IF //переменные и данные в циклах
    }

    //private val ctx = context
    //private val sharedPrefs: SharedPreferences? = ctx.getSharedPreferences("init", Context.MODE_PRIVATE) //getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

    //Записываем данные(data) в файл
    fun writeToFile(data: String, nameFile: String, context: Context) {
        Log.d(TAG_DEBUG, "FilesWorker >f writeToFile === START")
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(nameFile, AppCompatActivity.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "ERROR: $logNameClass >f writeToFile > File write failed: $e")
        }
        Log.d(TAG_DEBUG, "$logNameClass >f writeToFile ----- END")
    }

    //Читаем данные(data) из файла
    fun readFromFile(nameFile: String, context: Context): String {
        Log.d(TAG_DEBUG, "$logNameClass >f readFromFile === START")
        var data = ""
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
                data = stringBuilder.toString()
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "ERROR: $logNameClass >f readFromFile > File not found: $e")
        } catch (e: IOException) {
            Log.e(TAG, "ERROR: $logNameClass >f readFromFile > Can not read file: $e")
        }
        Log.d(TAG_DEBUG, "$logNameClass >f readFromFile ----- END")
        Log.d(TAG_DATA_BIG, "$logNameClass >f readFromFile > data: $data")
        return data
    }

    //записывем данные в JSON
    fun writeJSON(data: Any, nameFile: String, context: Context) {
        Log.d(TAG_DEBUG, "$logNameClass >f writeJSON === START")

        //сериализация
        val gson = Gson()
        val json = gson.toJson(data)
        //Log.d(TAG_DATA, "FilesWorker >f writeJSON > json: $json")

        //Записываем текст в файл
        writeToFile(json, nameFile, context)
        Log.d(TAG_DEBUG, "$logNameClass >f writeJSON ------------END")
    }

    //читаем JSON - SearchItemArrayList
    fun readJSONSearchItemArrayList(nameFile: String, context: Context):  SearchItemArrayList {
        Log.d(TAG_DEBUG, "$logNameClass >f readJSONSearchItemArrayList === START")
        Log.d(TAG_DEBUG, "$logNameClass >f readJSONSearchItemArrayList // читаем JSON")
        //читаем файл JSON
        val searchItemListJSON = readFromFile(nameFile, context)

        Log.d(TAG_DATA, "$logNameClass >f readJSONSearchItemArrayList > searchItemListJSON: $searchItemListJSON")

        //десериализация
        var data = SearchItemArrayList(ArrayList())
        try {
            data = Gson().fromJson(searchItemListJSON, SearchItemArrayList::class.java)
            //Log.d(TAG_DATA, "$logNameClass >f readJSONSearchItemArrayList > data: $data")
            /*data.list.forEach {
                val dateParse = it.searchItem.search
                val dateParse1 = it.searchItem
                //Log.d(TAG, "$logNameClass >f readJSONSearchItemArrayList > it: ${dateParse1.search}")
            }*/

        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: $logNameClass >f readJSONSearchItemArrayList > JSONException: $e")
        }
        Log.d(TAG_DEBUG, "$logNameClass >f readJSONSearchItemArrayList ----- END")
        return data
    }

    /*fun readSharedPreferencesString(nameSharedPreferences: String): String? {
        //читаем строку из SharedPreferences
        return sharedPrefs.getString(nameSharedPreferences, "")
    }

    fun writeSharedPreferencesString(data: String, nameSharedPreferences: String) {
        //записываем строку в SharedPreferences
        sharedPrefs.edit().putString(nameSharedPreferences, data).apply()
    }*/

    //проверка первый ли это запуск приложения
    fun checkStatusFirstLaunch(context: Context): Boolean {
        //записываем строку в SharedPreferences
        val sharedPrefs = context.getSharedPreferences("init", Context.MODE_PRIVATE)
        val statusFirstLaunch = sharedPrefs.getBoolean(Constants.SHARED_FIRST_LAUNCH, true)

        //если это первый запуск, то меняем true на false и записываем в SharedPreferences
        val statusOutput: Boolean = if (statusFirstLaunch) {
            //первый запуск приложения
            Log.d(TAG, "$logNameClass >f checkStatusFirstLaunch > IF > firstLaunch")
            sharedPrefs.edit().putBoolean(Constants.SHARED_FIRST_LAUNCH, false).apply()
            firstLaunch(context)
            false
        } else {
            true
        }
        return statusOutput
    }

    private fun firstLaunch(context: Context){
        //генерируем пустой JSON
        val dataListWorker = ArrayList<SearchItemWorker>()
        val searchItemArrayList = SearchItemArrayList(dataListWorker)
        FilesWorker().writeJSON(searchItemArrayList, ViewModelFunctions.FILE_SEARCH_ITEM, context)
        Log.d(TAG_DEBUG, "$logNameClass >f firstLaunch === START / END")
        Log.d(TAG_DATA, "$logNameClass >f firstLaunch > searchItemArrayList: $searchItemArrayList")
        writeJSON(searchItemArrayList, Constants.FILE_SEARCH_ITEM, context)
        WorkerFindNewsFun().workerFindNewsFirst(context)
    }

    //?? что за Any?
    /*fun readJSONAny(nameFile: String, classJava: Class<*>?, context: Context):Any {
        //читаем любой JSON
        Log.d(TAG, "FilesWorker >f readJSONAny ======START")
        val searchItemListJSON = readFromFile(nameFile, context)
        var data = Any()
        try {
            data = Gson().fromJson(searchItemListJSON, classJava) // classJava = SearchItemArrayList::class.java

        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: FilesWorker >f readJSON > JSONException: $e")
        }
        Log.d(TAG, "FilesWorker >f readJSONAny ------------END")
        return data
    }*/

    //загружаем список сохраненных поисков (searchItem) для поиска новых новстей
    /*val searchItemList = mutableListOf<SearchItem>() //готовим список searchItem
    val arrayItem = searchItemString?.split("%20:")?.toTypedArray() //разбиваем цельную строку на массив будущих элементов searchItem
    arrayItem?.forEach {
        //каждый элемент массива записываем в список как объекты SearchItem
        val searchItem = SearchItem(it)
        searchItemList.add(searchItem)
    }
    //добавляем новое значение SearchItem
    val stringItem = sharedPrefs.getString("testSite", "+") //читаем сохраненную ранее строку с searchItem
    Log.d(TAG, "WorkerFindNews >f doWork > try > stringItem: $stringItem")*/

}

/*
fun firstFirst(context: Context){
        writeToFile("false", Constants.FILE_FIRST_LAUNCH, context)
    }
//первый запуск приложения
fun firstLaunch(context: Context) {
    //читаем статус из файла
    val statusFirstLaunch = readFromFile(Constants.FILE_FIRST_LAUNCH, context)

    //Проверка на краш SharedPreferences
    if (statusFirstLaunch == "true") {
        //все норм
    }else //какой-то баг

    //записываем в файл - единственный раз (до переустановки приложения)
        writeToFile("false", Constants.FILE_FIRST_LAUNCH, context)
    //записываем в SharedPreferences
    val sharedPrefs = context.getSharedPreferences("init", Context.MODE_PRIVATE)
    sharedPrefs.edit().putString(Constants.SHARED_FIRST_LAUNCH, "false").apply()
}*/
