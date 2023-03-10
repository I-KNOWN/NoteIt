package com.collide.noteit

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.databinding.ActivityPreviewBinding
import com.collide.noteit.tools.loadingDialog
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.*

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding
    private lateinit var textRecognition: TextRecognizer
    private var loadingDialog = loadingDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.materialCardView.setBackgroundResource(R.drawable.top_only_rounded_cardview_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.materialCardView.setBackgroundResource(R.drawable.top_only_rounded_cardview)

            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        var struri = intent.getStringExtra("photo_url")
        var photouri = Uri.parse(struri)
        binding.previewView.setImageURI(photouri)

        textRecognition = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.btnClose.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.left_slide_in_acitivity, R.anim.right_slide_out_acitivity)
        }

        binding.btnAccept.setOnClickListener {
            RecoganizeText(photouri)
        }
    }

    private fun RecoganizeText(photouri: Uri?) {
        loadingDialog.startloading()
        try{
            val inputImage = InputImage.fromFilePath(this, photouri!!)

            val textTaskResult = textRecognition.process(inputImage)
                .addOnSuccessListener {text->

                    val data_text = text.text
                    var spannableString = SpannableStringBuilder(data_text)
                    Log.d("preview_text_rec","${Html.toHtml(spannableString,   Html.FROM_HTML_MODE_COMPACT)}")
                    var html_string = Html.toHtml(spannableString,   Html.FROM_HTML_MODE_COMPACT)
                    var note_id = UUID.randomUUID().toString()
                    var currentdate = Calendar.getInstance().time
                    var DateFormat = SimpleDateFormat("EEE, MMM dd, ''yyyy", Locale.getDefault())
                    var formatedDate = DateFormat.format(currentdate)

                    var timestamp = Timestamp.now()
                    var note = Note_Data_Model("",html_string,photouri!!.toString(),"ET||||", "${html_string}", "", note_id, "red", "", formatedDate, "Unpinned", timestamp, timestamp)


                    var intent = Intent(this, Note_Activity::class.java)
                    var gson = Gson()
                    var note_gson = gson.toJson(note)
                    intent.putExtra("note_data",note_gson)
                    intent.putExtra("change_img", "true")
                    finish()
                    startActivity(intent)
                    loadingDialog.isDismis()

                }
                .addOnFailureListener{
                    Toast.makeText(this,"Failed inside", Toast.LENGTH_LONG).show()

                }
        }catch (e: java.lang.Exception){
            Toast.makeText(this,"Failed", Toast.LENGTH_LONG).show()
        }
    }
}