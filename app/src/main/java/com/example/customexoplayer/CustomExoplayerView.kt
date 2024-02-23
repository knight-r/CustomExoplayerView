package com.example.customexoplayer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes

class CustomExoplayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : PlayerView(context, attrs, defStyleAttr) {
    private var isPlayPauseButtonVisible = true
    private lateinit var player: ExoPlayer
    private val playPauseButton: ImageButton
    private val buttonContainer: FrameLayout
    private var playerView: PlayerView
    private lateinit var seekBar: SeekBar
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewCurrentTime: TextView
    private lateinit var textViewTotalDuration: TextView
    init {
        // Inflate the custom layout
        LayoutInflater.from(context).inflate(R.layout.custom_exoplayer_layout, this, true)

        playPauseButton = findViewById(R.id.btn_play_pause)
        buttonContainer = findViewById(R.id.play_pause_button_container)
        seekBar = findViewById(R.id.exo_seekbar)
        playerView = findViewById(R.id.player_view)
        progressBar = findViewById(R.id.progress_bar)

        textViewCurrentTime = findViewById(R.id.tv_current_time)
        textViewTotalDuration = findViewById(R.id.tv_total_duration)

        addSeekBarListener()
        setupPlayPauseButton()
        onRootTouchListener()
        Log.e("CustomExoView","Initialised")

    }

    private fun onRootTouchListener() {
        this.rootView.setOnClickListener {
            if(isPlayPauseButtonVisible) {
                isPlayPauseButtonVisible = false
                playPauseButton.visibility = View.GONE
            } else {
                isPlayPauseButtonVisible = true
                playPauseButton.visibility = View.VISIBLE
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressAction = object : Runnable {
        override fun run() {
            updateSeekBar()
            // Schedule the task to repeat every 1 second
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateSeekBar() {
        player?.let {
            val position = it.currentPosition
            val duration = it.duration
            if (duration > 0) {
                seekBar.progress = position.toInt()
                seekBar.max = duration.toInt()
            }
        }
    }

    private fun updateProgress() {
        val player = playerView.player ?: return
        val position = player.currentPosition
        val duration = player.duration
        if (duration > 0) {
            seekBar.progress = position.toInt()
            textViewCurrentTime.text = formatTime(position)
            handler.postDelayed(updateProgressAction, 1000 - (position % 1000)) // Schedule the next update
        }
    }
    private fun addSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                player.seekTo(progress.toLong())
                textViewTotalDuration.text = formatTime(player.duration)
                seekBar?.max = player.duration.toInt()
                updateProgress() // Initial update
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Implement if needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Implement if needed
            }
        })
    }
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        return if (hours > 0) String.format("%d:%02d:%02d", hours, minutes, seconds)
        else String.format("%02d:%02d", minutes, seconds)
    }
    private fun setupPlayPauseButton() {
        playPauseButton.setOnClickListener {
            playerView.player?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    Log.e("CustomExoView", "Player paused")
                    playPauseButton.setImageResource(R.drawable.ic_play_exoplayer) // Update to your play icon
                } else {
                    player.play()
                    Log.e("CustomExoView", "Player played")
                    playPauseButton.setImageResource(R.drawable.ic_pause_exoplayer) // Update to your pause icon
                }
            }
        }
    }

    fun setPlayer(player: ExoPlayer) {
        Log.e("CustomExoView","SettingPLayer")
        playerView.player = player
        this.player = player

        this.player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                progressBar.visibility = when (playbackState) {
                    Player.STATE_BUFFERING -> View.VISIBLE // Show progress bar during buffering
                    else -> View.GONE // Hide progress bar otherwise
                }
            }
        })

        prepareMedia("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        //prepareMedia("http://sample.vodobox.net/skate_phantom_flex_4k/skate_phantom_flex_4k.m3u8")
        handler.post(updateProgressAction)
    }

    private fun prepareMedia(mediaUrl: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(mediaUrl)
            .setMimeType(
                MimeTypes.APPLICATION_MP4)
            .build()
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(context) // <- context
        ).createMediaSource(mediaItem)

        player?.setMediaSource(mediaSource)
        player?.prepare()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        Log.e("CustomExoView","onAttached")
//        player.let {
//            buttonContainer.visibility = VISIBLE
//        }
    }

    override fun onDetachedFromWindow() {

        super.onDetachedFromWindow()
        Log.e("CustomExoView","onDetached")
//        player.release()
//        handler.removeCallbacks(updateProgressAction)

    }
}