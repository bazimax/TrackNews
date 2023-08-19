package com.example.tracknews.services


import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.tracknews.MainActivity
import com.example.tracknews.classes.Constants
import com.example.tracknews.classes.FilesWorker
import com.example.tracknews.db.MainDbManager
import com.example.tracknews.db.MainDbNameObject
import com.example.tracknews.parseSite.ParserSites
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

    private val mainDbManager = MainDbManager(context) //База Данных (БД)


    private val parserSites = ParserSites() //парсинг
    private val ctx = context
    private val filesWorker = FilesWorker() //работа с файлами (чтение и запись)
    private var statusUpdateWorker = inputData.getBoolean(WORKER_PUT_STATUS_UPDATE, false) //есть ли новые новости-NewsItem (по умолчанию нет)

    //?? @NonNull //import androidx.annotation.NonNull
    override fun doWork(): Result {
        Log.d(TAG_DEBUG, "$logNameClass >f doWork === START")

        val outputData : Data

        try {
            outputData = work()
        } catch (ex: Exception) {
            Log.d(TAG, "$logNameClass >f doWork > catch > FAILURE")
            Log.e(TAG, "$logNameClass >f doWork > catch > FAILURE: $ex")
            return Result.retry()
        }

        Log.d(TAG_DEBUG, "$logNameClass >f doWork ------------END")
        return Result.success(outputData)
    }

    //основная работа
    private fun work(): Data {
        //загружаем список "сохраненных поисков" (SearchItem) для поиска новых новостей -> читаем данные из JSON
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
            //Если интернета нет
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

                    //если совпадений со старыми новостями нет -> новость уникальная ->записываем её в бд (если новость не уникальная, то ничего не делаем)
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

        //определяем время запуска следующей задачи
        var timeDiff = WorkerFindNewsFun().timeDiff() //timeDiff()
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Time to start next Work > timeDiff: $timeDiff")

        if (!statusInternet) {
            Log.d(TAG, "$logNameClass >f doWork > statusInternet - FALSE")
            //timeDiff = Duration.ofSeconds(30) //Delete
            timeDiff = Duration.ofMinutes(30)
        }

        //формируем данные и новую задачу
        data = newWorker(timeDiff)
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Start next Work > timeDiff: $timeDiff")
        //outputData = newWorker(timeDiff)

        //отправляем уведомление если надо
        Log.d(Constants.TAG_DATA, "$logNameClass >f doWork > Run Notification > statusUpdateWorker: $statusUpdateWorker")
        MainServices().notification(statusUpdateWorker, ctx)

        Log.d(TAG, "$logNameClass >f doWork > try > DONE ==================")
        return data
    }

    //Новая задача
    private fun newWorker(timeDiff: Duration): Data {
        //подготовка к новой итерации Задачи

        //подготавливаем полученные и старые данные для вывода из Задачи и передачи в следующую Задачу
        val outputData = Data.Builder()
            .putBoolean(WORKER_PUT_STATUS_UPDATE, statusUpdateWorker) //статус новых новостей
            .build()

        //Критерии запуска
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) //уровень батареи не ниже критического
            .setRequiredNetworkType(NetworkType.CONNECTED) //наличие интернета - WiFi или Mobile Data
            .build()

        //Сборка Задачи и запуск в определенное время +- (главное не час-пик, для снижения нагрузки на сервер)
        val  myWorkRequest = OneTimeWorkRequestBuilder<WorkerFindNews>()
            .setConstraints(constraints)
            .setInitialDelay(timeDiff)
            .addTag(WORKER_TAG_PARSER)
            .setInputData(outputData)
            .build()

        //Запускаем новую Задачу
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork(WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest)
        Log.d(TAG_DEBUG, "$logNameClass >f newWorker ----- END")
        return outputData
    }
}

class WorkerFindNewsFun{
    private val logNameClass = "WorkerFindNewsFun"

    //КОНСТАНТЫ
    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
    }

    //рассчитываем время
    fun timeDiff(): Duration{
        Log.d(TAG_DEBUG, "$logNameClass >f timeDiff === START")
        Log.d(TAG_DEBUG, "$logNameClass >f timeDiff // рассчитываем время")

        val timeNow = LocalTime.now()
        val rndMinute = (10..59).random() // generated random from 10 to 59 included
        val rndSecond = (10..59).random() // generated random from 10 to 59 included
        val timeNight = LocalTime.parse("03:$rndMinute:$rndSecond") //Время ночного запуска - 03:XX:XX
        val timeNoon = LocalTime.parse("13:$rndMinute:$rndSecond") //Время дневного запуска - 13:XX:XX

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

        //Запускаем Задачу
        WorkManager.getInstance(context).enqueueUniqueWork(WorkerFindNews.WORKER_UNIQUE_NAME_PARSER, ExistingWorkPolicy.REPLACE, myWorkRequest) //для единоразового запуска

        Log.d(TAG_DEBUG, "$logNameClass >f workerFindNewsFirst ----- END")
    }
}


