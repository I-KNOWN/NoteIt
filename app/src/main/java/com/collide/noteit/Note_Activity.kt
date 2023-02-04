package com.collide.noteit

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.core.view.get
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.dataClass.Note_Image_Data_Model
import com.collide.noteit.databinding.ActivityNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.jsibbold.zoomage.ZoomageView
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import ozaydin.serkan.com.image_zoom_view.ImageViewZoom
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Note_Activity : AppCompatActivity() {

    private var _binding: ActivityNoteBinding? = null
    private val binding get() = _binding!!

    lateinit var dialog: Dialog

    val photos: MutableList<Note_Image_Data_Model> = mutableListOf()
    var note_id: String = ""
    var flag_image_uri = false
    var order_view_all = ""
    var edit_text_data_all = ""
    lateinit var spannableString: SpannableStringBuilder
    lateinit var spannable_html: String

    private lateinit var firebaseFirestore: FirebaseFirestore
    var data_image: Intent? = null

    lateinit var current_imageView: ImageView
    lateinit var parent_layout: LinearLayout
    lateinit var current_editetxt: EditText

    private var image_uri_list = mutableListOf<String>()
    private var image_name_list = mutableListOf<String>()
    var imageUri = ""

    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        var notedisplayadapter = NoteDisplayAdapter(applicationContext)
        auth = FirebaseAuth.getInstance()
        firebaseFirestore =  Firebase.firestore

        setCurrentDate()

        binding.selectionImage.setOnClickListener {
            gallery_intent()
        }
        binding.imageViewNote.setOnLongClickListener {
            var inanim = AnimationUtils.loadAnimation(this@Note_Activity, R.anim.popup_delete_btn_image_note)
            binding.imageDeleteNote.startAnimation(inanim)
            var outanim = AnimationUtils.loadAnimation(this@Note_Activity, R.anim.popdown_delete_btn_image_note)
            binding.imageDeleteNote.startAnimation(outanim)
            return@setOnLongClickListener true
        }
        binding.imageDeleteNote.setOnClickListener {
            binding.imageDeleteNote.visibility = ViewGroup.GONE
            binding.imageViewNote.visibility = ViewGroup.GONE
            photos.clear()
            Log.d("hold", ""+photos)
        }

        if(note_id == ""){
            note_id = UUID.randomUUID().toString()
        }



//        binding.fabImageAdd.setOnClickListener {
//
//            gallery_intent()
//
//        }

//        binding.fabTextColor.setOnClickListener {
////            setColor()
//        }

//        binding.saveBtn.setOnClickListener {
//            if (!binding.etTitle.equals("") && !binding.etDesc.equals("")){
//                var imageUri = ""
//                var layout = findViewById<LinearLayout>(R.id.layout_linear_adder)
//                var layout_childs = layout.children
//                for(child in layout_childs){
//                    if(child is EditText){
//                        order_view_all += "ET||||"
//                        edit_text_data_all += child.text.toString()+"|"
//
//                    }else if(child is FrameLayout ){
//                        order_view_all +="IV||||"
//                    }
//
//                }
//                if(!order_view_all.equals("ET||||")){
//                    order_view_all = order_view_all.substring(6..order_view_all.length-5)
//                    Log.d("child", ""+order_view_all)
//                } else{
//                    order_view_all = order_view_all.substring(0..2)
//                }
//
//                edit_text_data_all = edit_text_data_all.substring(0..edit_text_data_all.length-5)
//
//
//
//
//
////                if(!image_uri_list.isEmpty()){
//
//                Log.d("child", ""+firebaseFirestore)
//
//                    firebaseFirestore.collection("Notes")
//                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
//                        .collection("Mynotes")
//                        .document(note_id)
//                        .set(Note_Data_Model(binding.etTitle.text.toString(), spannable_html, imageUri, order_view_all, edit_text_data_all, note_id))
//                        .addOnSuccessListener {
//                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
//
//                            if(photos.isNotEmpty()){
//                                uploadPhotos()
//                            }
//                            val intent = Intent(this@Note_Activity, MainActivity::class.java)
//                            startActivity(intent)
//
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
//                        }
//
//
//                val intent = Intent(this@Note_Activity, MainActivity::class.java)
//                startActivity(intent)
//
////
////
////                } else{
////                    firebaseFirestore.collection("Notes")
////                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
////                        .collection("Mynotes")
////                        .document(note_id)
////                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri, note_id))
////                        .addOnSuccessListener {
////                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
////                        }
////                        .addOnFailureListener {
////                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
////                        }
////                }
////
//                Log.d("User", ""+image_name_list)
//
//
//            }
//
//
//        }

    }

    private fun setCurrentDate() {
        var currentdate = Calendar.getInstance().time
        var DateFormat = SimpleDateFormat("EEE, MMM dd, ''yyyy", Locale.getDefault())
        var formatedDate = DateFormat.format(currentdate)
        binding.createdDate.text = formatedDate
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setColor() {

        spannableString = SpannableStringBuilder(binding.etDesc.text)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE),
            binding.etDesc.selectionStart,
            binding.etDesc.selectionEnd,
            0
        )
        spannableString.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
            binding.etDesc.selectionStart,
            binding.etDesc.selectionEnd,
            0
        )
        binding.etDesc.setText(spannableString)


        spannable_html = Html.toHtml(spannableString, Html.FROM_HTML_MODE_COMPACT)
    }

