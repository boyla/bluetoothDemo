package com.upbetter.bt

import android.Manifest.permission.*
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.lifecycle.lifecycleScope
import com.inuker.bluetooth.library.search.SearchResult
import com.upbetter.bt.bt.BtHelper
import com.upbetter.bt.util.ToastUtil
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class HomeActivity : AppCompatActivity() {

    val TAG = "HomeActivity"
    lateinit var downloadManager: DownloadManager
    lateinit var vLogo: View
    lateinit var vLightSwitch: View
    lateinit var tvConnectInfo: TextView
    var lightOn = false
    lateinit var statusListener: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtil.ctx = this
        setContentView(R.layout.activity_home)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.app_theme_color) //设置状态栏颜色
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR //实现状态栏图标和文字颜色为暗色
        }
        tvConnectInfo = findViewById(R.id.tvConnectInfo)
        vLogo = findViewById(R.id.vLogo)
        vLightSwitch = findViewById(R.id.vLightSwitch)
        vLightSwitch.setOnClickListener {
            lightOn = !lightOn
            vLightSwitch.keepScreenOn = lightOn
            vLightSwitch.setBackgroundResource(if (lightOn) R.drawable.ic_light_on else R.drawable.ic_light_off)
            ToastUtil.showToast(this, "已${if (lightOn) "开启" else "关闭"}屏幕常亮")
        }
        vLogo.setClipToOutline(true)
        vLogo.setOutlineProvider(object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 16f)
            }
        })
        findViewById<View>(R.id.llUpdate).setOnClickListener {
            downloadFile("http://124.223.197.120:83/chfs/shared/jay/%E4%B8%80%E8%B7%AF%E5%90%91%E5%8C%97.flac")
            ToastUtil.showToast(this, "下载测试文件")
        }
        findViewById<View>(R.id.llConnect).setOnClickListener {
            startActivity(Intent(this, BletoothActivity::class.java))
        }
        findViewById<View>(R.id.ll1).setOnClickListener {
            ToastUtil.showToast(this, "演示操作教学")
        }
        findViewById<View>(R.id.ll2).setOnClickListener {
            checkAndThen {
                // todo record
            }
        }
        findViewById<View>(R.id.ll3).setOnClickListener {
            checkAndThen {
                // todo lock
            }
        }
        findViewById<View>(R.id.ll4).setOnClickListener {
            checkAndThen {
                // todo time
            }
        }
        findViewById<View>(R.id.ll5).setOnClickListener {
            checkAndThen {
                // todo type
            }
        }
        findViewById<View>(R.id.ll6).setOnClickListener {
            checkAndThen {
                // todo check point
            }
        }
        findViewById<View>(R.id.ll7).setOnClickListener {
            checkAndThen {
                // todo model
            }
        }
        findViewById<View>(R.id.ll8).setOnClickListener {
            checkAndThen {
                // todo setting
            }
        }
        findViewById<View>(R.id.ll9).setOnClickListener {
            ToastUtil.showToast(this, "售后服务")
        }
        //注册广播，监听下载状态
        val intentfilter = IntentFilter()
        intentfilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadrReceiver, intentfilter)

        statusListener = {
            if (BtHelper.currentConnects.size == 0) {
                tvConnectInfo.text = "当前未连接设备"
            } else {
                val device = BtHelper.currentConnects[0]
                tvConnectInfo.text = "${device.name + " : " + device.address} \n已连接"
            }
        }
        BtHelper.registOnStatusChange(statusListener)
    }

    private fun checkAndThen(then: (bt: SearchResult) -> Unit) {
        if (BtHelper.currentConnects.size == 0) {
            ToastUtil.showToast(this, "请连接熔接机后再操作")
        } else {
            val device = BtHelper.currentConnects[0]
            ToastUtil.showToast(this, "熔接机${device.name}已连接，开始操作")
            then(device)
        }
    }

    fun Context.dp2px(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density + 0.5f
    }

    private fun downloadFile(imgUrl: String?) {
        Log.d(TAG, "downloadFile url: $imgUrl")
        if (TextUtils.isEmpty(imgUrl)) {
            Log.d(TAG, "downloadFile empty imgUrl")
            return
        }
        val url = imgUrl!!
        //下载路径，如果路径无效了，可换成你的下载路径
        //创建下载任务,downloadUrl就是下载链接
        val request = DownloadManager.Request(Uri.parse(url))
        //指定下载路径和下载文件名
        val subPath = "" + url.substring(url.lastIndexOf("/") + 1)
        var exist = false
        val imgFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            subPath
        )
        if (imgFile.exists()) {
            Log.d(TAG, "imgFile exists")
            exist = true
        }
        ToastUtil.showToast(this, if (exist) "文件已下载" else "开始下载...")
        if (exist) {
            return
        }
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            subPath
        )
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        //获取下载管理器
        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        //将下载任务加入下载队列，否则不会进行下载
        val downLoadId = downloadManager.enqueue(request)
        downloadrReceiver.ids.add(downLoadId)
    }

    //广播接受者，接收下载状态 
    private val downloadrReceiver = object : BroadcastReceiver() {
        val ids = mutableListOf<Long>()

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == -1L) {
                    return
                }
                var inTask = false
                for (id in ids) {
                    if (id == downloadId) {
                        inTask = true
                        break
                    }
                }
                if (inTask) {
                    //检查下载状态修改后缀名
                    checkDownloadStatus(downloadId)
                }
            }
        }

        //检查下载状态
        private fun checkDownloadStatus(downloadId: Long) {
            lifecycleScope.launch {
                val query = DownloadManager.Query().setFilterById(downloadId)
                var cursor: Cursor? = null
                var filename: String? = null
                try {
                    cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        var index = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        if (index < 0) {
                            index = 0
                        }
                        val downloadFileLocalUri =
                            cursor.getString(index)
                        if (downloadFileLocalUri != null) {
                            val mFile = File(Uri.parse(downloadFileLocalUri).path)
                            filename = mFile.absolutePath
                        }
                    }
                } finally {
                    cursor?.close()
                }
                val retFile = File(filename)
                val exist = retFile.exists()
                Log.d(TAG, "retFile exists: $exist")
                if (!exist) {
                    return@launch
                }
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeFile(retFile.path, options)
                val type = options.outMimeType
                var fileFormat = ""
                when (type) {
                    "image/jpeg" -> fileFormat = ".jpg"
                    "image/png" -> fileFormat = ".png"
                }
                val newName = filename + fileFormat
                retFile.renameTo(File(newName))
                Log.d(TAG, "renameTo $newName")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
//        if (BtHelper.currentConnects.size == 0) {
//            tvConnectInfo.text = "当前未连接设备"
//        } else {
//            val device = BtHelper.currentConnects[0]
//            tvConnectInfo.text = "熔接机 ${device.name} 已连接"
//        }
    }

    override fun onDestroy() {
        BtHelper.unregistOnStatusChange(statusListener)
        ToastUtil.ctx = null
        super.onDestroy()
        unregisterReceiver(downloadrReceiver)
    }
}

