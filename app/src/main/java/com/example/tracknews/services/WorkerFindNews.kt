package com.example.tracknews.services

//import android.support.annotation.NonNull
//import androidx.work.WorkRequest
//import androidx.work.Worker
import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.tracknews.MainActivity
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.FilesWorker
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit

//запуск worker(задачи) на поиск новостей
class WorkerFindNews(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val logNameClass = "WorkerFindNews"

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG

        //Worker
        const val WORKER_TAG_PARSER = Constants.WORKER_TAG_PARSER//"parser"
        const val WORKER_UNIQUE_NAME_PARSER = Constants.WORKER_UNIQUE_NAME_PARSER//"uniqueParser"
        const val WORKER_PUT_STATUS_UPDATE = Constants.WORKER_PUT_STATUS_UPDATE//"statusUpdate"
    }

    //private val sharedPrefs = context.getSharedPreferences("init", Context.MODE_PRIVATE) //getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    private val mainDbManager = MainDbManager(context) //База Данных (БД)


    private val parserSites = ParserSites() //парсинг
    private val ctx = context
    private val filesWorker = FilesWorker() //работа с файлами (чтение и запись)
    private var statusUpdateWorker = inputData.getBoolean(WORKER_PUT_STATUS_UPDATE, false) //есть ли новые новости-NewsItem (по умолчанию нет)

    @NonNull
    override fun doWork(): Result {
        Log.d(TAG_DEBUG, "$logNameClass >f doWork === START")

        val outputData : Data

        try {
            outputData = work()
            /*GlobalScope.launch {

            }*/
        } catch (ex: Exception) {
            Log.d(TAG, "$logNameClass >f doWork > catch > FAILURE")
            Log.e(TAG, "$logNameClass >f doWork > catch > FAILURE: $ex")
            return Result.retry()//Result.failure(); //или Result.retry()
        }

        /*val outputData : Data

        try {
            //загружаем список "сохраненных поисков" (SearchItem) для поиска новых новстей -> читаем данные из JSON
            val searchItemArrayList = filesWorker.readJSONSearchItemArrayList(MainActivity.FILE_SEARCH_ITEM, ctx)

            //открываем Базу Данных (БД) SQLite
            mainDbManager.openDb()

            //проверка интернета
            var statusInternet = true
            var pauseCounter: Long = 0
            var pauseCounterI: Long = 0

            searchItemArrayList.list.forEach { itSearchItemArrayList ->
                //для каждого "сохраненного поиска" ищем новые новости
                val search = itSearchItemArrayList.searchItem.search
                Log.d(TAG, "$logNameClass >f doWork > try > search: $search")

                //запускаем парсинг новостных сайтов/сайта
                val resultParse = parserSites.parse(search, ctx)

                Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > resultParse.statusEthernet: ${resultParse.statusEthernet}")
                //Если инета нет
                if (resultParse.statusEthernet == false.toString()) statusInternet = false

                //если есть новости (любые) (если новостей нет, то ничего не делаем)
                if (resultParse.list.size != 0) {
                    //для каждой найденной новости
                    resultParse.list.forEach { itResultParse ->
                        Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > itResultParse.link: ${itResultParse.link}")
                        //Ищем совпадения ссылок напрямую в БД
                        //запускаем поиск, функция возвращает нам список найденных элементов > если список пустой то новость уникальная
                        val findNewsItem = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, itResultParse.link)
                        Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > findNewsItem.size: ${findNewsItem.size}")

                        //если совпадений со старыми новостями нет -> новость уникальная ->записываем её в бд  (если новость не уникальная, то ничего не делаем)
                        if (findNewsItem.size == 0) {
                            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > IF > insertToDb: $findNewsItem")
                            //записываем новость в бд
                            mainDbManager.insertToDb(
                                itResultParse.search,
                                itResultParse.img,
                                itResultParse.date,
                                itResultParse.title,
                                itResultParse.content,
                                itResultParse.link,
                                itResultParse.statusSaved)
                            //увеличиваем счетчик новых новостей
                            itSearchItemArrayList.searchItem.counterNewNews++

                            //меняем статус "есть ли новые новости" на "да"
                            statusUpdateWorker = true
                        }
                    }
                }
                //Считаем общее количество новостей
                itSearchItemArrayList.searchItem.counterAllNews = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, search).size

                //после парсинга одного "сохраненного поиска" немного ждем
                Log.d(TAG, "$logNameClass >f doWork > PAUSE START")
                if (pauseCounter < 600) {
                    pauseCounterI++
                    pauseCounter += 30 + pauseCounterI
                }
                else {
                    pauseCounter = 0
                }
                Log.d(TAG, "$logNameClass >f doWork > PAUSE > pauseCounter: $pauseCounter")
                TimeUnit.SECONDS.sleep(pauseCounter)
                //Thread.sleep(300_000) //TimeUnit.MINUTES.sleep(5) //одно и тоже
                Log.d(TAG, "$logNameClass >f doWork > PAUSE END")
            }

            //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
            Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Write JSON: searchItemArrayList: $searchItemArrayList")
            filesWorker.writeJSON(searchItemArrayList, MainActivity.FILE_SEARCH_ITEM, ctx)

            //определяем время запуска следующей задачи
            var timeDiff = WorkerFindNewsFun().timeDiff() //timeDiff()
            Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Time to start next Work > timeDiff: $timeDiff")
            //timeDiff = Duration.ofSeconds(5) //Delete

            if (!statusInternet) {
                Log.d(TAG, "$logNameClass >f doWork > statusInternet - FALSE")
                //timeDiff = Duration.ofSeconds(30) //Delete
                timeDiff = Duration.ofMinutes(30)
            }

            //формируем данные и новую задачу
            newWorker(timeDiff)
            Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Start next Work > timeDiff: $timeDiff")
            //outputData = newWorker(timeDiff)

            //отправлем уведомление если надо
            Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Run Notification > statusUpdateWorker: $statusUpdateWorker")
            MainServices().notification(statusUpdateWorker, ctx)

            Log.d(TAG, "$logNameClass >f doWork > try > DONE ==================")
        } catch (ex: Exception) {
            Log.d(TAG, "$logNameClass >f doWork > catch > FAILURE")
            Log.e(TAG, "$logNameClass >f doWork > catch > FAILURE: $ex")
            return Result.retry()//Result.failure(); //или Result.retry()
        }*/
        Log.d(TAG_DEBUG, "$logNameClass >f doWork ------------END")
        return Result.success(outputData) //return Result.success(outputData)
    }

    //основная работа
    private fun work(): Data {
        //загружаем список "сохраненных поисков" (SearchItem) для поиска новых новстей -> читаем данные из JSON
        val searchItemArrayList = filesWorker.readJSONSearchItemArrayList(MainActivity.FILE_SEARCH_ITEM, ctx)

        val data: Data

        //открываем Базу Данных (БД) SQLite
        mainDbManager.openDb()

        //проверка интернета
        var statusInternet = true
        var pauseCounter: Long = 0
        var pauseCounterI: Long = 0

        searchItemArrayList.list.forEach { itSearchItemArrayList ->
            //для каждого "сохраненного поиска" ищем новые новости
            val search = itSearchItemArrayList.searchItem.search
            Log.d(TAG, "$logNameClass >f doWork > try > search: $search")

            //запускаем парсинг новостных сайтов/сайта
            val resultParse = parserSites.parse(search, ctx)

            Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > resultParse.statusEthernet: ${resultParse.statusEthernet}")
            //Если инета нет
            if (resultParse.statusEthernet == false.toString()) statusInternet = false

            //если есть новости (любые) (если новостей нет, то ничего не делаем)
            if (resultParse.list.size != 0) {
                //для каждой найденной новости
                resultParse.list.forEach { itResultParse ->
                    Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > itResultParse.link: ${itResultParse.link}")
                    //Ищем совпадения ссылок напрямую в БД
                    //запускаем поиск, функция возвращает нам список найденных элементов > если список пустой то новость уникальная
                    val findNewsItem = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, itResultParse.link)
                    Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > findNewsItem.size: ${findNewsItem.size}")

                    //если совпадений со старыми новостями нет -> новость уникальная ->записываем её в бд  (если новость не уникальная, то ничего не делаем)
                    if (findNewsItem.size == 0) {
                        Log.d(Constants.TAG_DATA_IF, "$logNameClass >f doWork > try > IF > insertToDb: $findNewsItem")
                        //записываем новость в бд
                        mainDbManager.insertToDb(
                            itResultParse.search,
                            itResultParse.img,
                            itResultParse.date,
                            itResultParse.title,
                            itResultParse.content,
                            itResultParse.link,
                            itResultParse.statusSaved)
                        //увеличиваем счетчик новых новостей
                        itSearchItemArrayList.searchItem.counterNewNews++

                        //меняем статус "есть ли новые новости" на "да"
                        statusUpdateWorker = true
                    }
                }
            }
            //Считаем общее количество новостей
            itSearchItemArrayList.searchItem.counterAllNews = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_SEARCH, search).size

            //Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
            Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Write JSON: searchItemArrayList: $searchItemArrayList")
            filesWorker.writeJSON(searchItemArrayList, MainActivity.FILE_SEARCH_ITEM, ctx)

            //после парсинга одного "сохраненного поиска" немного ждем
            Log.d(TAG, "$logNameClass >f doWork > PAUSE START")
            if (pauseCounter < 600) {
                pauseCounterI++
                pauseCounter += 30 + pauseCounterI
            }
            else {
                pauseCounter = 0
            }
            Log.d(TAG, "$logNameClass >f doWork > PAUSE > pauseCounter: $pauseCounter")
            TimeUnit.SECONDS.sleep(pauseCounter)
            //Thread.sleep(300_000) //TimeUnit.MINUTES.sleep(5) //одно и тоже
            Log.d(TAG, "$logNameClass >f doWork > PAUSE END")
        }

        /*//Записываем обновленный список "сохраненных поисков" (счетчики) обратно в JSON
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Write JSON: searchItemArrayList: $searchItemArrayList")
        filesWorker.writeJSON(searchItemArrayList, MainActivity.FILE_SEARCH_ITEM, ctx)*/

        //определяем время запуска следующей задачи
        var timeDiff = WorkerFindNewsFun().timeDiff() //timeDiff()
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Time to start next Work > timeDiff: $timeDiff")
        //timeDiff = Duration.ofSeconds(5) //Delete

        if (!statusInternet) {
            Log.d(TAG, "$logNameClass >f doWork > statusInternet - FALSE")
            //timeDiff = Duration.ofSeconds(30) //Delete
            timeDiff = Duration.ofMinutes(30)
        }

        //формируем данные и новую задачу
        data = newWorker(timeDiff)
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Start next Work > timeDiff: $timeDiff")
        //outputData = newWorker(timeDiff)

        //отправлем уведомление если надо
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Run Notification > statusUpdateWorker: $statusUpdateWorker")
        MainServices().notification(statusUpdateWorker, ctx)

        Log.d(TAG, "$logNameClass >f doWork > try > DONE ==================")
        return data
    }

    //Новая задача
    private fun newWorker(timeDiff: Duration): Data {
        //подготовка к новой итерации Задачи

        //подготавливаем полученные и старые данные для вывода из Задачи и передачи в следуюущее Задачу
        val outputData = Data.Builder()
            .putBoolean(WORKER_PUT_STATUS_UPDATE, statusUpdateWorker) //статус новых новостей
            .build()
        //Log.d(TAG, "$logNameClass >f newWorker > outputData")

        //Критерии запуска
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
            .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - WiFi или Mobile Data
            .build()
        //Log.d(TAG, "$logNameClass >f newWorker > constraints")

        //Сборка Задачи и запуск в определенное время +- (главное не час-пик, для снижения нагрузки на сервер)
        val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
            .setConstraints(constraints)
            .setInitialDelay(timeDiff)
            .addTag(WORKER_TAG_PARSER)
            .setInputData(outputData)
            .build()

        //Delete >
        val  myWorkRequestMinute = OneTimeWorkRequestBuilder<WorkerFindNews>() //test
            .setConstraints(constraints)
            .setInitialDelay(10, TimeUnit.MINUTES)
            .addTag(WORKER_TAG_PARSER)
            .setInputData(outputData)
            .build()
        //Delete ^

        //Запускаем новую Задачу
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork(WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest)
        Log.d(TAG_DEBUG, "$logNameClass >f newWorker ----- END")
        return outputData
    }
}

