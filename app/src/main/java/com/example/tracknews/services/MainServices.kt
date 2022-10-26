package com.example.tracknews.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tracknews.MainActivity
import com.example.tracknews.classes.Constants

class MainServices {
    private val logNameClass = "MainServices" //для логов



    fun notification(checkStart: Boolean, context: Context){

        val intent = Intent(context, MainActivity::class.java)
        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        //Уведомления
        //val mess = ctx.resources.getString(com.example.tracknews.R.string.loadWebsiteFail)//resources.getString(com.example.tracknews.R.string.loadWebsiteFail)
        //Log.d(TAG, "WorkerFindNews >f notification ======START")
        //Log.d(TAG, "WorkerFindNews >f notification > IF > Счетчик: $testCounter")
        //Если есть новые новости - создаём уведомление
        if (checkStart) {
            val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(com.example.tracknews.R.drawable.ic_dark_mode)
                .setContentTitle("Find News")
                .setContentText("Найдены новости")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

            //val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            //val notificationManager = NotificationManagerCompat.from(this)

            //посылаем уведомление
            //notificationManager.notify(NOTIFICATION_ID, builder.build())
            //или
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID, builder.build())
            }
        }
        //Log.d(TAG, "WorkerFindNews >f notification ------------END")
    }
}