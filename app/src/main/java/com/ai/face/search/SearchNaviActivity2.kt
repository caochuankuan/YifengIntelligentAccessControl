package com.ai.face.search

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ai.face.FaceApplication
import com.ai.face.faceSearch.search.FaceSearchImagesManger
import com.ai.face.ui.LoginActivity
import com.airbnb.lottie.LottieAnimationView
import com.lzf.easyfloat.EasyFloat
import com.yifeng.face.R
import com.yifeng.face.databinding.ActivityFaceSearchNaviBinding
import com.yifeng.face.databinding.ActivitySearchNavi2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * 导航Navi，主要界面App
 *
 *
 */
class SearchNaviActivity2 : AppCompatActivity(), PermissionCallbacks {

    private lateinit var binding: ActivitySearchNavi2Binding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences1: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }


        binding = ActivitySearchNavi2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNeededPermission()

        binding.zhuti.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity2, SearchNaviActivity::class.java)
            )
            finish()
        }


        binding.aboutus.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity2, com.ai.face.ui.about::class.java)
            )
        }

        // 初始化SharedPreferences，传递文件名作为参数
        sharedPreferences1 = getSharedPreferences("users", Context.MODE_PRIVATE)
        sharedPreferences = getSharedPreferences("yifeng", Context.MODE_PRIVATE)

        binding.faceSearch1n.textSize = 50.0F

        binding.showImg.visibility = View.VISIBLE;
        binding.showImg.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity2, com.ai.face.ui.DisplayFacesActivity::class.java)
            )
        }

        binding.clearMsg.setOnClickListener {
            val sharedPref = getSharedPreferences("yifeng", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()
            val sharedPref1 = getSharedPreferences("users", Context.MODE_PRIVATE)
            sharedPref1.edit().putString("sign","0").apply()
            Toast.makeText(baseContext, "感谢您的使用", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.tips.setOnClickListener {
            Toast.makeText(baseContext,"请长按",Toast.LENGTH_LONG).show()
        }

        binding.tips.setOnLongClickListener {
            // 处理长按事件

            // 创建 AlertDialog.Builder 对象
            val alertDialogBuilder = AlertDialog.Builder(this)

            // 设置对话框标题
            alertDialogBuilder.setTitle("小区信息")

            // 设置对话框内容
            alertDialogBuilder.setMessage("小区："+ sharedPreferences.getString("xingming","") + "\n编号："
                    + sharedPreferences.getString("xuehao","") + "\n维护人："
                    + sharedPreferences.getString("yuanxi","")
            )

            // 设置积极按钮，可以根据需要修改按钮文本和点击事件
            alertDialogBuilder.setPositiveButton("确定") { dialog, which ->
                // 在点击确定按钮时的处理逻辑
                // 如果不需要处理，可以将这个回调留空
            }

            // 设置消极按钮，同样可以根据需要修改按钮文本和点击事件
            alertDialogBuilder.setNegativeButton("取消") { dialog, which ->
                // 在点击取消按钮时的处理逻辑
                // 如果不需要处理，可以将这个回调留空
            }

            // 创建并显示对话框
            alertDialogBuilder.show()

            true // 返回true表示消费了长按事件
        }


        binding.faceSearch1n.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity2, FaceSearch1NActivity::class.java)
            )
        }

        binding.faceSearchMn.setOnClickListener {
            startActivity(
                Intent(this@SearchNaviActivity2, FaceSearchMNActivity::class.java)
            )
        }

        binding.mima.visibility = View.VISIBLE;
        binding.mima.setOnClickListener {
            showInputDialogMima()
        }


        //验证复制图片
