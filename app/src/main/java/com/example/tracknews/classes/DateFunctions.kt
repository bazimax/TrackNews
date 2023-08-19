package com.example.tracknews.classes

import android.content.Context
import com.example.tracknews.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class DateFunctions {
    fun parseNewsItemDate(date: String, ctx: Context): Array<String>{
        val locale = Locale.getDefault().language
        val dateParse = LocalDate.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val timeParse = LocalTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val textToday = ctx.resources.getString(R.string.timeToday)
        val textYesterday = ctx.resources.getString(R.string.timeYesterday)
        val dateNow = LocalDate.now()

        //Часы и минуты (обрезаем секунды)
        val timeHHmm = timeParse.toString().removeRange(5, timeParse.toString().length)

        //Месяц
        val arrayMonth = ctx.resources.getStringArray(R.array.timeMonth)
        //Сопоставляем месяц
        var dateMonth = when(dateParse.month.value) {
            1 -> arrayMonth[0] //January
            2 -> arrayMonth[1] //
            3 -> arrayMonth[2] //
            4 -> arrayMonth[3] //
            5 -> arrayMonth[4] //
            6 -> arrayMonth[5] //
            7 -> arrayMonth[6] //
            8 -> arrayMonth[7] //
            9 -> arrayMonth[8] //
            10 -> arrayMonth[9] //
            11 -> arrayMonth[10] //
            12 -> arrayMonth[11] //December
            else -> ""
        }

        //Форматируем месяц
        dateMonth = dateMonth.replaceFirstChar { it.titlecase() }
        dateMonth = dateMonth.removeRange(3, dateMonth.toString().length)

        //День и месяц
        //меняем положение дня и месяца в зависимости от языка
        var dateDDmm: String = when(locale) {
            "en" -> "$dateMonth ${dateParse.dayOfMonth}"
            "ru" -> "${dateParse.dayOfMonth} $dateMonth"
            else -> "${dateParse.dayOfMonth} $dateMonth"
        }

        val outputDateTime = arrayOf("00:00", "Jan 1", "2012")

        //Собираем дату и время
        //проверяем насколько актуальная дата
        dateDDmm = when (dateParse) {
            dateNow -> textToday //сегодня
            dateNow.minusDays(1) -> textYesterday //вчера
            else -> dateDDmm //в этом месяце
        }
        outputDateTime[0] = timeHHmm
        outputDateTime[1] = dateDDmm
        //Если год нынешний, то не пишем. Если нет, то пишем полную дату с годом
        if (dateParse.year == dateNow.year) {
            outputDateTime[2] = ""
        }
        else outputDateTime[2] = dateParse.year.toString()

        return outputDateTime
    }
}