class WorkerFindNewsFun(){
    private val logNameClass = "WorkerFindNewsFun"

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, активити и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
        const val TAG_DATA_IF = Constants.TAG_DATA_IF //переменные и данные в циклах
    }

    //рассчитываем время
    fun timeDiff(): Duration{
        Log.d(TAG_DEBUG, "$logNameClass >f timeDiff === START")
        Log.d(TAG_DEBUG, "$logNameClass >f timeDiff // рассчитываем время")

        val timeNow = LocalTime.now()
        val rndMinute = (10..59).random() // generated random from 10 to 59 included
        val rndSecond = (10..59).random() // generated random from 10 to 59 included
        val timeNight = LocalTime.parse("03:$rndMinute:$rndSecond") //Время ночного запуска - 03:XX:XX
        //val timeNoon = LocalTime.parse("13:$rndMinute:$rndSecond") //Время дневного запуска - 13:XX:XX
        val timeNoon = LocalTime.parse("11:35:38") //test

        val timeDiff: Duration//Duration.ofHours(24).minus(Duration.between(timeNow, timeNight))

        Log.d(TAG_DATA, "$logNameClass >f timeDiff > timeNow: $timeNow")

        if(timeNow in timeNight..timeNoon){
            timeDiff = Duration.between(timeNow, timeNoon)
            Log.d(TAG_DATA, "$logNameClass >f timeDiff > Вечер-Ночь > timeDuration: $timeDiff")
        }
        else if(timeNow >= timeNoon) {
            timeDiff = Duration.ofHours(24).plus(Duration.between(timeNow, timeNight)) //
            Log.d(TAG_DATA, "$logNameClass >f timeDiff > Утро > timeDuration: $timeDiff")
        }
        else {
            timeDiff = Duration.between(timeNow, timeNight)
            Log.d(TAG_DATA, "$logNameClass >f timeDiff > Ночь > timeDuration: $timeDiff")
        }
        Log.d(TAG_DATA, "$logNameClass >f timeDiff > timeDiff: $timeDiff")
        Log.d(TAG_DEBUG, "$logNameClass >f timeDiff ----- END")
        return timeDiff
    }

    //запуск первой worker (задачи), которая уже сама сделает новые повторяющиеся задачи
    //при каждом заходе в приложение и любом запуске поиска -> запускать заново
    fun workerFindNewsFirst(context: Context) {
        Log.d(TAG_DEBUG, "$logNameClass >f workerFindNewsFirst === START")
        Log.d(TAG_DEBUG, "$logNameClass >f workerFindNewsFirst // запуск первой worker (задачи), которая уже сама сделает новые повторяющиеся задачи")

        //очищаем предыдущие задачи
        WorkManager.getInstance(context).cancelAllWorkByTag(WorkerFindNews.WORKER_TAG_PARSER)
        WorkManager.getInstance(context).cancelUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER)

        //Критерии
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического //.setRequiresDeviceIdle(true) //девайс ушел в спячку
            .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - WiFi или Mobile Data
            .build()

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

        val  periodicWorkRequest = PeriodicWorkRequestBuilder<WorkerFindNews>(20L, TimeUnit.MINUTES)
            .addTag(WorkerFindNews.WORKER_TAG_PARSER)
            .setConstraints(constraints)
            .setInputData(dataForWorker)
            .build()

        //Delete >
        val  myWorkRequestMinute = OneTimeWorkRequestBuilder<WorkerFindNews>() //test
            .addTag(WorkerFindNews.WORKER_TAG_PARSER)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(dataForWorker)
            .build()
        //Delete ^

        //Запускаем Задачу
        WorkManager.getInstance(context).enqueueUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest) //для единоразового запуска
        //WorkManager.getInstance(this).enqueue(myWorkRequest) //почему-то запускается несколько раз


        //Запускаем Задачу
        //WorkManager.getInstance(context).enqueueUniquePeriodicWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER, ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest) //периодческий запуск

        Log.d(TAG_DEBUG, "$logNameClass >f workerFindNewsFirst ----- END")
    }
}

