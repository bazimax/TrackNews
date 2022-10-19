package com.example.tracknews.classes

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tracknews.MainActivity
import com.google.gson.Gson
import org.json.JSONException
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class FilesWorker (context: Context) {
    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG
    }

    private val ctx = context
    //private val sharedPrefs: SharedPreferences? = ctx.getSharedPreferences("init", Context.MODE_PRIVATE) //getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

    fun writeToFile(data: String, nameFile: String,  context: Context) {
        //Записываем данные(data) в файл
        //Log.d(TAG, "FilesWorker >f writeToFile ======START")
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput(nameFile, AppCompatActivity.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e(TAG, "ERROR: FilesWorker >f writeToFile > File write failed: $e")
            Log.d(TAG, "ERROR: FilesWorker >f writeToFile > File write failed: $e")
        }
        //Log.d(TAG, "FilesWorker >f writeToFile ------------END")
    }

    fun readFromFile(nameFile: String, context: Context): String {
        //Читаем данные(data) из файла
        //Log.d(TAG, "FilesWorker >f readFromFile ======START")
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
            Log.e(TAG, "ERROR: FilesWorker >f readFromFile > File not found: $e")
            Log.d(TAG, "ERROR: FilesWorker >f readFromFile > File not found: $e")
        } catch (e: IOException) {
            Log.e(TAG, "ERROR: FilesWorker >f readFromFile > Can not read file: $e")
            Log.d(TAG, "ERROR: FilesWorker >f readFromFile > Can not read file: $e")
        }
        //Log.d(TAG, "FilesWorker >f readFromFile ------------END")
        return data
    }

    fun writeJSON(data: Any, nameFile: String, context: Context) {
        //записывем данные в JSON
        //Log.d(TAG, "FilesWorker >f writeJSON ======START")

        //сериализация
        val gson = Gson()
        val json = gson.toJson(data)
        Log.d(TAG, "FilesWorker >f writeJSON > json: $json")
        Log.d(TAG, "FilesWorker >f writeJSON > -------------------")

        //Записываем текст в файл
        writeToFile(json, nameFile, context)
        //Log.d(TAG, "FilesWorker >f writeJSON ------------END")
    }

    fun readJSONSearchItemArrayList(nameFile: String, context: Context):  SearchItemArrayList {
        //читаем JSON - SearchItemArrayList
        /*val data1 = ArrayList<SearchItemWorker>()
        //сериализация
        val gson = Gson()
        val newsItemArrayList = SearchItemArrayList(data1)
        val json4 = gson.toJson(newsItemArrayList)
        Log.d(MainActivity.TAG, "Main Activity >f writeJSON > json4: $json4")*/


        Log.d(TAG_DEBUG, "FilesWorker >f readJSONSearchItemArrayList ======START")
        //Читаем файл JSON
        val searchItemListJSON = readFromFile(nameFile, context)
        //Log.d(TAG, "FilesWorker >f readJSONSearchItemArrayList > searchItemListJSON: $searchItemListJSON")

        //десериализация
        var data = SearchItemArrayList(ArrayList())
        try {
            data = Gson().fromJson(searchItemListJSON, SearchItemArrayList::class.java)
            //Log.d(TAG, "FilesWorker >f readJSONSearchItemArrayList > data: $data")
            /*data.list.forEach {
                val dateParse = it.searchItem.search
                val dateParse1 = it.searchItem
                //Log.d(TAG, "FilesWorker >f readJSONSearchItemArrayList > it: ${dateParse1.search}")
            }*/

        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: FilesWorker >f readJSONSearchItemArrayList > JSONException: $e")
        }
        Log.d(TAG_DEBUG, "FilesWorker >f readJSONSearchItemArrayList ------------END")
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

    //что за Any?
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