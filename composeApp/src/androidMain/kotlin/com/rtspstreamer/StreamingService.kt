package com.rtspstreamer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager

/**
 * Foreground service to keep RTSP streaming alive when the app is backgrounded.
 *
 * Acquires a wake lock and wifi lock to prevent the system from
 * suspending the stream.
 */
class StreamingService : Service() {

  companion object {
    private const val CHANNEL_ID = "rtsp_stream_channel"
    private const val NOTIFICATION_ID = 1001
    const val ACTION_STOP = "com.rtspstreamer.STOP_STREAM"

    fun start(context: Context, rtspUrl: String) {
      val intent = Intent(context, StreamingService::class.java).apply {
        putExtra("rtsp_url", rtspUrl)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stop(context: Context) {
      context.stopService(Intent(context, StreamingService::class.java))
    }
  }

  private var wakeLock: PowerManager.WakeLock? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    acquireWakeLock()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action === ACTION_STOP) {
      stopSelf()
      return START_NOT_STICKY
    }

    val rtspUrl = intent?.getStringExtra("rtsp_url") ?: "rtsp://..."
    val notification = buildNotification(rtspUrl)
    startForeground(NOTIFICATION_ID, notification)

    return START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    releaseWakeLock()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "RTSP Streaming",
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = "Notification for active RTSP streaming"
        setShowBadge(false)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(rtspUrl: String): Notification {
    // Open app when tapping notification
    val openIntent = PendingIntent.getActivity(
      this, 0,
      Intent(this, MainActivity::class.java),
      PendingIntent.FLAG_IMMUTABLE,
    )

    // Stop action
    val stopIntent = PendingIntent.getService(
      this, 1,
      Intent(this, StreamingService::class.java).apply { action = ACTION_STOP },
      PendingIntent.FLAG_IMMUTABLE,
    )

    return Notification.Builder(this, CHANNEL_ID)
      .setContentTitle("📡 RTSP Streaming")
      .setContentText(rtspUrl)
      .setSmallIcon(android.R.drawable.ic_media_play)
      .setContentIntent(openIntent)
      .addAction(
        Notification.Action.Builder(
          null, "Stop", stopIntent
        ).build()
      )
      .setOngoing(true)
      .build()
  }

  private fun acquireWakeLock() {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    wakeLock = powerManager.newWakeLock(
      PowerManager.PARTIAL_WAKE_LOCK,
      "RTSPStreamer::StreamingWakeLock"
    ).apply {
      acquire(4 * 60 * 60 * 1000L) // 4 hours max
    }
  }

  private fun releaseWakeLock() {
    wakeLock?.let {
      if (it.isHeld) it.release()
    }
    wakeLock = null
  }
}
