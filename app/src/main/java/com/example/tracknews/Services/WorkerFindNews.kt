package com.example.tracknews.Services

//import android.support.annotation.NonNull
//import androidx.work.WorkRequest
//import androidx.work.Worker
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.tracknews.MainActivity
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.FilesWorker
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.SearchItem
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites
import java.io.*
import java.security.AccessController.getContext
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit


class WorkerFindNews(context: Context, params: WorkerParameters) : Worker(context, params) {

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG

        //Worker
        const val WORKER_TAG_PARSER = "parser"
        const val WORKER_UNIQUE_NAME_PARSER = "uniqueParser"
        const val WORKER_PUT_ID = "id"
        const val WORKER_PUT_IMG = "img"
        const val WORKER_PUT_DATE = "date"
        const val WORKER_PUT_TITLE = "title"
        const val WORKER_PUT_CONTENT = "content"
        const val WORKER_PUT_LINK = "link"
        const val WORKER_PUT_LINK_SQL = "linkSQL"
        const val WORKER_PUT_STATUS_SAVED = "statusSaved"

        const val TEST_WORKER_PUT_COUNTER = "counter"
        const val WORKER_PUT_STATUS_UPDATE = "statusUpdate"
    }

    //private val sharedPrefs = context.getSharedPreferences("init", Context.MODE_PRIVATE) //getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    private val mainDbManager = MainDbManager(context) //База Данных (БД)

    //private var testCounter = inputData.getInt(TEST_WORKER_PUT_COUNTER, 0)
    //private val appContext = applicationContext

    private val parserSites = ParserSites() //парсинг
    private val ctx = context
    private val filesWorker = FilesWorker(ctx) //работа с файлами (чтение и запись)
    //private val testSiteString = readFromFile(ctx) //delete
    //private var arrayStringLinkSQL = inputData.getStringArray(WORKER_PUT_LINK_SQL) //ссылки из БД с которыми будет сравнивать
    private var statusUpdateWorker = inputData.getBoolean(WORKER_PUT_STATUS_UPDATE, false) //есть ли новые новости-NewsItem (по умолчанию нет)

    @NonNull
    override fun doWork(): Result {
        Log.d(TAG, "WorkerFindNews >f doWork ======START")

        val outputData : Data
        //testCounter++

        try {
            //загружаем список "сохраненных поисков" (SearchItem) для поиска новых новстей -> читаем данные из JSON
            val searchItemArrayList = filesWorker.readJSONSearchItemArrayList(MainActivity.FILE_SEARCH_ITEM, ctx)

            //открываем Базу Данных (БД) SQLite
            mainDbManager.openDb()

            searchItemArrayList.list.forEach { itSearchItemArrayList ->
                //для каждого "сохраненного поиска" ищем новые новости
                val search = itSearchItemArrayList.searchItem.search
                Log.d(TAG, "WorkerFindNews >f doWork > try > search: $search")

                //Delete >
                //запускаем парсинг
                val testSiteString = filesWorker.readFromFile("testSite.txt", ctx)
                //val resultParse = testSiteString?.let { parserSites.testParse("witcher", it) } ?: parserSites.testParse("witcher", "") //запасной вариант
                val resultParse = parserSites.testParse(search, testSiteString)
                //Delete ^

                //запускаем парсинг новостных сайтов/сайта
                //val resultParse = parserSites.parse(search)
                //Если инета нет
                //if (resultParse.statusEthernet == false.toString()) {}

                //если есть новости (любые) (если новостей нет, то ничего не делаем)
                if (resultParse.list.size != 0) {
                    //для каждой найденной новости
                    resultParse.list.forEach { itResultParse ->
                        //Ищем совпадения ссылок напрямую в БД
                        val findNewsItem = mainDbManager.findItemInDb(MainDbNameObject.COLUMN_NAME_LINK, itResultParse.link)
                        //Log.d(TAG, "WorkerFindNews >f doWork > try > findNewsItem.size: ${findNewsItem.size}")

                        //если совпадений со старыми новостями нет -> новость уникальная (если новость не уникальная, то ничего не делаем)
                        if (findNewsItem.size == 0) {
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
                            itSearchItemArrayList.counterNewNews++
                            //меняем статус "есть ли новые новости" на "да"
                            statusUpdateWorker = true
                        }
                    }
                }
                //после парсинга одного "сохраненного поиска" немного ждем
                TimeUnit.MINUTES.sleep(5)
                //TimeUnit.SECONDS.sleep(1)
            }

            //формируем данные и новую задачу
            //outputData = newWorker(resultParse)
            outputData = newWorker1()

            //отправлем уведомление если надо
            notification()

            Log.d(TAG, "WorkerFindNews >f doWork > try > DONE ==================")
        } catch (ex: Exception) {
            Log.d(TAG, "WorkerFindNews >f doWork > catch > FAILURE")
            Log.e(TAG, "WorkerFindNews >f doWork > catch > FAILURE: $ex")
            return Result.retry()//Result.failure(); //или Result.retry()
        }
        //Log.d(TAG, "WorkerFindNews >f doWork ------------END")
        return Result.success(outputData)
    }

    private fun readDb(): ArrayList<NewsItem>{
        Log.d(TAG, "WorkerFindNews >f dbWork ======START")
        //открываем Базу Данных (БД) SQLite
        mainDbManager.openDb()

        //читаем БД
        val newsItemList = mainDbManager.readDbData()

        Log.d(TAG, "WorkerFindNews >f dbWork ------------END")
        return newsItemList
    }

    private fun writeToDb(newsItemList: ArrayList<NewsItem>){
        //Записываем в БД
        newsItemList.forEach {
            val search = it.search
            val img = it.img
            val date = it.date
            val title= it.title
            val content = it.content
            val link = it.link
            val statusSaved = it.statusSaved
            mainDbManager.insertToDb(search ,img, date, title, content, link, statusSaved)
        }
        mainDbManager.closeDb() //закрываем БД (доступ к БД?)
    }

    private fun newWorker1(): Data {
        //подготовка к новой итерации Задачи

        //подготавливаем полученные и старые данные для вывода из Задачи и передачи в следуюущее Задачу
        val outputData = Data.Builder()
            .putBoolean(WORKER_PUT_STATUS_UPDATE, statusUpdateWorker) //статус новых новостей
            .build()
        //Log.d(TAG, "WorkerFindNews >f newWorker > outputData")

        //Критерии запуска
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
            .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - только WiFi
            .build()
        //Log.d(TAG, "WorkerFindNews >f newWorker > constraints")

        //определяем время запуска
        val timeDiff = WorkerFindNewsFun().timeDiff() //timeDiff()

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
            .setInitialDelay(10, TimeUnit.SECONDS)
            .addTag(WORKER_TAG_PARSER)
            .setInputData(outputData)
            .build()
        //Log.d(TAG, "WorkerFindNews >f newWorker > myWorkRequest")
        //Delete ^

        //Запускаем новую Задачу
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork(WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequestMinute)
        //Log.d(TAG, "WorkerFindNews >f newWorker ------------END")

        return outputData
    }

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

    fun timeDiff(): Duration{
        //определяем время запуска
        val timeNow = LocalTime.now()
        val rndMinute = (10..59).random() // generated random from 10 to 59 included
        val rndSecond = (10..59).random() // generated random from 10 to 59 included
        val timeNight = LocalTime.parse("03:$rndMinute:$rndSecond") //Время ночного запуска
        val timeNoon = LocalTime.parse("13:$rndMinute:$rndSecond") //Время дневного запуска

        var timeDiff: Duration//Duration.ofHours(24).minus(Duration.between(timeNow, timeNight))

        Log.d(TAG, "WorkerFindNews >f doWork > timeNow: $timeNow")

        if(timeNow in timeNight..timeNoon){
            timeDiff = Duration.between(timeNow, timeNoon)
            Log.d(TAG, "WorkerFindNews >f doWork > Вечер-Ночь > timeDuration: $timeDiff")
        }
        else if(timeNow >= timeNoon) {
            timeDiff = Duration.ofHours(24).plus(Duration.between(timeNow, timeNight)) //
            Log.d(TAG, "WorkerFindNews >f doWork > Утро > timeDuration: $timeDiff")
        }
        else {
            timeDiff = Duration.between(timeNow, timeNight)
            Log.d(TAG, "WorkerFindNews >f doWork > Ночь > timeDuration: $timeDiff")
        }
        return timeDiff
    }

    private fun notification(){
        //Уведомления
        //Log.d(TAG, "WorkerFindNews >f notification ======START")
        //Log.d(TAG, "WorkerFindNews >f notification > IF > Счетчик: $testCounter")
        //Если есть новые новости - создаём уведомление
        if (statusUpdateWorker) {
            val builder = NotificationCompat.Builder(ctx, MainActivity.CHANNEL_ID)
                .setSmallIcon(com.example.tracknews.R.drawable.ic_dark_mode)
                .setContentTitle("Find News")
                .setContentText("Найдены новости")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            //val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            //val notificationManager = NotificationManagerCompat.from(this)

            //посылаем уведомление
            //notificationManager.notify(NOTIFICATION_ID, builder.build())
            //или
            with(NotificationManagerCompat.from(ctx)) {
                notify(MainActivity.NOTIFICATION_ID, builder.build())
            }
        }
        //Log.d(TAG, "WorkerFindNews >f notification ------------END")
    }
}