//        binding.copyFaceImages.setOnClickListener {
//            binding.copyFaceImages.isClickable = false
//            Toast.makeText(baseContext, "复制处理中...", Toast.LENGTH_LONG).show()
//            showAppFloat(baseContext)
//
//            Toast.makeText(baseContext, "复制处理中...", Toast.LENGTH_LONG).show()
//
//            CoroutineScope(Dispatchers.Main).launch {
//                copyManyTestFaceImages(application)
//                EasyFloat.hide("speed")
//                Toast.makeText(baseContext, "已经复制导入验证图片", Toast.LENGTH_SHORT).show()
//            }
//        }

        //切换摄像头
        binding.changeCamera.setOnClickListener {
            val sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE)

            if (sharedPref.getInt("cameraFlag", 0) == 1) {
                sharedPref.edit().putInt("cameraFlag", 0).apply()
                Toast.makeText(
                    baseContext,
                    "已切换前置摄像头",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sharedPref.edit().putInt("cameraFlag", 1).apply()
                Toast.makeText(
                    baseContext,
                    "已切换后置/外接摄像头",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        binding.editFaceImage.setOnClickListener {
            startActivity(
                Intent(baseContext, FaceImageEditActivity::class.java).putExtra(
                    "isAdd",
                    false
                )
            )
        }


        binding.addFaceImage.setOnClickListener {
            startActivity(
                Intent(baseContext, FaceImageEditActivity::class.java).putExtra(
                    "isAdd",
                    true
                )
            )
        }

//        binding.deviceInfo.text="设备指纹:"+ DeviceFingerprint.getDeviceFingerprint()

    }

    private fun showInputDialogMima() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("修改小区密码")

        // 使用自定义布局
        val customLayout = layoutInflater.inflate(R.layout.custom_input_dialog, null)
        val inputEditText = customLayout.findViewById<EditText>(R.id.inputEditText)
        builder.setView(customLayout)

        // 设置确定按钮
        builder.setPositiveButton("确定") { dialog, which ->
            // 获取输入框的文本
            val userInput = inputEditText.text.toString()

            // 使用SharedPreferences保存用户输入的值到特定文件
            val editor = sharedPreferences1.edit()
            editor.putString("mima", userInput)
            editor.apply()
            Toast.makeText(this, "修改小区密码为：“"+userInput+"”成功！", Toast.LENGTH_SHORT).show()

            // 处理输入框中的文本，你可以根据需要进行其他操作

        }

        // 设置取消按钮
        builder.setNegativeButton("取消") { dialog, which ->
            dialog.cancel()
        }

        // 显示弹窗
        val dialog = builder.create()
        dialog.show()
    }

    /**
     * companion object 辅助验证
     *
     */
    companion object {
        fun showAppFloat(context: Context) {
            if (EasyFloat.getFloatView("speed")?.isShown == true) return
            EasyFloat.with(context)
                .setTag("speed")
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(false)
                .setLayout(R.layout.float_loading) {
                    val entry: LottieAnimationView = it.findViewById(R.id.entry)
                    entry.setAnimation(R.raw.loading2)
                    entry.loop(true)
                    entry.playAnimation()
                }
                .show()
        }

        private fun getBitmapFromAsset(assetManager: AssetManager, strName: String): Bitmap? {
            val istr: InputStream
            var bitmap: Bitmap?
            try {
                istr = assetManager.open(strName)
                bitmap = BitmapFactory.decodeStream(istr)
            } catch (e: IOException) {
                return null
            }
            return bitmap
        }


        /**
         * 拷贝工程Assets 目录下的人脸图来演示人脸搜索，实际上的业务人脸可能是在局域网服务器或只能本地录入
         *
         * 只有Assets 肯定搜索不到对应的人脸（也许有BUG 也能） 这个时候你要再录入一张你的人脸照片
         * FaceImageEditActivity 中的拍照按钮可以触发自拍
         *
         *
         *
         */
        suspend fun copyManyTestFaceImages(context: Application) = withContext(Dispatchers.IO) {
            val assetManager = context.assets
            val subFaceFiles = context.assets.list("")
            if (subFaceFiles != null) {
                for (index in subFaceFiles.indices) {
                    //插入照片
                    FaceSearchImagesManger.c.getInstance(context)?.insertOrUpdateFaceImage(
                        getBitmapFromAsset(
                            assetManager,
                            subFaceFiles[index]
                        ),
                        FaceApplication.CACHE_SEARCH_FACE_DIR + File.separatorChar + subFaceFiles[index]
                    )
                }
            }
        }

    }




    /**
     * 统一全局的拦截权限请求，给提示。权限处理用自己项目稳定方案就行
     *
     */
    private fun checkNeededPermission() {
        val perms = arrayOf(Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *perms)) {
        } else {
            EasyPermissions.requestPermissions(this, "请授权相机使用权限！", 11, *perms)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}

    /**
     * 当用户点击了不再提醒的时候的处理方式
     */
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}

}