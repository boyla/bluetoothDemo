package com.upbetter.bt

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.upbetter.App
import com.upbetter.bt.bt.BtHelper
import com.upbetter.bt.util.ToastUtil
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.io.File

class BletoothActivity : AppCompatActivity() {

    val TAG = "BletoothActivity"
    lateinit var downloadManager: DownloadManager
    var permissions = mutableListOf<String>()
    var hasPermission = false
    lateinit var tvName: TextView
    lateinit var tvMac: TextView
    lateinit var tvSum: TextView
    lateinit var rvDevice: RecyclerView
    lateinit var adp: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtil.ctx = this
        setContentView(R.layout.activity_bt)
        tvName = findViewById(R.id.tvName)
        tvMac = findViewById(R.id.tvMac)
        tvSum = findViewById(R.id.tvSum)
        rvDevice = findViewById(R.id.rvDevice)
        findViewById<Button>(R.id.btnScan).setOnClickListener {
            scanBluetooth()
        }

        //注册广播，监听下载状态
        val intentfilter = IntentFilter()
        intentfilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(downloadrReceiver, intentfilter)
    }

    @SuppressLint("SetTextI18n")
    private fun scanBluetooth() {
        checkPermissions()
        val locationOn = isLocationOn()
        if (!locationOn) {
            ToastUtil.showToast(this, "无法扫描蓝牙设备，请打开定位开关")
            return
        }
        if (hasPermission) {
            rvDevice.layoutManager = LinearLayoutManager(this)
            adp = DeviceAdapter(this, BtHelper.deviceLi)
            rvDevice.adapter = adp
            BtHelper.scan(App.getInstance()!!.applicationContext) {
                rvDevice.post {
                    adp.notifyDataSetChanged()
                    tvSum.text = "" + BtHelper.deviceLi.size
                }
            }
        } else {
            ToastUtil.showToast(this, "请开启蓝牙和定位权限")
        }
    }

    private fun isLocationOn(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        Log.d(TAG, "isLocationOn GPS_PROVIDER enable: $gpsOn")
        if (gpsOn) {
            return true
        }
        val netOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        Log.d(TAG, "isLocationOn NETWORK_PROVIDER enable: $netOn")
        if (netOn) {
            return true
        }
        return false
    }

//    @Composable
//    fun BtDeviceList(vm: BtModel) {
////        lifecycleScope.launchWhenStarted{
////            vm.btDevices.collect { li ->
////                Log.d(BtUtils.TAG, "render BtDeviceList size: ${li.size}")
////                if (li.size > 0) {
////                    BtUtils.mLoadState = STATE_IDLE
////                }
////                when (BtUtils.mLoadState) {
////                    STATE_IDLE -> LazyColumn(
////                        modifier = Modifier
////                            .background(
////                                color = Color(
////                                    0xf5f5f5
////                                )
////                            )
////                            .padding(0.dp, 0.dp)
////                            .clipToBounds()
////                            .fillMaxSize(),
////                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 15.dp),
////                        verticalArrangement = Arrangement.spacedBy(20.dp),
////                    ) {
////                        items(li.size) { item ->
////                            DeviceItem(li[item])
////                        }
////                    }
////
////                    STATE_ERROR -> ErrorPage("unknow error") { }
////
////                    STATE_LOADING -> LoadingPage()
////                }
////            }
////        }
//
//        val li by vm.btDevices.collectAsState()
////        val li = remember {
////            rawli
////        }
//        Log.d(BtUtils.TAG, "render BtDeviceList size: ${li.size}")
//        if (li.size > 0) {
//            BtUtils.mLoadState = STATE_IDLE
//        }
//        when (BtUtils.mLoadState) {
//            STATE_IDLE -> LazyColumn(
//                modifier = Modifier
//                    .background(
//                        color = Color(
//                            0xf5f5f5
//                        )
//                    )
//                    .padding(0.dp, 0.dp)
//                    .clipToBounds()
//                    .fillMaxSize(),
//                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 15.dp),
//                verticalArrangement = Arrangement.spacedBy(20.dp),
//            ) {
//                items(li.size) { item ->
//                    DeviceItem(li[item])
//                }
//            }
//
//            STATE_ERROR -> ErrorPage("unknow error") { }
//
//            STATE_LOADING -> LoadingPage()
//        }
//
//    }

    @Composable
//    fun DeviceItem(device: BtDevice?) {
//        Card(
//            shape = RoundedCornerShape(20.dp),
//            elevation = CardDefaults.elevatedCardElevation(30.dp),
//            modifier = Modifier
//                .background(Color.Transparent)
//                .fillMaxSize(),
//        ) {
//            Column(
//                modifier = Modifier
//                    .background(Color.White)
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(410.dp),
//                    contentAlignment = Alignment.BottomEnd
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_bluetooth),
//                        contentDescription = "",
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .size(110.dp)
//                            .clip(RoundedCornerShape(0, 0, 0, 0))
//                    )
//                    Text(
//                        text = "${device?.realName}",
//                        modifier = Modifier
//                            .padding(15.dp)
//                            .fillMaxSize()
//                    )
//                    Text(
//                        text = "信号：${device?.rssi}",
//                        modifier = Modifier
//                            .padding(15.dp)
//                            .fillMaxSize()
//                    )
//                }
//            }
//        }
//    }

//    @Composable
//    fun LoadingPage(context: Context = LocalContext.current) {
//        val animate by rememberInfiniteTransition().animateFloat(
//            initialValue = 0f,
//            targetValue = 360f,
//            animationSpec = infiniteRepeatable(
//                tween(500, easing = LinearEasing),
//                RepeatMode.Restart
//            )
//        )
//        val radius = context.dp2px(80f)
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            translate(size.width / 2 - radius, size.height / 2 - radius) {
//                drawArc(
//                    Color.Green,
//                    0f,
//                    animate,
//                    false,
//                    size = Size(radius * 2f, radius * 2f),
//                    style = Stroke(context.dp2px(4f)),
//                    alpha = 0.6f
//                )
//            }
//        }
//    }

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

    //  蓝牙申请权限
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 版本大于等于 Android12 时
            // 只包括蓝牙这部分的权限，其余的需要什么权限自己添加
            permissions.add(BLUETOOTH_SCAN)
            permissions.add(BLUETOOTH_ADVERTISE)
            permissions.add(BLUETOOTH_CONNECT)

            permissions.add(BLUETOOTH)
            permissions.add(BLUETOOTH_ADMIN)
        } else {
            // Android 版本小于 Android12 及以下版本
        }
        permissions.add(ACCESS_COARSE_LOCATION)
        permissions.add(ACCESS_FINE_LOCATION)
        val arr = permissions.toTypedArray()
        if (permissions.size > 0) {
            if (EasyPermissions.hasPermissions(this, *(arr))) {
                hasPermission = true
            } else {
                // 没有申请过权限，现在去申请
                /**
                 *@param host Context对象
                 *@param rationale  权限弹窗上的提示语。
                 *@param requestCode 请求权限的唯一标识码
                 *@param perms 一系列权限
                 */
                EasyPermissions.requestPermissions(
                    this,
                    "申请权限",
                    1001,
                    *(arr)
                )
            }
            EasyPermissions.requestPermissions(
                this,
                "申请权限",
                1001,
                *(arr)
            )
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

}

