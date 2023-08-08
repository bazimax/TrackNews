package com.example.tracknews.services

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
//import androidx.test.core.app.ApplicationProvider.getApplicationContext
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

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE) //flags: PendingIntent.FLAG_UPDATE_CURRENT //flags: 0
        Log.d(Constants.TAG, "$logNameClass >f notification > pendingIntent")

        val bitmap = BitmapFactory.decodeResource(context.resources, com.example.tracknews.R.mipmap.ic_launcher)
        Log.d(Constants.TAG, "$logNameClass >f notification > bitmap")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationChannel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
            Log.d(Constants.TAG, "$logNameClass >f notification > VERSION.SDK_INT >= O")
        }

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
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)//.setPriority(PRIORITY_HIGH) //.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

            //посылаем уведомление
            //val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            //val notificationManager = NotificationManagerCompat.from(this)
            //notificationManager.notify(NOTIFICATION_ID, builder.build())
            //или
            with(NotificationManagerCompat.from(context)) {
                notify(Constants.NOTIFICATION_ID, builder.build())
            }
            Log.d(Constants.TAG, "$logNameClass >f notification > send")
        }
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f notification ----- END")
    }

    fun notificationNew(checkStart: Boolean, context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)

        val intent = Intent(context, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)



        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setAutoCancel(true)
                .setSmallIcon(com.example.tracknews.R.drawable.ic_notification)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setContentTitle("Title")
                .setContentText("text")
                .setPriority(PRIORITY_HIGH)

        notificationManager.notify(Constants.NOTIFICATION_ID, notificationBuilder.build())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
            Log.d(Constants.TAG, "$logNameClass >f notificationNew > send")
        }
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f notificationNew ----- END")
    }
}