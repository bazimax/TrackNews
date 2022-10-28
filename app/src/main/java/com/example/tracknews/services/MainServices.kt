package com.example.tracknews.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.tracknews.MainActivity
import com.example.tracknews.classes.Constants

class MainServices {
    private val logNameClass = "MainServices" //для логов



    //Уведомления
    fun notification(checkStart: Boolean, context: Context){
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f notification === START")
        Log.d(Constants.TAG_DATA, "$logNameClass >f notification > checkStart: $checkStart")

        val intent = Intent(context, MainActivity::class.java)

        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        Log.d(Constants.TAG, "$logNameClass >f notification > intent")

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        Log.d(Constants.TAG, "$logNameClass >f notification > pendingIntent")

        val bitmap = BitmapFactory.decodeResource(context.resources, com.example.tracknews.R.mipmap.ic_launcher)
        Log.d(Constants.TAG, "$logNameClass >f notification > bitmap")

        //Если есть новые новости - создаём уведомление
        if (checkStart) {
            Log.d(Constants.TAG, "$logNameClass >f notification > IF checkStart")
            val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(com.example.tracknews.R.drawable.ic_notification)
                .setColor(com.example.tracknews.R.color.baseColorAccent)
                .setLargeIcon(bitmap)
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
            Log.d(Constants.TAG, "$logNameClass >f notification > with")
        }
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f notification ----- END")
    }
}