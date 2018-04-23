package ru.wanket.opengappsupdater.background.update

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.android.volley.Response
import org.json.JSONObject
import ru.wanket.opengappsupdater.R
import ru.wanket.opengappsupdater.gapps.GAppsInfo
import ru.wanket.opengappsupdater.network.GitHubGApps
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.app.PendingIntent
import ru.wanket.opengappsupdater.activity.MainActivity
import ru.wanket.opengappsupdater.Settings

class GAppsIntentService : IntentService("GAppsIntentService") {
    companion object {
        private const val ACTION_CHECK_UPDATE = "ru.wanket.opengappsupdater.action.ACTION_CHECK_UPDATE"
        private const val CHANNEL_ID = "UPDATE_CHANNEL"
        private const val UpdateNotificationID = 0

        fun startActionCheckUpdate(context: Context) {
            val intent = Intent(context, GAppsIntentService::class.java)
            intent.action = ACTION_CHECK_UPDATE
            context.startService(intent)
        }
    }

    private val currentGAppsInfo = GAppsInfo.getCurrentGAppsInfo()

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_CHECK_UPDATE == action) {
                handleActionCheckUpdate()
            }
        }
    }

    private fun handleActionCheckUpdate() {
        GitHubGApps(this, currentGAppsInfo.arch).getInfoGApps(
                Response.Listener { response -> checkUpdate(response) },
                Response.ErrorListener {})
    }

    private fun checkUpdate(response: String) {
        if (!Settings(applicationContext).autoCheckUpdate) {
            return
        }

        val version = JSONObject(response).getInt("tag_name")

        if (version > currentGAppsInfo.version) {
            setupChannelId()
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            NotificationCompat.Builder(this, CHANNEL_ID).apply {
                setAutoCancel(true)
                setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, 0))
                setSmallIcon(R.mipmap.ic_launcher)
                setContentText("${getString(R.string.update_aviable)} $version")
            }.let {
                NotificationManagerCompat.from(this).notify(UpdateNotificationID, it.build())
            }
        }
    }

    private fun setupChannelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Updates", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Check Update"
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notificationChannels.find {
                if (it == channel) {
                    return
                }
                return@find false
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
