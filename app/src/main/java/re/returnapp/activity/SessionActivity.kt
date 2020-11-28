package re.returnapp.activity

import re.returnapp.model.Entity
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_session.*
import re.returnapp.Collection
import re.returnapp.R
import re.returnapp.model.Type
import java.io.IOException

class SessionActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SessionActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var gong1fd: AssetFileDescriptor
    private lateinit var gong2fd: AssetFileDescriptor
    private lateinit var player: MediaPlayer
    private var start: Long = System.currentTimeMillis()
    private var celebrations = 0

    private lateinit var updateTimeRunnable: Runnable
    private val handler = Handler()

    private fun gong(assetFd: AssetFileDescriptor) {
        try {
            player.reset()
            player.setDataSource(
                assetFd.fileDescriptor, assetFd.startOffset,
                assetFd.length
            )
            player.prepare()
            player.start()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        start = System.currentTimeMillis()
        updateTimeRunnable = Runnable {
            val now = System.currentTimeMillis()
            val minutes = (now - start) / 60000
            val seconds = ((now - start) % 60000) / 1000
            timeText.text = String.format("%02d:%02d", minutes, seconds)
            val celebration = (minutes / 15).toInt()
            if (celebration > celebrations) {
                celebrations = celebration
                gong(gong2fd)
            }
            handler.postDelayed(updateTimeRunnable, 1000)
        }

        gong1fd = resources.openRawResourceFd(R.raw.gong)
        gong2fd = resources.openRawResourceFd(R.raw.gong2)
        player = MediaPlayer()
        player.isLooping = false

        handler.post(updateTimeRunnable)

        cancelButton.setOnClickListener {
            finish()
        }
        doneButton.setOnClickListener {
            finish()
            val entity = Entity(Collection.newId, Type.SESSION)
            entity.name = ((System.currentTimeMillis() - start) / 60000).toString()
            Collection.addEntity(entity)
            EntityActivity.start(this, entity.id)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }

}