package com.ai.face.search

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ai.face.FaceApplication.Companion.CACHE_SEARCH_FACE_DIR
import com.ai.face.base.baseImage.BaseImageDispose
import com.ai.face.base.utils.FaceFileProviderUtils
import com.ai.face.faceSearch.search.FaceSearchImagesManger
import com.ai.face.faceSearch.utils.BitmapUtils
import com.ai.face.search.SearchNaviActivity.Companion.copyManyTestFaceImages
import com.ai.face.search.SearchNaviActivity.Companion.copyManyTestFaceImages1
import com.yifeng.face.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.Locale
import java.util.UUID

/**
 * 通过SDK API 增删改 编辑人脸图片
 *
 */
class FaceImageEditActivity : AppCompatActivity() {
    private val faceImageList: MutableList<String> = ArrayList()
    private lateinit var faceImageListAdapter: FaceImageListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_image_list)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        val mRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val gridLayoutManager: LinearLayoutManager = GridLayoutManager(this, 2)
        mRecyclerView.layoutManager = gridLayoutManager
        loadImageList()

        faceImageListAdapter = FaceImageListAdapter(faceImageList)
        mRecyclerView.adapter = faceImageListAdapter


        //长按列表的人脸可以进行删除
        faceImageListAdapter.setOnItemLongClickListener { _, _, i ->
            AlertDialog.Builder(this@FaceImageEditActivity)
                .setTitle("确定要删除" + File(faceImageList[i]).name)
                .setMessage("删除后对应的人将无法被程序识别")
                .setPositiveButton("确定") { _: DialogInterface?, _: Int ->

                    //删除一张照片
                    FaceSearchImagesManger.c.getInstance(application)
                        ?.deleteFaceImage(faceImageList[i])


                    //更新列表
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
                .setNegativeButton("取消") { _: DialogInterface?, _: Int -> }
                .show()
            false
        }

        val addButton: Button = findViewById(R.id.add_img)
        addButton.setOnClickListener {
            // 在这里添加按钮点击事件的逻辑
            if (isNetworkAvailable(this@FaceImageEditActivity)) {
                // 有网络，从服务器下载人脸图片
                CoroutineScope(Dispatchers.IO).launch {
                    downloadFromFTP()
                    //copyManyTestFaceImages(application)
                    copyManyTestFaceImages1(application)
                }
            } else {
                // 无网络，从本地Assets目录复制人脸图片
                Toast.makeText(baseContext, "无网络...请稍后再试", Toast.LENGTH_LONG).show()
            }
        }


        faceImageListAdapter.setEmptyView(R.layout.empty_layout)

        faceImageListAdapter.emptyLayout?.setOnClickListener { v: View? ->
            SearchNaviActivity.showAppFloat(baseContext)
            Toast.makeText(baseContext, "复制中...", Toast.LENGTH_LONG).show()
            CoroutineScope(Dispatchers.IO).launch {

                Log.d("NetworkCheck", isNetworkAvailable(this@FaceImageEditActivity).toString())
                if (isNetworkAvailable(this@FaceImageEditActivity)) {
                    // 有网络，从服务器下载人脸图片
                    runOnUiThread {
                        Toast.makeText(baseContext, "联网...", Toast.LENGTH_LONG).show()
                    }
                    downloadFromFTP()
                    //copyManyTestFaceImages(application)
                    copyManyTestFaceImages1(application)
                } else {
                    // 无网络，从本地Assets目录复制人脸图片
                    copyManyTestFaceImages(application)

                }


                delay(800)
                MainScope().launch {
                    //Kotlin 混淆操作后协程操作失效了，因为是异步操作只能等一下
                    loadImageList()
                    faceImageListAdapter.notifyDataSetChanged()
                }
            }
        }

        if (intent.extras?.getBoolean("isAdd") == true) {
            dispatchTakePictureIntent()
        }

    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

            if (networkCapabilities != null) {
                val hasWiFi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                val hasCellular = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

                // 输出日志以便调试
                Log.d("NetworkCheck", "Has WiFi: $hasWiFi, Has Cellular: $hasCellular")

                return hasWiFi || hasCellular
            }
        } else {
            // For devices with API levels below 23
            val networkInfo = connectivityManager.activeNetworkInfo

            if (networkInfo != null) {
                // 输出日志以便调试
                Log.d("NetworkCheck", "NetworkInfo isConnected: ${networkInfo.isConnected}")

                return networkInfo.isConnected
            }
        }

        return false
    }


    // 下载
    fun downloadFromFTP() {
        val ftpClient = FTPClient()
        val server = "http://chuankuan.com.cn"
        val port = 21
        val username = "yifeng"
        val password = "123456"

        try {
            ftpClient.connect(server, port)
            ftpClient.login(username, password)
            ftpClient.enterLocalPassiveMode()
            ftpClient.enterLocalActiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)

            val remoteDirectory = "/original/" // Specify the remote directory path
            val localDirectory = CACHE_SEARCH_FACE_DIR // Specify the local directory path

            val remoteFiles = ftpClient.listFiles(remoteDirectory)

            for (remoteFile in remoteFiles) {
                if (remoteFile.isFile) {
                    val remoteFilePath = remoteDirectory + remoteFile.name
                    val localFilePath = "$localDirectory/${remoteFile.name}"

                    val outputStream = FileOutputStream(localFilePath)

                    if (ftpClient.retrieveFile(remoteFilePath, outputStream)) {
                        // Download successful for this file
                    } else {
                        // Download failed for this file
                    }

                    outputStream.close()
                }
            }

            runOnUiThread {
                loadImageList()
                faceImageListAdapter.notifyDataSetChanged()
                Toast.makeText(baseContext, "下载完成", Toast.LENGTH_SHORT).show()
            }

            ftpClient.logout()
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle exception
            runOnUiThread {
                Toast.makeText(baseContext, "下载失败", Toast.LENGTH_SHORT).show()
            }
        } finally {
            if (ftpClient.isConnected) {
                try {
                    ftpClient.disconnect()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }





    /**
     * 加载人脸库中的人脸，长按照片删除某个人脸
     */
    private fun loadImageList() {
        faceImageList.clear()
        val file = File(CACHE_SEARCH_FACE_DIR)
        val subFaceFiles = file.listFiles()
        if (subFaceFiles != null) {
            Arrays.sort(subFaceFiles, object : Comparator<File> {
                override fun compare(f1: File, f2: File): Int {
                    val diff = f1.lastModified() - f2.lastModified()
                    return if (diff > 0) -1 else if (diff == 0L) 0 else 1
                }

                override fun equals(obj: Any?): Boolean {
                    return true
                }
            })
            for (index in subFaceFiles.indices) {
                // 判断是否为文件夹
                if (!subFaceFiles[index].isDirectory) {
                    val filename = subFaceFiles[index].name
                    val lowerCaseName = filename.trim { it <= ' ' }.lowercase(Locale.getDefault())
                    if (lowerCaseName.endsWith(".jpg")
                        || lowerCaseName.endsWith(".png")
                        || lowerCaseName.endsWith(".jpeg")
                    ) {
                        faceImageList.add(subFaceFiles[index].path)
                    }
                }
            }
        }
    }


    class FaceImageListAdapter(results: MutableList<String>) :
        BaseQuickAdapter<String, BaseViewHolder>(R.layout.adapter_face_image_list_item, results) {
        override fun convert(helper: BaseViewHolder, item: String) {
            Glide.with(context).load(item)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((helper.getView<View>(R.id.image) as ImageView))
        }
    }


    /**
     * 确认是否保存底图
     *
     * @param bitmap
     */
    private fun showConfirmDialog(bitmap: Bitmap) {
        //裁剪扣出人脸部分保存, 这里 bitmap 大小统一由处理
        var bitmapCrop = BaseImageDispose(baseContext).cropFaceBitmap(bitmap)

        if (bitmapCrop == null) {
            //Bitmap 太小品质太差将不可用
            Toast.makeText(this, "没有检测到人脸或人脸太小", Toast.LENGTH_LONG).show()
            Toast.makeText(this, "没有检测到人脸或人脸太小", Toast.LENGTH_LONG).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        val dialog = builder.create()
        val dialogView = View.inflate(this, R.layout.dialog_confirm_base, null)

        //设置对话框布局
        dialog.setView(dialogView)
        dialog.setCanceledOnTouchOutside(false)
        val basePreView = dialogView.findViewById<ImageView>(R.id.preview)
        basePreView.setImageBitmap(bitmapCrop)
        val btnOK = dialogView.findViewById<Button>(R.id.btn_ok)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val editText = dialogView.findViewById<EditText>(R.id.edit_text) //face id

        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.requestFocus()

        btnOK.setOnClickListener { v: View? ->
            if (!TextUtils.isEmpty(editText.text.toString())) {
                val name = editText.text.toString() + ".jpg"

                Toast.makeText(baseContext, "处理中...", Toast.LENGTH_LONG).show()
                //Kotlin 混淆操作后协程操作失效了，因为是异步操作只能等一下
                CoroutineScope(Dispatchers.IO).launch {

                    FaceSearchImagesManger.c.getInstance(application)
                        ?.insertOrUpdateFaceImage(
                            bitmap,
                            CACHE_SEARCH_FACE_DIR + File.separatorChar + name
                        )
                    delay(300)
                    MainScope().launch {
                        loadImageList()
                        faceImageListAdapter.notifyDataSetChanged()
                    }
                }
                dialog.dismiss()
            } else {
                Toast.makeText(baseContext, "请确认ID 名字", Toast.LENGTH_LONG).show()
            }
        }
        btnCancel.setOnClickListener { v: View? ->
            dialog.dismiss()
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    /**
     * 处理自拍
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            val bitmap = BitmapUtils.a.getFixedBitmap(currentPhotoPath!!, contentResolver)
            //加一个确定ID的操作
            showConfirmDialog(bitmap)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                dispatchTakePictureIntent()
                true
            }

            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    private var currentPhotoPath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val imageFileName = "JPEG_" + UUID.randomUUID() + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }


    /**
     * SDK 由于都是离线工作，这里演示从摄像头录入人脸
     * 也可以从业务服务器导入人脸
     * 通过SDK接口导入管理
     *
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FaceFileProviderUtils.getUriForFile(
                    this,
                    FaceFileProviderUtils.getAuthority(this),
                    photoFile
                )
                //前置摄像头 1:1
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }


    companion object {
        const val REQUEST_TAKE_PHOTO = 1
    }


}
