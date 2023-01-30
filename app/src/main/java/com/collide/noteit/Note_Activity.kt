package com.collide.noteit

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File
import java.util.*

class Note_Activity : AppCompatActivity() {

    private var _binding: ActivityNoteBinding? = null
    private val binding get() = _binding!!

    lateinit var dialog: Dialog

    val photos: ArrayList<Note_Image_Data_Model> = ArrayList<Note_Image_Data_Model>()
    var note_id: String = ""
    var flag_image_uri = false
    var order_view_all = ""


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

        if(note_id == ""){
            note_id = UUID.randomUUID().toString()
        }



        binding.fabImageAdd.setOnClickListener {

            gallery_intent()

        }


        binding.saveBtn.setOnClickListener {
            if (!binding.etTitle.equals("") && !binding.etDesc.equals("")){
                var imageUri = ""
                var layout = findViewById<LinearLayout>(R.id.layout_linear_adder)
                var layout_childs = layout.children
                for(child in layout_childs){
                    if(child is EditText){
                        order_view_all += "ET|"
                    }else if(child is FrameLayout ){
                        order_view_all +="IV|"
                    }

                }
                order_view_all = order_view_all.substring(3..order_view_all.length-2)
                Log.d("child", ""+order_view_all)


//                if(!image_uri_list.isEmpty()){

                Log.d("child", ""+firebaseFirestore)

                    firebaseFirestore.collection("Notes")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Mynotes")
                        .document(note_id)
                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri, order_view_all, note_id))
                        .addOnSuccessListener {
                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()

                            if(photos.isNotEmpty()){
                                uploadPhotos()
                            }
                            val intent = Intent(this@Note_Activity, MainActivity::class.java)
                            startActivity(intent)

                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
                        }




//
//
//                } else{
//                    firebaseFirestore.collection("Notes")
//                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
//                        .collection("Mynotes")
//                        .document(note_id)
//                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri, note_id))
//                        .addOnSuccessListener {
//                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
//                        }
//                        .addOnFailureListener {
//                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
//                        }
//                }
//
                Log.d("User", ""+image_name_list)


            }


        }

    }

    private fun uploadPhotos() {
        photos.forEach{
            photo ->
            var uri = Uri.parse(photo.localUri)
            val imageRef = storageReference.child("images/${auth.currentUser!!.uid}/${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Log.i("upload-task", "Image Uploaded $imageRef")
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener {
                    remoteUri ->
                    photo.remoteUri = remoteUri.toString()
                    updatePhotoDatabase(photo)
                }
            }
            uploadTask.addOnFailureListener {
                Log.e("upload-task", it.message ?: "No Message")
            }

        }
    }

    private fun updatePhotoDatabase(photo: Note_Image_Data_Model) {

        var photoCollection = firebaseFirestore.collection("Notes")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection("Mynotes")
                                    .document(note_id)
                                    .collection("photos")
        var handle = photoCollection.add(photo)
        handle.addOnSuccessListener {
            Log.i("upload-task", "Successfully updated phto medtadata")
            photo.id = it.id
            var updateCollection = firebaseFirestore.collection("Notes")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .collection("Mynotes")
                                    .document(note_id)
                                    .collection("photos")
                                    .document(photo.id)
                                    .set(photo)

            updateCollection.addOnSuccessListener {
                if(flag_image_uri == false){
                    imageUri = photo.remoteUri
                    firebaseFirestore.collection("Notes")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Mynotes")
                        .document(note_id)
                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri, order_view_all, note_id))

                    flag_image_uri = true
                }
            }
            updateCollection.addOnFailureListener {
                Log.e("upload-task", "Error Updating photo data: ${it.message} ")

            }

        }
        handle.addOnFailureListener {
            Log.e("upload-task", "Error Updating photo data: ${it.message} ")
        }


    }

    private fun gallery_intent() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){

            val photo = Note_Image_Data_Model(localUri = it.data!!.data.toString())
            photos.add(photo)

            var options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(File(it.data!!.data!!.path).absolutePath, options)
            var imgHeight = options.outHeight
            var imgWidth = options.outWidth


//            var imageView = ImageView(this)
            var imageView = ZoomageView(this)
            imageView.id = View.generateViewId()
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
//            imageView.setImageResource(R.drawable.app_logo)
//            Log.d("view", ""+data_image)
            imageView.setImageURI(it.data!!.data)
            var EditText = EditText(this)
            EditText.id = View.generateViewId()
            EditText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            EditText.setText("text")

            var layout = findViewById<LinearLayout>(R.id.layout_linear_adder)

            var view = layout.focusedChild

            var frameLayout = FrameLayout(this)
            frameLayout.id = View.generateViewId()
            frameLayout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)

            var uid = auth.currentUser!!.uid
            var filename = uid+"/"+UUID.randomUUID().toString()
            image_name_list.add(filename)

            Log.d("User",""+image_name_list)



            imageView.setOnLongClickListener {
                imgview->
                var parent: ViewGroup = imgview.parent as ViewGroup
                var btn_delete = Button(this)
                btn_delete.id = View.generateViewId()
                btn_delete.layoutParams = ViewGroup.LayoutParams(50, 50)


                parent.addView(btn_delete)
                btn_delete.gravity = Gravity.BOTTOM or Gravity.START

                btn_delete.setOnClickListener {
                    Toast.makeText(this, ""+btn_delete.gravity, Toast.LENGTH_SHORT).show()

                    parent.removeView(imageView)
                    var grandparent: ViewGroup = parent.parent as ViewGroup
                    var index_parent = grandparent.indexOfChild(parent)


//                    Toast.makeText(this,""+(view_iterator is EditText), Toast.LENGTH_SHORT).show()

                    if(grandparent.getChildAt(index_parent +1) is EditText){
                        var et: EditText = grandparent.getChildAt(index_parent +1) as EditText
                        var text_et = et.text

                        var store_et: EditText = findEt(grandparent, index_parent)
                        store_et.setText(store_et.text.toString() + "\n" +text_et)
                        grandparent.removeView(et)
                    }
                    grandparent.removeView(parent)
                }

                return@setOnLongClickListener true
            }

            frameLayout.addView(imageView)

            if(view is EditText){


                var currentindex = view.selectionStart
                var enteredtext = view.text.toString()
                var endtext = enteredtext.subSequence(currentindex, enteredtext.length)
                var startext = enteredtext.subSequence(0, currentindex)

                var childindex = layout.indexOfChild(view)
                view.setText(startext)
                EditText.setText(endtext)
                current_imageView = imageView
                current_editetxt = EditText
                image_uri_list.add(it.data!!.data.toString())

                layout.addView(frameLayout, childindex + 1)
                layout.addView(EditText, childindex + 2)
                parent_layout = layout
            }else{
                image_uri_list.add(it.data!!.data.toString())
                current_imageView = imageView
                layout.addView(frameLayout)
                layout.addView(EditText)
                var views = layout.children
                for(view in views){
                    Log.d("view", ""+view)
                }

            }

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