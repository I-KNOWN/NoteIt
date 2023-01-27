package com.collide.noteit

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.databinding.ActivityNoteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.util.*

class Note_Activity : AppCompatActivity() {

    private var _binding: ActivityNoteBinding? = null
    private val binding get() = _binding!!

    lateinit var dialog: Dialog

    private lateinit var firebaseFirestore: FirebaseFirestore
    var data_image: Intent? = null

    lateinit var current_imageView: ImageView
    lateinit var parent_layout: LinearLayout
    lateinit var current_editetxt: EditText

    private var image_uri_list = mutableListOf<String>()
    private var image_name_list = mutableListOf<String>()
    var imageUri = ""

    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        var notedisplayadapter = NoteDisplayAdapter(applicationContext)
        auth = FirebaseAuth.getInstance()
        firebaseFirestore =  Firebase.firestore


        binding.fabImageAdd.setOnClickListener {

            gallery_intent()

        }

        registerForContextMenu(binding.etTitle)

        binding.saveBtn.setOnClickListener {
            if (!binding.etTitle.equals("") && !binding.etDesc.equals("")){
                var imageUri = ""
                if(!image_uri_list.isEmpty()){


                    for(item in 0..image_uri_list.size - 1){
                        Log.d("data", "inside image")
                        var uri = Uri.parse(image_uri_list[item])
                        storageReference = FirebaseStorage.getInstance().getReference(image_name_list[item])
                        storageReference.putFile(uri)
                            .addOnSuccessListener {

                                Log.d("User", ""+image_name_list)
                                Log.d("User", "Uploaded Successfully")
                            }
                            .addOnFailureListener {

                                Log.d("User", "Uploaded Failed")
                            }
                    }

                    if(!image_name_list.isEmpty()){
                        for(item in image_name_list){
                            imageUri += item+"|"
                        }
                        Log.d("data", "inside the loop")
                    }
                    Log.d("data", "inside everything")

                    firebaseFirestore.collection("Notes")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Mynotes")
                        .document()
                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri))
                        .addOnSuccessListener {
                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
                        }






                } else{
                    firebaseFirestore.collection("Notes")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Mynotes")
                        .document()
                        .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri))
                        .addOnSuccessListener {
                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
                        }
                }

                Log.d("User", ""+image_name_list)


            }


        }

    }

    private fun gallery_intent() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){

            var imageView = ImageView(this)
            imageView.id = View.generateViewId()
            imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300)
//            imageView.setImageResource(R.drawable.app_logo)
//            Log.d("view", ""+data_image)
            imageView.setImageURI(it.data!!.data)
            registerForContextMenu(imageView)

            var EditText = EditText(this)
            EditText.id = View.generateViewId()
            EditText.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            EditText.setText("text")

            var layout = findViewById<LinearLayout>(R.id.layout_linear_adder)

            var view = layout.focusedChild


            var uid = auth.currentUser!!.uid
            var filename = uid+"/"+UUID.randomUUID().toString()
            image_name_list.add(filename)

            Log.d("User",""+image_name_list)


            imageView.setOnTouchListener { view, motionEvent ->

                var x: Int = motionEvent.x.toInt()
                var y: Int = motionEvent.y.toInt()

                Log.d("touch", "x = "+x+" y = "+y)

                return@setOnTouchListener true
            }

            imageView.setOnLongClickListener {

                dialog = Dialog(this)
                dialog.setContentView(R.layout.dialoglayout)


                dialog.show()



                return@setOnLongClickListener true
            }

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

                layout.addView(imageView, childindex + 1)
                layout.addView(EditText, childindex + 2)
                parent_layout = layout
            }else{
                image_uri_list.add(it.data!!.data.toString())
                current_imageView = imageView
                layout.addView(imageView)
                layout.addView(EditText)
                var views = layout.children
                for(view in views){
                    Log.d("view", ""+view)
                }

            }

        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.imageview_popup_custom, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.delete_option ->{
                Log.d("data","Delete Clicked")
                return true
            }
            else ->{
                return super.onContextItemSelected(item)
            }
        }
    }



}