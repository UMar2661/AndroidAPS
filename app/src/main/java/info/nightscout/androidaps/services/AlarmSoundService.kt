package info.nightscout.androidaps.services

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import dagger.android.DaggerService
import info.nightscout.androidaps.MainApp
import info.nightscout.androidaps.R
import info.nightscout.androidaps.logging.AAPSLogger
import info.nightscout.androidaps.logging.LTag
import info.nightscout.androidaps.utils.resources.ResourceHelper
import javax.inject.Inject

class AlarmSoundService : DaggerService() {
    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var resourceHelper: ResourceHelper
    @Inject lateinit var mainApp: MainApp

    private var player: MediaPlayer? = null
    private var resourceId = R.raw.error

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        aapsLogger.debug(LTag.CORE, "onCreate")
        startForeground(mainApp.notificationId(), mainApp.notification)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(mainApp.notificationId(), mainApp.notification)

        player?.let { if (it.isPlaying) it.stop() }

        aapsLogger.debug(LTag.CORE, "onStartCommand")
        if (intent.hasExtra("soundid")) resourceId = intent.getIntExtra("soundid", R.raw.error)
        player = MediaPlayer()
        try {
            val afd = resourceHelper.openRawResourceFd(resourceId) ?: return START_STICKY
            player?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            player?.isLooping = true // Set looping
            val manager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (!manager.isMusicActive) {
                player?.setVolume(100f, 100f)
            }
            player?.prepare()
            player?.start()
        } catch (e: Exception) {
            aapsLogger.error("Unhandled exception", e)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        player?.stop()
        player?.release()
        aapsLogger.debug(LTag.CORE, "onDestroy")
    }
}