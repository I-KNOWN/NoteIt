package com.collide.noteit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.collide.noteit.databinding.ActivityCameraBinding
import com.collide.noteit.tools.loadingDialog
import java.io.File
import java.lang.invoke.ConstantCallSite
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var sp: SharedPreferences
    private var flash_bool = true
    private var loadingDialog = loadingDialog(this)
    private lateinit var cameraProvider: ProcessCameraProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sp = getSharedPreferences("Camera_Pref", Context.MODE_PRIVATE)
        Log.d("CameraActivity","${sp.getString("photo_URI", "")}")
        if(sp.getString("photo_URI", "") != ""){
            var photo_uri = Uri.parse(sp.getString("photo_URI", ""))

            binding.galleryImg.setImageURI(photo_uri)
        }

        startCamera()

        outputDirectory = getOutputDirectory()

        binding.imgCapture.setOnClickListener{
//            var animat = AnimationUtils.loadAnimation(this, R.anim.capture_btn_scale)
//            animat.duration = 250
//            binding.imgCapture.startAnimation(animat)
            captureImage()
        }

        binding.galleryImg.setOnClickListener {
            gallery_intent()
        }
        binding.bkBtn.setOnClickListener{
            val intent = Intent(this@CameraActivity, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.left_slide_in_acitivity, R.anim.right_slide_out_acitivity)

        }
    }

    private fun gallery_intent() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){

            var uri = it.data!!.data
            val intent = Intent(this@CameraActivity, PreviewActivity::class.java)
            intent.putExtra("photo_url",uri.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.right_slide_in_acitivity, R.anim.left_slide_out_acitivity)

        }
    }

    private fun getOutputDirectory():File {

        val mediaDir = externalMediaDirs.firstOrNull()?.let{mFile->
            File(mFile, resources.getString(R.string.app_name)).apply {
                mkdirs()
            }

        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir

    }

    private fun captureImage() {
        loadingDialog.startloading()
        val imageCapture = imageCapture ?: return
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yy-MM-dd-HH-mm-ss-SS", Locale.getDefault()).format(System.currentTimeMillis())+".jpg")
        val outputOption = ImageCapture
            .OutputFileOptions
            .Builder(photoFile)
            .build()
        imageCapture.takePicture(
            outputOption, ContextCompat.getMainExecutor(this),
            object: ImageCapture.OnImageSavedCallback{
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {



                    val savedUri = Uri.fromFile(photoFile)
//                    val msg = "Photo Saved"
//                    Toast.makeText(this@CameraActivity, "$msg $savedUri", Toast.LENGTH_LONG).show()
                    loadingDialog.isDismis()
                    storeShared(savedUri.toString())

                    val intent = Intent(this@CameraActivity, PreviewActivity::class.java)
                    intent.putExtra("photo_url",savedUri.toString())
                    startActivity(intent)
                    overridePendingTransition(R.anim.right_slide_in_acitivity, R.anim.left_slide_out_acitivity)
                    binding.flashImg.setImageDrawable(ContextCompat.getDrawable(this@CameraActivity, R.drawable.ic_baseline_flash_off))
                    flash_bool = false
//                    cameraProvider.unbindAll()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("EXception_camera", "Unable to save: ${exception.message}")
                }

            }
        )

    }

    private fun storeShared(uri: String) {
        var editor = sp.edit()
        editor.putString("photo_URI", uri)
        editor.commit()
        var photo_uri = Uri.parse(sp.getString("photo_URI", ""))

        binding.galleryImg.setImageURI(photo_uri)

    }
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalZeroShutterLag::class)
    private fun  startCamera() {
        val cameraProviderFurture = ProcessCameraProvider.getInstance(this)

        cameraProviderFurture.addListener({

            cameraProvider = cameraProviderFurture.get()



            val preview = Preview.Builder()
                .build()
                .also { mPreview->
                    mPreview.setSurfaceProvider(
                        binding.previewView.surfaceProvider
                    )

                }
            imageCapture =  ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
                .build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this,cameraSelector,preview,imageCapture
                )
                binding.flash.setOnClickListener {
                    if(flash_bool){
                        camera.cameraControl.enableTorch(true)
                        binding.flashImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_flash_on))
                        flash_bool = false
                    } else{
                        camera.cameraControl.enableTorch(false)
                        binding.flashImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_flash_off))
                        flash_bool = true

                    }
                }

            }catch (e: Exception){
                Log.d("EXception_camera","StartCamera Fail: ${e}" )
            }

        }, ContextCompat.getMainExecutor(this))

    }
}