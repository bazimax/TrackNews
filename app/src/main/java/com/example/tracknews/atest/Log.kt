package com.example.tracknews.atest

import android.util.Log
import com.example.tracknews.BuildConfig
import com.example.tracknews.classes.Constants


/**
 * Обозначения пометок к комментариям:
 *
 * //!! Важно - важный момент, за которым нужно следить
 * //?? - код вроде важен, необходим комментарий
 * //Delete - временные данные, удалить после окончания разработки
 * //Delete? - проверить, возможно элемент не нужен и его можно удалить
 * //TEST - тестовый элемент
 * //WORK - элемент еще не готов и находится в разработке
 * //Zapas - код, который может быть полезен и оставлен про запас
 *
 * //Recycler View > - начало группы
 * ...
 * //Recycler View ^ - конец группы
 * значок ">" указывает, что комментарий относится ко всему дальнейшему коду до значка "^"
*/

/**
 *Использование логов
 * Для каждой функции в начале и в конце ставим логи чала (срабатывает при создании нового объекта класса "MyLog") и
 * конца (MyLog.end), чтобы отслеживать, отработала функция или нет.
 *
 * При запуске activity - при создании объекта "MyLog" добавляем launch = true (будет более заметно)
 *
 * logNameClass (lnc) - имя класса в котором запускается функция
 * function - имя самой функции
 * msgStart - сообщение на старте //??
 * msg - сообщение
 */

const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функций, activity и тд

//!! некоторые функции еще не используются, так как логи в процессе переписывания

//простой быстрый лог
fun logD(text: String){

    // Режим отладки, ведём логи
    if (BuildConfig.DEBUG) {
        Log.d(TAG_DEBUG, text)
    }
}
fun logI(text: String){

    // Режим отладки, ведём логи
    if (BuildConfig.DEBUG) {
        Log.i(TAG_DEBUG, text)
    }
}

//второй вариант логов //!! подумать над расширением стандартного Log
class MyLog(private var logNameClass: String, private var function: String, msgStart: String = "", launch: Boolean = false){

    companion object {
        //log
        const val TAG = Constants.TAG //разное
        const val TAG_DEBUG = Constants.TAG_DEBUG //запуск функция, activity и тд
        const val TAG_DATA = Constants.TAG_DATA //переменные и данные
        const val TAG_DATA_BIG = Constants.TAG_DATA_BIG//объемные данные
        const val TAG_DATA_EACH = Constants.TAG_DATA_IF //переменные и данные в циклах
        const val TAG_ERROR = Constants.TAG_ERROR //переменные и данные в циклах
    }

    //private var logNameClass: String = function2
    //private var function: String
    //отметка начала работы функции

    init {

        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            //Если это запуск activity
            if (launch) {
                Log.d(TAG_DEBUG, "$logNameClass >f $function ======================== >" +
                        correctText("$logNameClass >f $function","======") +
                        correctText("$logNameClass >f $function","======") +
                        correctText("$logNameClass >f $function","======") +
                        correctText("$logNameClass >f $function","====== LAUNCH") +
                        correctText("$logNameClass >f $function",msgStart))
            }
            //если обычная функция
            else Log.d(TAG_DEBUG, "$logNameClass >f $function === START" +
                    correctText("$logNameClass >f $function",msgStart))
        }
    }

    //Конструктор для вложенных логов
    constructor(myLog: MyLog,
                childFunction: String,
                logNameClass: String = myLog.logNameClass,
                function: String = "${myLog.function} > $childFunction",
                msgStart: String = "",
                launch: Boolean = false
    ) : this(logNameClass, function, msgStart, launch)

    //отметка окончания работы функции
    fun end(msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DEBUG, "$logNameClass >f $function ----- END" +
                    correctText("$logNameClass >f $function",msg))
        }
    }

    //отметка без типа
    fun d(msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            //val t = if (text != "") " // $text" else ""
            Log.d(TAG_DEBUG, "$logNameClass >f $function > $msg")
        }
    }

    //отслеживание данных
    fun data(data: String, msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DATA, "$logNameClass >f $function > data:: $data" +
                    correctText("$logNameClass >f $function",msg)
            )
        }
    }

    //отслеживание больших данных
    fun bigData(bigData: String, msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DATA_BIG, "$logNameClass >f $function > dataBig:: $bigData" +
                    correctText("$logNameClass >f $function",msg)
            )
        }
    }

    //отслеживание данных в циклах
    fun eachData(eachData: String, msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            Log.d(TAG_DATA_EACH, "$logNameClass >f $function > dataEach:: $eachData" +
                    correctText("$logNameClass >f $function",msg)
            )
        }
    }

    //сообщение об ошибке / Log.e
    fun error(textError: String, msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            Log.e(TAG_ERROR, "$logNameClass >f $function > ERROR: $textError" +
                    correctText("$logNameClass >f $function",msg))
        }
    }

    //отметка info / Log.i //WORK -> correctText
    fun i(msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            //val t = if (text != "") " // $text" else ""
            Log.i(TAG_DEBUG, "$logNameClass >f $function > $msg")
        }
    }

    //отметка warning / Log.w //WORK -> correctText
    fun w(msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            //val t = if (text != "") " // $text" else ""
            Log.w(TAG_DEBUG, "$logNameClass >f $function > $msg")
        }
    }

    //отметка verbose / Log.v //WORK -> correctText
    fun v(msg: String = ""){
        // Режим отладки, ведём логи
        if (BuildConfig.DEBUG) {
            //val t = if (text != "") " // $text" else ""
            Log.v(TAG_DEBUG, "$logNameClass >f $function > $msg")
        }
    }

    //перемещаем текст на новую строку и смещаем на длину tabString
    private fun correctText(tabString: String, text: String): String {
        return if (text != "") "\n${"".padStart(tabString.count() - 1, ' ')}* " + text else ""

    }
}