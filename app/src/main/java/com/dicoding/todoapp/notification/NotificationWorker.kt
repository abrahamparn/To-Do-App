package com.dicoding.todoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat.getSystemService
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.data.TaskRepository
import com.dicoding.todoapp.ui.detail.DetailTaskActivity
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.todoapp.utils.TASK_ID

class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "channel_01"
        private const val CHANNEL_NAME = "dicoding channel"
    }
    private val channelName = inputData.getString(NOTIFICATION_CHANNEL_ID)

    private fun getPendingIntent(task: Task): PendingIntent? {
        val intent = Intent(applicationContext, DetailTaskActivity::class.java).apply {
            putExtra(TASK_ID, task.id)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun doWork(): Result {
        //TODO 14 : If notification preference on, get nearest active task from repository and show notification with pending intent
        //https://www.dicoding.com/academies/14/tutorials/1081

        val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val textRepository = TaskRepository.getInstance(applicationContext).getNearestActiveTask()
        val pendingIntent = getPendingIntent(textRepository)
        val contentTitle = textRepository.title
        val contentText = applicationContext.resources.getString(
            R.string.notify_content,
            DateConverter.convertMillisToString(textRepository.dueDateMillis)
        )
        val smallIcon = R.drawable.ic_notifications
        val mBuilder =
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(smallIcon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(NotificationChannel(CHANNEL_ID, channelName , NotificationManager.IMPORTANCE_HIGH))

        }

        val notification = mBuilder.build()
        mNotificationManager.notify(NOTIFICATION_ID, notification)



        return Result.success()
    }

}
