package com.upbetter.bt

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.coil.rememberCoilPainter
import com.upbetter.bt.data.DataBean
import com.upbetter.bt.util.ToastUtil
import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {

    val TAG = "MainActivity"
    lateinit var downloadManager: DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtil.ctx = this
        setContent {
            val vm: MainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
            MaterialTheme {
                ImgList(vm)
            }
        }
        //注册广播，监听下载状态
        val intentfilter = IntentFilter()
        intentfilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadrReceiver, intentfilter)
    }

    @Composable
    fun ImgList(vm: MainViewModel) {
        val imgs = vm.rawData.collectAsLazyPagingItems()
        when (imgs.loadState.refresh) {
            is LoadState.NotLoading -> LazyColumn(
                modifier = Modifier
                    .background(
                        color = Color(
                            0xf5f5f5
                        )
                    )
                    .padding(0.dp, 0.dp)
                    .clipToBounds()
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(imgs.itemCount) { item ->
                    ImgItem(imgs[item])
                }
            }

            is LoadState.Error -> ErrorPage(
                (imgs.loadState.refresh as LoadState.Error).error.message ?: ""
            ) { imgs.refresh() }

            is LoadState.Loading -> LoadingPage()
        }
    }

    @Composable
    fun ImgItem(imgItem: DataBean.ResBean.Item?) {
        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.elevatedCardElevation(30.dp),
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .size(410.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Image(
                        painter = rememberCoilPainter(request = imgItem?.img, fadeIn = true),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(410.dp)
                            .clip(RoundedCornerShape(0, 0, 0, 0))
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(15.dp)
                            .background(Color(0x7A000000), RoundedCornerShape(90, 90, 90, 90))
                            .clickable { downloadFile(imgItem?.img) }
                            .clip(RoundedCornerShape(0, 0, 0, 0))
                    )
                }

                Text(
                    text = "${imgItem?.tag}",
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxSize()
                )
            }
        }
    }

    @Composable
    fun LoadingPage(context: Context = LocalContext.current) {
        val animate by rememberInfiniteTransition().animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                tween(500, easing = LinearEasing),
                RepeatMode.Restart
            )
        )
        val radius = context.dp2px(80f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            translate(size.width / 2 - radius, size.height / 2 - radius) {
                drawArc(
                    Color.Green,
                    0f,
                    animate,
                    false,
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(context.dp2px(4f)),
                    alpha = 0.6f
                )
            }
        }
    }

    fun Context.dp2px(dp: Float): Float {
        val density = resources.displayMetrics.density
        return dp * density + 0.5f
    }

    @Composable
    fun ErrorPage(msg: String, onclick: () -> Unit = {}) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(300.dp, 180.dp),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "",
                contentScale = ContentScale.Crop
            )
            Button(modifier = Modifier.padding(8.dp), onClick = onclick) {
                Text(text = "网络异常：$msg")
            }
        }
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
        val subPath = "/bg/" + url.substring(url.indexOf("="))
        val jpgPath = subPath + ".jpg"
        val pngPath = subPath + ".png"
        var exist = false
        val jpgFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            jpgPath
        )
        if (jpgFile.exists()) {
            Log.d(TAG, "jpgFile exists")
            exist = true
        }
        val pngFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            pngPath
        )
        if (pngFile.exists()) {
            Log.d(TAG, "pngFile exists")
            exist = true
        }
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

//                if (!TextUtils.isEmpty(filename)) {
//                    val installIntent = Intent(Intent.ACTION_VIEW)
//                    installIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    installIntent.setDataAndType(
//                        Uri.fromFile(File(filename)),
//                        "application/vnd.android.package-archive"
//                    )
//                    context.startActivity(installIntent)
//                }
}

