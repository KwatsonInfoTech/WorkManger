/*
 *
 *  * Copyright (c) 2020 Razeware LLC
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *  * distribute, sublicense, create a derivative work, and/or sell copies of the
 *  * Software in any work that is designed, intended, or marketed for pedagogical or
 *  * instructional purposes related to programming, coding, application development,
 *  * or information technology.  Permission for such use, copying, modification,
 *  * merger, publication, distribution, sublicensing, creation of derivative works,
 *  * or sale is expressly withheld.
 *  *
 *  * This project and source code may use libraries or frameworks that are
 *  * released under various Open-Source licenses. Use of those libraries and
 *  * frameworks are governed by their own individual licenses.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 */

package com.raywenderlich.android.workmanager.workers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.raywenderlich.android.workmanager.utils.getUriFromUrl
import kotlinx.coroutines.delay

class ImageDownloadWorker(
  private val context: Context,
  private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
  private val notificationManager =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as
        NotificationManager

  override suspend fun doWork(): Result {
    setForeground(createForegroundInfo())
    delay(10000)
    val savedUri = context.getUriFromUrl()
    return Result.success(workDataOf("IMAGE_URI" to savedUri.toString()))
  }

  private fun createForegroundInfo(): ForegroundInfo {
    val intent = WorkManager.getInstance(applicationContext)
      .createCancelPendingIntent(getId())

    val notification = NotificationCompat.Builder(applicationContext, "workDownload")
      .setContentTitle("Downloading Your Image")
      .setTicker("Downloading Your Image")
      .setSmallIcon(R.drawable.notification_action_background)
      .setOngoing(true)
      .addAction(android.R.drawable.ic_delete, "Cancel Download", intent)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createChannel(notification, "workDownload")
    }

    return ForegroundInfo(1, notification.build())
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun createChannel(notificationBuilder: NotificationCompat.Builder, id: String) {
    notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE)
    val channel = NotificationChannel(
      id,
      "WorkManagerApp",
      NotificationManager.IMPORTANCE_HIGH
    )
    channel.description = "WorkManagerApp Notifications"
    notificationManager.createNotificationChannel(channel)
  }

}