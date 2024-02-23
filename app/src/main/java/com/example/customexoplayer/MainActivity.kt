package com.example.customexoplayer

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes

class MainActivity : AppCompatActivity() {
    private lateinit var customExoPlayerView: CustomExoplayerView
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        customExoPlayerView = findViewById(R.id.custom_exoplayer_view)
        val imageView = ZoomImageView(this)

        loadImage(imageView)

        Log.e("NetworkType",NetworkType.WIFI.typeName)
        Log.e("NetworkType",NetworkType.MOBILE.typeName)
        //initializePlayer()
    }

    private fun loadImage(imageView: ZoomImageView) {
        val path = "/sdcard/.transforms/synthetic/picker/0/com.android.providers.media.photopicker/media/1000002268.jpg"

        val requestOptions = RequestOptions()
            .skipMemoryCache(true)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.NONE)

        Glide.with(applicationContext)
            .load(path)
            .into(imageView)
    }
    fun getPathFromUri(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }
    private fun initializePlayer() {
        Log.e("MainActivity","Initializing PLayer")
        player = ExoPlayer.Builder(this).build()
        customExoPlayerView.setPlayer(player)

    }
    override fun onDestroy() {
        Log.e("CustomExoView","Player released")
        super.onDestroy()
        player?.release()
    }
}