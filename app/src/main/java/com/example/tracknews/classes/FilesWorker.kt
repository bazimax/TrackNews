package com.example.tracknews.classes

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tracknews.services.WorkerFindNewsFun
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.*

class FilesWorker {
    private val logNameClass = "FilesWorker" //для логов

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
    }

    //private val ctx = context
    //private val sharedPrefs: SharedPreferences? = ctx.getSharedPreferences("init", Context.MODE_PRIVATE) //getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

    //Записываем данные(data) в файл
    //write data to a file
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
    //read data from file
    private fun readFromFile(nameFile: String, context: Context): String {
        Log.d(TAG_DEBUG, "$logNameClass >f readFromFile === START")
        var data = ""
        try {
            val inputStream: InputStream? = context.openFileInput(nameFile)
            if (inputStream != null) {
                val inputStreamReader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                var receiveString: String?
                //Delete var receiveString: String? = ""
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

    //записываем данные в JSON
    //writing data in JSON
    fun writeJSON(data: Any, nameFile: String, context: Context) {
        Log.d(TAG_DEBUG, "$logNameClass >f writeJSON === START")

        //сериализация
        val gson = Gson()
        val json = gson.toJson(data)

        //Записываем текст в файл
        writeToFile(json, nameFile, context)
        Log.d(TAG_DEBUG, "$logNameClass >f writeJSON ------------END")
    }

    //читаем JSON - SearchItemArrayList
    //read JSON - SearchItemArrayList
    fun readJSONSearchItemArrayList(nameFile: String, context: Context):  SearchItemArrayList {
        Log.d(TAG_DEBUG, "$logNameClass >f readJSONSearchItemArrayList === START")
        Log.d(TAG_DEBUG, "$logNameClass >f readJSONSearchItemArrayList // читаем JSON")
        //читаем файл-JSON
        //read file-JSON
        val searchItemListJSON = readFromFile(nameFile, context)

        Log.d(TAG_DATA, "$logNameClass >f readJSONSearchItemArrayList > searchItemListJSON: $searchItemListJSON")

        //десериализация
        //deserialization
        var data = SearchItemArrayList(ArrayList())
        try {
            data = Gson().fromJson(searchItemListJSON, SearchItemArrayList::class.java)
            //Log.d(TAG_DATA, "$logNameClass >f readJSONSearchItemArrayList > data: $data")

        }catch (e: JSONException) {
            Log.e(TAG, "ERROR: $logNameClass >f readJSONSearchItemArrayList > JSONException: $e")
        }
        Log.d(TAG_DEBUG, "$logNameClass >f readJSONSearchItemArrayList ----- END")
        return data
    }

    //проверка первый ли это запуск приложения
    //checking First Run
    fun checkStatusFirstLaunch(context: Context): Boolean {
        //получаем значение из SharedPreferences
        //get status from SharedPreferences
        val sharedPrefs = context.getSharedPreferences("init", Context.MODE_PRIVATE)
        val statusFirstLaunch = sharedPrefs.getBoolean(Constants.SHARED_FIRST_LAUNCH, true)

        //если это первый запуск, то меняем true на false и записываем в SharedPreferences
        //if this is the first run, then change TRUE to FALSE and write to SharedPreferences
        val statusOutput: Boolean = if (statusFirstLaunch) {
            //первый запуск приложения
            //first run
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
        //generate empty JSON
        val dataListWorker = ArrayList<SearchItemWorker>()
        val searchItemArrayList = SearchItemArrayList(dataListWorker)
        FilesWorker().writeJSON(searchItemArrayList, ViewModelFunctions.FILE_SEARCH_ITEM, context)
        Log.d(TAG_DEBUG, "$logNameClass >f firstLaunch === START / END")
        Log.d(TAG_DATA, "$logNameClass >f firstLaunch > searchItemArrayList: $searchItemArrayList")
        writeJSON(searchItemArrayList, Constants.FILE_SEARCH_ITEM, context)

        //run first Worker
        CoroutineScope(Dispatchers.IO).launch {
            WorkerFindNewsFun().workerFindNewsFirst(context)
        }
    }
}