// BACKUP >
/*
class WorkerFindNews() : Worker() {
    @NonNull
    override fun doWork(): WorkerResult {
        Log.d(TAG, "WorkerFindNews >f doWork: start")
        try {
            TimeUnit.SECONDS.sleep(10)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.d(TAG, "WorkerFindNews >f doWork: end")
        return WorkerResult.SUCCESS
    }
    */
/*override fun doWork(): WorkerResult {
        Log.d(TAG, "WorkerFindNews >f doWork: start")
        try {
            for (i in 0..9) {
                TimeUnit.SECONDS.sleep(1)
                Log.d(TAG, "$i, isStopped $isStopped")
                if (isStopped) return WorkerResult.FAILURE
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.d(TAG, "WorkerFindNews >f doWork: end")
        return WorkerResult.SUCCESS
    }

    override fun onStopped() {
        super.onStopped()
        Log.d(TAG, "WorkerFindNews >f onStopped")
    }*//*


    companion object {
        const val TAG = "TAG1"
    }
}*/
/*private fun newWorker(resultParse: ParserSites.ResultParse): Data {
    //подготовка к новой итерации Задачи
    Log.d(TAG, "WorkerFindNews >f newWorker ======START")

    val arrayString = emptyArray<String>()

    var arrayId = emptyArray<Int>()
    var arrayDate = emptyArray<String>()
    var arrayTitle = emptyArray<String>()
    var arrayContent = emptyArray<String>()
    var arrayLink = emptyArray<String>()
    val arrayLinkOldWorker = inputData.getStringArray(WORKER_PUT_LINK)

    //проверям новые ссылки - есть ли совпадения с БД
    resultParse.list.forEach { itNewsItem ->
        var checkUnique = true
        var checkUniqueUpdate = true

        //проверям со старыми сслыками из БД
        arrayStringLinkSQL?.forEach { itLink ->
            //Log.d(TAG, "WorkerFindNews >f doWork > forEach > == ${itLink == itNewsItem.link}")
            //Log.d(TAG, "WorkerFindNews >f doWork > forEach > == ${itNewsItem.link.equals(itLink, true)}")
            if(itLink == itNewsItem.link) checkUnique = false
        }
        //если есть обновления в предыдущей задаче то проверям и новые ссылки
        if (statusUpdateWorker) {
            arrayLinkOldWorker?.forEach { itLinkOld ->
                //Log.d(TAG, "WorkerFindNews >f doWork > forEach New > == ${itLinkOld == itNewsItem.link}")
                //Log.d(TAG, "WorkerFindNews >f doWork > forEach New > == ${itNewsItem.link.equals(itLinkOld, true)}")
                if(itLinkOld == itNewsItem.link) checkUniqueUpdate = false
            }
        }

        //delete >
        checkUnique = true
        checkUniqueUpdate = true
        //delete ^

        //если есть новые ссылки добавляем новый элемент NewsItem
        if (checkUnique && checkUniqueUpdate) {
            statusUpdateWorker = true
            arrayId += itNewsItem.id + 1
            arrayDate += itNewsItem.date
            arrayTitle += itNewsItem.title
            arrayContent += itNewsItem.content
            arrayLink += itNewsItem.link
        }
    }

    //подготавливаем полученные и старые данные для вывода из Задачи и передачи в следуюущее Задачу
    val outputData = Data.Builder()
        .putIntArray(WORKER_PUT_ID, arrayId.toIntArray())
        .putStringArray(WORKER_PUT_DATE, arrayDate)
        .putStringArray(WORKER_PUT_TITLE, arrayTitle)
        .putStringArray(WORKER_PUT_CONTENT, arrayContent)
        .putStringArray(WORKER_PUT_LINK, arrayLink)
        .putStringArray(WORKER_PUT_LINK_SQL, arrayStringLinkSQL ?: arrayLink)
        .putBoolean(WORKER_PUT_STATUS_UPDATE, statusUpdateWorker)
        .putInt(TEST_WORKER_PUT_COUNTER, testCounter)
        .build()
    Log.d(TAG, "WorkerFindNews >f newWorker > outputData")

    //определяем время запуска
    val timeNow = LocalTime.now()
    val rndMinute = (10..59).random() // generated random from 10 to 59 included
    val rndSecond = (10..59).random() // generated random from 10 to 59 included
    //val timeFiveAM = LocalTime.parse("00:$rndMinute:$rndSecond")
    val timeFiveAM = LocalTime.parse("00:01:00")
    val timeBetween = Duration.between(timeNow, timeFiveAM)
    val timeBetween1 = Duration.between(timeFiveAM, timeNow)
    val timeDiff = Duration.ofHours(24).minus(Duration.between(timeFiveAM, timeNow))
    val timeDiffMinute = Duration.ofMinutes(60).minus(Duration.between(timeFiveAM, timeNow))

    //Критерии запуска
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
        .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - только WiFi
        .build()
    Log.d(TAG, "WorkerFindNews >f newWorker > constraints")


    //Log.d(TAG, "WorkerFindNews >f doWork > applicationContext: $applicationContext")
    //Log.d(TAG, "WorkerFindNews >f doWork > getContext: ${getContext()}")
    //Log.d(TAG, "WorkerFindNews >f doWork > timeDiff: $timeDiffMinute, timeFiveAM: $timeFiveAM, timeNow: $timeNow, duration: ${Duration.between(timeFiveAM, timeNow)}")


    //Сборка Задачи и запуск в определенное время +- (главное не час-пик, для снижения нагрузки на сервер)
    val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
        .setConstraints(constraints)
        .setInitialDelay(timeDiff)
        .addTag(WORKER_TAG_PARSER)
        .setInputData(outputData)
        .build()

    val  myWorkRequestMinute = OneTimeWorkRequestBuilder<WorkerFindNews>() //test
        .setConstraints(constraints)
        .setInitialDelay(1, TimeUnit.MINUTES)
        .addTag(WORKER_TAG_PARSER)
        .setInputData(outputData)
        .build()
    Log.d(TAG, "WorkerFindNews >f newWorker > myWorkRequest")

    //Запускаем новую Задачу
    WorkManager.getInstance(ctx)
        .enqueueUniqueWork(WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequestMinute)
    Log.d(TAG, "WorkerFindNews >f newWorker ------------END")

    return outputData
}*/
// BACKUP ^