//    private fun uploadPhotos() {
//        photos.forEach{
//            photo ->
//            var uri = Uri.parse(photo.localUri)
//            val imageRef = storageReference.child("images/${auth.currentUser!!.uid}/${uri.lastPathSegment}")
//            val uploadTask = imageRef.putFile(uri)
//            uploadTask.addOnSuccessListener {
//                Log.i("upload-task", "Image Uploaded $imageRef")
//                val downloadUrl = imageRef.downloadUrl
//                downloadUrl.addOnSuccessListener {
//                    remoteUri ->
//                    photo.remoteUri = remoteUri.toString()
//                    updatePhotoDatabase(photo)
//                }
//            }
//            uploadTask.addOnFailureListener {
//                Log.e("upload-task", it.message ?: "No Message")
//            }
//
//        }
//    }

//    private fun updatePhotoDatabase(photo: Note_Image_Data_Model) {
//
//        var photoCollection = firebaseFirestore.collection("Notes")
//                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
//                                    .collection("Mynotes")
//                                    .document(note_id)
//                                    .collection("photos")
//        var handle = photoCollection.add(photo)
//        handle.addOnSuccessListener {
//            Log.i("upload-task", "Successfully updated phto medtadata")
//            photo.id = it.id
//            var updateCollection = firebaseFirestore.collection("Notes")
//                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
//                                    .collection("Mynotes")
//                                    .document(note_id)
//                                    .collection("photos")
//                                    .document(photo.id)
//                                    .set(photo)
//
//            updateCollection.addOnSuccessListener {
//                if(flag_image_uri == false){
//                    imageUri = photo.remoteUri
//                    firebaseFirestore.collection("Notes")
//                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
//                        .collection("Mynotes")
//                        .document(note_id)
//                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri, order_view_all, edit_text_data_all, note_id))
//
//                    flag_image_uri = true
//                }
//            }
//            updateCollection.addOnFailureListener {
//                Log.e("upload-task", "Error Updating photo data: ${it.message} ")
//
//            }
//
//        }
//        handle.addOnFailureListener {
//            Log.e("upload-task", "Error Updating photo data: ${it.message} ")
//        }
//
//
//    }

    private fun gallery_intent() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            binding.imageViewNote.visibility = ViewGroup.VISIBLE
            binding.imageDeleteNote.visibility = ViewGroup.VISIBLE

            var uri = it.data!!.data
            val photo = Note_Image_Data_Model(path = "images/${auth.currentUser!!.uid}/${uri!!.lastPathSegment}" ,localUri = it.data!!.data.toString())
            photos.add(photo)
            binding.imageViewNote.setImageURI(uri)

        }
    }

    private fun findEt(grandparent: ViewGroup, indexParent: Int): EditText {
        var view = grandparent.getChildAt(indexParent - 1)
        if(view is EditText){
            return view
        }
        else{
            return findEt(grandparent, indexParent - 1)
        }
    }


}