class WorkerFindNewsFun(){
    fun timeDiff(): Duration{
        //определяем время запуска
        //Log.d(WorkerFindNews.TAG, "WorkerFindNewsFun >f timeDiff ======START")

        val timeNow = LocalTime.now()
        val rndMinute = (10..59).random() // generated random from 10 to 59 included
        val rndSecond = (10..59).random() // generated random from 10 to 59 included
        val timeNight = LocalTime.parse("03:$rndMinute:$rndSecond") //Время ночного запуска
        val timeNoon = LocalTime.parse("13:$rndMinute:$rndSecond") //Время дневного запуска

        val timeDiff: Duration//Duration.ofHours(24).minus(Duration.between(timeNow, timeNight))

        Log.d(WorkerFindNews.TAG, "WorkerFindNewsFun >f timeDiff > timeNow: $timeNow")

        if(timeNow in timeNight..timeNoon){
            timeDiff = Duration.between(timeNow, timeNoon)
            Log.d(WorkerFindNews.TAG, "WorkerFindNewsFun >f timeDiff > Вечер-Ночь > timeDuration: $timeDiff")
        }
        else if(timeNow >= timeNoon) {
            timeDiff = Duration.ofHours(24).plus(Duration.between(timeNow, timeNight)) //
            Log.d(WorkerFindNews.TAG, "WorkerFindNewsFun >f timeDiff > Утро > timeDuration: $timeDiff")
        }
        else {
            timeDiff = Duration.between(timeNow, timeNight)
            Log.d(WorkerFindNews.TAG, "WorkerFindNewsFun >f timeDiff > Ночь > timeDuration: $timeDiff")
        }
        //Log.d(WorkerFindNews.TAG, "WorkerFindNewsFun >f timeDiff ------------END")
        return timeDiff
    }
}

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
