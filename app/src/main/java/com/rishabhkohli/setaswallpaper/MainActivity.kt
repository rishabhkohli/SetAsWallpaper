package com.rishabhkohli.setaswallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)

        val actionBar = supportActionBar
        actionBar?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#60000000")))
        actionBar?.title = ""

        if ((intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_ATTACH_DATA)
            && intent.type?.startsWith("image/") == true
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
        imageView.setImage(ImageSource.uri(uri))
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

    override fun onDestroy() {
        super.onDestroy()
        deleteCache()
        imageView.recycle()
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
