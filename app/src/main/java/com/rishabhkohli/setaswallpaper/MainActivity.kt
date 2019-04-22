package com.rishabhkohli.setaswallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.thread
import android.view.Menu
import android.view.MenuItem
import android.graphics.*
import androidx.core.view.drawToBitmap
import android.graphics.Bitmap
import android.util.DisplayMetrics
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.io.File
import java.security.MessageDigest
import java.nio.file.Files.delete
import java.nio.file.Files.isDirectory




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#60000000")))
        actionBar?.title = ""

        if ((intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_ATTACH_DATA) && intent.type?.startsWith(
                "image/"
            ) == true
        ) {
            val uri: Uri? =
                when {
                    intent.action == Intent.ACTION_SEND -> intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                    intent.action == Intent.ACTION_ATTACH_DATA -> intent.data
                    else -> null
                }
            if (uri != null) handleReceivedImage(uri)
        } else {
            finishAndRemoveTask()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.set_wallpaper)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.title == getString(R.string.set_wallpaper)) {
            setWallpaper()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun handleReceivedImage(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .load(uri)
            .transform(CenterCropTOScreenSizeKeepingAspectRatio(getScreenWidth(), getScreenHeight()))
            .thumbnail(0.5f)
            .into(imageView)
    }

    private fun setWallpaper() {
        thread {
            WallpaperManager.getInstance(this).setBitmap(imageView.drawToBitmap())
        }
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    class CenterCropTOScreenSizeKeepingAspectRatio(private val screenWidth: Int, private val screenHeight: Int) :
        BitmapTransformation() {
        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            if (toTransform.width == outWidth && toTransform.height == outHeight) {
                return toTransform
            }

            val originalHeight = toTransform.height
            val originalWidth = toTransform.width

            var newHeight = screenHeight
            var newWidth = (originalWidth / (originalHeight / screenHeight.toFloat())).toInt()
            if (newWidth < screenWidth) {
                newWidth = screenWidth
                newHeight = (originalHeight / (originalWidth / screenWidth.toFloat())).toInt()
            }

            return TransformationUtils.centerCrop(pool, toTransform, newWidth, newHeight)
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        }
    }

    private fun getScreenHeight(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteCache()
    }

    private fun deleteCache() {
        try {
            deleteDir(cacheDir)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                if (!deleteDir(File(dir, children[i]))) {
                    return false
                }
            }
            return dir.delete()
        } else {
            return if (dir != null && dir.isFile) {
                dir.delete()
            } else {
                false
            }
        }
    }
}
