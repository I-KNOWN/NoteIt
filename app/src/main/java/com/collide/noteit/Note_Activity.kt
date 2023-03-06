package com.collide.noteit

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.*
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.getSpans
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.customView.ETCheckbox
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.dataClass.Note_Image_Data_Model
import com.collide.noteit.databinding.ActivityNoteBinding
import com.collide.noteit.observeconnectivity.ConnectivityObserver
import com.collide.noteit.observeconnectivity.NetworkConnectivityObserver
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.checkerframework.common.subtyping.qual.Bottom
import java.text.SimpleDateFormat
import java.util.*

class Note_Activity : AppCompatActivity() {

    private var _binding: ActivityNoteBinding? = null
    private val binding get() = _binding!!

    lateinit var dialog: Dialog
    lateinit var dialog_style: BottomSheetDialog
    lateinit var dialog_color: BottomSheetDialog
    var intentcalled = false

    private lateinit var connectivityObserver: ConnectivityObserver

    companion object{
        lateinit var instance: Note_Activity
        var connectivity = ""
    }
    var image_changed = false
    val photos: MutableList<Note_Image_Data_Model> = mutableListOf()
    var note_id: String = ""
    var flag_image_uri = false
    var order_view_all = ""
    var edit_text_data_all = ""
    var task_data_all = ""
    var task_data_check_all = ""
    var desc = ""
    var ll_id: Int = 1
    var formatedDate = ""
    lateinit var spannableString: Spannable
    lateinit var spannable_html: String
    var note_color_hole = "blue"

    private lateinit var firebaseFirestore: FirebaseFirestore
    var data_image: Intent? = null

    lateinit var current_imageView: ImageView
    lateinit var parent_layout: LinearLayout
    lateinit var current_editetxt: EditText

    private var image_uri_list = mutableListOf<String>()
    private var image_name_list = mutableListOf<String>()
    var imageUri = ""
    var pinned_note = "Unpinned"
    lateinit var timestamp: Timestamp

    private var storageReference: StorageReference = FirebaseStorage.getInstance().getReference()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        connectivityObserver = NetworkConnectivityObserver(applicationContext)

        connectivityObserver.observer().onEach {
            connectivity = it.toString()
        }.launchIn(lifecycleScope)

        instance = this

//        var notedisplayadapter = NoteDisplayAdapter(applicationContext)
        auth = FirebaseAuth.getInstance()
        firebaseFirestore =  Firebase.firestore


//        binding.selectionImage.setOnClickListener {
//            gallery_intent()
//        }




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
            binding.parentofparent.visibility = ViewGroup.INVISIBLE
            imageUri = ""


            val parmas = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            parmas.setMargins(0,0,0,0)
            var ll = findViewById<LinearLayout>(R.id.layout_linear_adder)
            ll.layoutParams = parmas

            ll.setPadding(0, 0, 0, 0)


            photos.clear()
            Log.d("hold", ""+photos)
        }
        binding.addTask.setOnClickListener {
            createTask(it)
        }
        if(note_id == ""){
            note_id = UUID.randomUUID().toString()
        }

        binding.selectionStyle.setOnClickListener {
            showDialogStyle()
        }
        binding.noteColorBtn.setOnClickListener {
            showDialogColor()
        }

//        binding.selectionColor.setOnClickListener {
//            Log.d("spannable","html: "+SpannableStringBuilder(binding.etDesc.text.toString()).getSpanStart(StyleSpan::class.java))
//            setColor()
//        }
//        binding.selectionBold.setOnClickListener {
//            Log.d("spannable","html: "+SpannableStringBuilder(binding.etDesc.text.toString()))
//
//            setBold()
//        }
//        binding.selectionItalic.setOnClickListener {
//            Log.d("spannable","html: "+SpannableStringBuilder(binding.etDesc.text.toString()))
//
//            setItalic()
//        }
        binding.backBtnMain.setOnClickListener {
            saveNote()
        }
        binding.backBtnMainTxt.setOnClickListener {
            saveNote()
        }

//        binding.etDesc.addTextChangedListener(object: TextWatcher{
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                return
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//
//                var spannablestring = Html.fromHtml(styledText(p0), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE)
//                Log.d("StyledText",""+spannablestring)
//                binding.etDesc.setText(spannablestring)
//
//
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//                return
//            }
//
//        })


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




        if(intent.getStringExtra("note_data") != null){
            var anim = AnimationUtils.loadAnimation(this, R.anim.option_layout_out)
            anim.duration = 250
            anim.fillAfter = true
            binding.optionLayout.startAnimation(anim)

            loadData()

            for(child in binding.layoutLinearAdder.children){
                child.setOnFocusChangeListener { view, b ->
                    var anim = AnimationUtils.loadAnimation(this, R.anim.option_layout_in)
                    anim.duration = 250
                    anim.fillAfter = true
                    binding.optionLayout.startAnimation(anim)
                    child.onFocusChangeListener = null
                }
            }

            var chil = binding.layoutLinearAdder.getChildAt(2)
            Log.d("Data-note",""+chil)

        }else{
            setCurrentDate()
        }
    }

    private fun loadData() {
        var Tag = "Data-note"
        var gson = Gson()
        var note_data_string = intent.getStringExtra("note_data")
        var note_data: Note_Data_Model = gson.fromJson(note_data_string, Note_Data_Model::class.java)

        if(intent.getStringExtra("change_img") != null){
            image_changed = true
        }

        binding.etTitle.setText(note_data.title)
        binding.createdDate.setText(note_data.created_date)
        Log.d("timestamp","1 "+note_data.timestamp)
        timestamp = note_data.timestamp!!
        pinned_note = note_data.pinned_note!!

        when(note_data.note_color){
            "blue" ->{
                binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_blue)
            }
            "red" ->{
                binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_red)

            }
            "green" ->{
                binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_green)

            }
            "pink" ->{
                binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_pink)

            }
            "purple" ->{
                binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_purple)

            }
            "yellow" ->{
                binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_yellow)

            }
        }

        if(note_data.image_URL != ""){
            var uri = Uri.parse(note_data.image_URL)
            imageUri = note_data.image_URL.toString()
            Glide.with(instance)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.imageViewNote)
            binding.imageViewNote.visibility = ViewGroup.VISIBLE
            binding.imageDeleteNote.visibility = ViewGroup.VISIBLE
            binding.parentofparent.visibility = ViewGroup.VISIBLE
        }

        note_id = note_data.note_id.toString()
        formatedDate = note_data.created_date!!

//        binding.imageViewNote.setImageURI(uri)
        var orderlist = note_data.order_view_all!!.split("||||")
        var etdata = note_data.edit_text_data_all!!.split("|&@!~~~|")
        var taskdata = note_data.task_data_all!!.split("|&@!~~~|")
        var taskcheck = note_data.task_check!!.split("|&@!~~~|")

        Log.d("Data-note", "order list: $orderlist")
        Log.d("Data-note1", "etdata: "+ etdata)
        Log.d("Data-note","taskdata: $taskdata")
        var len: Int = 1
        var data: String = ""
//        for(value in etdata){
//            Log.d("Data-note1", "before: ${value.toString().length}")
//            len = value.length
//            data = value.substring(0..len - 6 )
//            Log.d("Data-note1", "after: $data \n len: $len")
//        }

        var ettext: Int = 0
        var taskindex = 0

        for((index,value) in orderlist.withIndex()){

            Log.d("Data-note1","val: "+value)

            var LL2 = findViewById<LinearLayout>(R.id.layout_linear_adder)

            if(index == 0){
                var actual_data = etdata[ettext]
                Log.d("Data-note","len: "+len)
//                var data = actual_data.substring(0..len - 8 )
                var data:String = actual_data
                Log.d("Data-note1","inside kik "+ etdata[ettext])


                binding.etDesc.setText(Html.fromHtml(data,   Html.FROM_HTML_MODE_COMPACT))
                Log.d("Data-note","data ET: "+binding.etDesc.text)
                ettext += 1
                continue
            }
            when (value) {
                "ET" -> {
                    Log.d("Data-note","ET")
                    val ET = EditText(ContextThemeWrapper(instance, R.style.Note_EditText_parent))
                    ET.layoutParams= ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    ET.id = View.generateViewId()
                    ET.textSize = 18f
                    ET.backgroundTintMode = PorterDuff.Mode.SRC_OVER
                    ET.background = null
                    ET.typeface = ResourcesCompat.getFont(instance, R.font.montserrat)
        //
                        if(index == orderlist.size -1){
                            ET.setPadding(0,0,0, 200)
                            binding.etDesc.setPadding(0,0,0,0)
                        }

                    Log.d("Data-note",""+etdata[ettext])
//                    var data_ET = etdata[ettext].substring(0 until etdata[ettext].length - 1 )
//                    Log.d("Data-note","setdata: "+data_ET)
//                    ET.setText("tick")
                    var len = etdata[ettext].length
//                    var data = etdata[ettext].substring(0..len - 6 )
                    Log.d("Data-note1","value: "+data)
                    var data = etdata[ettext]
                    ET.setText(Html.fromHtml(data,   Html.FROM_HTML_MODE_COMPACT))

                    ettext += 1
                    LL2.addView(ET)
                    Log.d("Data-note","data ET: "+ET.text)

//                    binding.layoutLinearAdder.addView(ET)
                }
                "LL" -> {
                    Log.d("Data-note","LL")
                    val linearLayoutBox = LinearLayout(instance)
                    linearLayoutBox.orientation = LinearLayout.VERTICAL
                    linearLayoutBox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    linearLayoutBox.id = View.generateViewId()
                    linearLayoutBox.gravity = Gravity.CENTER

                    ll_id = linearLayoutBox.id
//                    binding.layoutLinearAdder.addView(linearLayoutBox)
                    LL2.addView(linearLayoutBox)


                }
                "EC" -> {
                    Log.d("Data-note","EC")
                    val ETbox = ETCheckbox(instance, attrs = null)
                    ETbox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    ETbox.id = View.generateViewId()
//                    ++taskindex
                    var LL_box = findViewById<LinearLayout>(ll_id)

//                    LL2.addView(ETbox)
                    LL_box.addView(ETbox)
                    ETbox.setDataEditText(taskdata[taskindex])
                    ETbox.setcheck(taskcheck[taskindex].toBoolean())
                    ++taskindex

                }
                "LA" -> {
                    Log.d("Data-note","LA")
                    var taskAddBox = LinearLayout(instance)
                    taskAddBox.orientation = LinearLayout.HORIZONTAL
                    taskAddBox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    taskAddBox.id = View.generateViewId()
                    taskAddBox.gravity = Gravity.CENTER_VERTICAL
                    taskAddBox.setPadding(57,0,0,0)
                    var imageView_plus = ImageView(instance)
                    imageView_plus.setBackgroundResource(R.drawable.ic_rounded_plus_grey)
        //        imageView_plus.setImageResource(R.drawable.ic_rounded_plus_grey)
                    imageView_plus.layoutParams = ViewGroup.LayoutParams(45,45)
                    imageView_plus.id = View.generateViewId()
                    var TextView_add = TextView(instance)
                    TextView_add.setText("Add Task")
                    var params_tv = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params_tv.setMargins(15,0,0,0)
                    TextView_add.layoutParams = params_tv
                    TextView_add.id = View.generateViewId()
                    TextView_add.setTypeface(ResourcesCompat.getFont(instance, R.font.montserrat_semibold))
                    TextView_add.textSize = 15F
                    TextView_add.setTextColor(ContextCompat.getColor(instance, R.color.grey_400))
                    taskAddBox.addView(imageView_plus)
                    taskAddBox.addView(TextView_add)
                    taskAddBox.alpha = 0.3f
                    taskAddBox.setOnClickListener {
                        createTask(it)
                        taskAddBox.alpha = 0.3f
                    }
                    var LL_box = findViewById<LinearLayout>(ll_id)

                    LL_box.addView(taskAddBox)

                    var taskAddindex = LL_box.childCount - 1
                    var childET = LL_box.getChildAt(taskAddindex - 1) as ETCheckbox
                    if(childET.getDataEditText()){
                        taskAddBox.alpha = 1.0f
                    }

//                    LL_box.addView(taskAddBox)
        //                LL?.addView(taskAddBox)

                }
            }
        }
        Log.d("Data-note","0: "+binding.layoutLinearAdder.getChildAt(0))
        Log.d("Data-note","1: "+binding.layoutLinearAdder.getChildAt(1))
        Log.d("Data-note","2: "+binding.layoutLinearAdder.getChildAt(2))
        Log.d("Data-note","3: "+binding.layoutLinearAdder.getChildAt(3))
        Log.d("Data-note","4: "+binding.layoutLinearAdder.getChildAt(4))

    }

    private fun chkInputNewLine(actualData: String): String {
        var length = actualData.length
        Log.d("newline","length: $length")
        if(length > 3){
            Log.d("newline","inside if")
            var last_data = actualData.substring(length -5 until length).trim()
            Log.d("newline","last-data: $last_data")
            Log.d("newline","chk-eq: ${last_data == "<br>"}")
            if(last_data == "<br>"){
                var data = actualData.substring(0 until length-5)
                Log.d("newline","data: $data")
                return data
            }else{
                return actualData
            }
        }
        return actualData
    }

    private fun showDialogStyle() {
        dialog_style = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
        dialog_style.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_style.setContentView(R.layout.bottom_sheet_layout_dialog)

        var add_image = dialog_style.findViewById<TextView>(R.id.Layout_add_image)
        add_image?.setOnClickListener {
            if(connectivity == "Available"){
                gallery_intent()
            }
        }

        var select_bold = dialog_style.findViewById<CardView>(R.id.btn_Bold)
        select_bold?.setOnClickListener {
            setBold()
        }

        var select_italic = dialog_style.findViewById<CardView>(R.id.btn_italic)
        select_italic?.setOnClickListener {
            setItalic()
        }

        var select_underline = dialog_style.findViewById<CardView>(R.id.btn_underline)
        select_underline?.setOnClickListener {
            setUnderline()
        }

        var btn_rmv = dialog_style.findViewById<ImageView>(R.id.blank_btn)
        btn_rmv?.setOnClickListener {
            resetColor()
        }

        var btn_red = dialog_style.findViewById<ImageView>(R.id.red_btn)
        btn_red?.setOnClickListener {
            setColor_red()
        }
        var btn_blue = dialog_style.findViewById<ImageView>(R.id.blue_btn)
        btn_blue?.setOnClickListener {
            setColor_blue()
        }
        var btn_yellow = dialog_style.findViewById<ImageView>(R.id.yellow_btn)
        btn_yellow?.setOnClickListener {
            setColor_yellow()
        }

        var btn_pink = dialog_style.findViewById<ImageView>(R.id.pink_btn)
        btn_pink?.setOnClickListener {
            setColor_pink()
        }
        var btn_purple = dialog_style.findViewById<ImageView>(R.id.purple_btn)
        btn_purple?.setOnClickListener {
            setColor_purple()
        }
        var btn_green = dialog_style.findViewById<ImageView>(R.id.green_btn)
        btn_green?.setOnClickListener {
            setColor_green()
        }

        dialog_style.show()
        dialog_style.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog_style.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog_style.window!!.attributes.windowAnimations = R.style.DialogAnimationStyle
        dialog_style.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun showDialogColor(){
        dialog_color = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
        dialog_color.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog_color.setContentView(R.layout.bottom_sheet_layout_dialog_color_note)

        var btn_red = dialog_color.findViewById<ImageView>(R.id.red_btn)
        btn_red?.setOnClickListener {
            binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_red)
            note_color_hole = "red"
            dialog_color.cancel()
        }

        var btn_blue = dialog_color.findViewById<ImageView>(R.id.blue_btn)
        btn_blue?.setOnClickListener {
            binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_blue)
            note_color_hole = "blue"
            dialog_color.cancel()
        }

        var btn_dblue = dialog_color.findViewById<ImageView>(R.id.yellow_btn)
        btn_dblue?.setOnClickListener {
            binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_yellow)
            note_color_hole = "yellow"
            dialog_color.cancel()
        }
        var btn_green = dialog_color.findViewById<ImageView>(R.id.green_btn)
        btn_green?.setOnClickListener {
            binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_green)
            note_color_hole = "green"
            dialog_color.cancel()
        }

        var btn_pink = dialog_color.findViewById<ImageView>(R.id.pink_btn)
        btn_pink?.setOnClickListener {
            binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_pink)
            note_color_hole = "pink"
            dialog_color.cancel()
        }
        var btn_purple = dialog_color.findViewById<ImageView>(R.id.purple_btn)
        btn_purple?.setOnClickListener {
            binding.noteColorBtn.setBackgroundResource(R.drawable.hole_punch_circle_purple)
            note_color_hole = "purple"
            dialog_color.cancel()
        }





        dialog_color.show()
        dialog_color.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog_color.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog_color.window!!.attributes.windowAnimations = R.style.DialogAnimationStyle
        dialog_color.window!!.setGravity(Gravity.BOTTOM)
    }

    private fun styledText(p0: CharSequence?): String {
        val newString = StringBuilder()
        val stack = Stack<Pair<Int, Char>>()

        for(c in p0!!.indices){
            if(p0[c].equals('*')){
                Log.d("StyledText","inside: "+newString)
                if(stack.isNotEmpty() && stack.peek().second == '*'){
                    val top = stack.pop()
                    val startIndex = top.first
                    newString.setRange(startIndex, startIndex+1,"<b>")
                    newString.append("</b>")
                }else{
                    stack.push(Pair(newString.length, '*'))
                    newString.append(p0[c])
                }
            } else{
                newString.append(p0[c])
            }
        }
        return newString.toString()
    }

    private fun saveNote() {

        Log.d("Here","inside")
        if (binding.etTitle.text.isNotEmpty() && binding.etDesc.text.isNotEmpty()) {
            Log.d("Here","inside")
            var layout = findViewById<LinearLayout>(R.id.layout_linear_adder)
            var layout_childs = layout.children
            Log.d("Here",""+layout)
            for (child in layout_childs) {
                if (child is EditText) {
                    order_view_all += "ET||||"

                    var edittext_child_data = child.text

                    spannable_html = Html.toHtml(edittext_child_data, Html.FROM_HTML_MODE_COMPACT)

                    Log.d("Data","span html: "+ spannable_html)
                    var data = chkInputNewLine(spannable_html)
                    edit_text_data_all += "$data|&@!~~~|"
                } else if (child is LinearLayout) {
                    order_view_all += "LL||||"
                    var LiLayout = child as LinearLayout
                    var LiLayout_childs = LiLayout.children
                    for (parent_child in LiLayout_childs) {
                        if (parent_child is ETCheckbox) {
                            order_view_all += "EC||||"
                            var ETCheckbox_data = parent_child.getDataETtext()
                            var check = parent_child.getcheck().toString()
                            task_data_check_all += "$check|&@!~~~|"
                            task_data_all += "$ETCheckbox_data|&@!~~~|"
                        }else if (parent_child is LinearLayout){
                            order_view_all += "LA||||"
                        }
                    }

                }
            }
            order_view_all = order_view_all.substring(0, order_view_all.length - 4)
            edit_text_data_all = edit_text_data_all.substring(0, edit_text_data_all.length - 8)
            if(task_data_all.isNotEmpty()){
                task_data_all = task_data_all.substring(0, task_data_all.length - 8)
            }
//            var check_length = task_data_check_all.split("|&@!~~~|")
//            Log.d("Data-note2","length: "+check_length[0].isEmpty())
//            if(task_data_check_all.isNotEmpty()){
//                task_data_check_all = task_data_check_all.substring(0, task_data_check_all.length - 8)
//            }

            Log.d("Data-note","Here is the data")
            Log.d("Data-note","Order All :"+order_view_all)
            Log.d("Data-note","ET Data All :"+edit_text_data_all)
            Log.d("Data-note","Task Data All: "+task_data_all)


            firebaseFirestore.collection("Notes")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Mynotes")
                .document(note_id)
                .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), imageUri, order_view_all, edit_text_data_all, task_data_all, note_id, note_color_hole, task_data_check_all, formatedDate, pinned_note, timestamp, timestamp))
                .addOnSuccessListener {
                    Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
                    if(!intentcalled){
                        intentcalled = true
                        val intent = Intent(this@Note_Activity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    if(imageUri.isNotEmpty() && image_changed){
                        uploadPhotos(desc)
                    }


                }
                .addOnFailureListener {
                    Log.d("Data","Exception Firebase: "+it.message)
                    Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
                }
            if(!intentcalled){
                intentcalled = true
                val intent = Intent(this@Note_Activity, MainActivity::class.java)
                startActivity(intent)
            }



        } else if (binding.etTitle.text.isEmpty() && binding.etDesc.text.isNotEmpty()) {
            Log.d("Here","inside")
            var layout = findViewById<LinearLayout>(R.id.layout_linear_adder)
            var layout_childs = layout.children
            Log.d("Here",""+layout)
            for (child in layout_childs) {
                if (child is EditText) {
                    order_view_all += "ET||||"

                    var edittext_child_data = child.text

                    spannable_html = Html.toHtml(edittext_child_data, Html.FROM_HTML_MODE_COMPACT)

                    Log.d("Data","span html: "+ spannable_html)
                    var data = chkInputNewLine(spannable_html)
                    edit_text_data_all += "$data|&@!~~~|"
                } else if (child is LinearLayout) {
                    order_view_all += "LL||||"
                    var LiLayout = child as LinearLayout
                    var LiLayout_childs = LiLayout.children
                    for (parent_child in LiLayout_childs) {
                        if (parent_child is ETCheckbox) {
                            order_view_all += "EC||||"
                            var ETCheckbox_data = parent_child.getDataETtext()
                            var check = parent_child.getcheck().toString()
                            task_data_check_all += "$check|&@!~~~|"
                            task_data_all += "$ETCheckbox_data|&@!~~~|"
                        }else if (parent_child is LinearLayout){
                            order_view_all += "LA||||"
                        }
                    }

                }
            }
            order_view_all = order_view_all.substring(0, order_view_all.length - 4)
            edit_text_data_all = edit_text_data_all.substring(0, edit_text_data_all.length - 8)
            if(task_data_all.isNotEmpty()){
                task_data_all = task_data_all.substring(0, task_data_all.length - 8)

            }
//            var check_length = task_data_check_all.split("|&@!~~~|")
//            Log.d("Data-note2","length: $check_length")
//            if(task_data_check_all.isNotEmpty()){
//                task_data_check_all = task_data_check_all.substring(0, task_data_check_all.length - 8)
//            }

            desc = binding.etDesc.text.toString()
            var desc_length = desc.length
            Log.d("length_desc","$desc_length")
            if(desc_length >= 6){
                desc = desc.substring(0..5)+"..."
            } else if(desc_length >= 5){
                desc = desc.substring(0..5)+"..."
            } else if(desc_length >= 4){
                desc = desc.substring(0..4)+"..."
            } else if(desc_length >= 3){
                desc = desc.substring(0..3)+"..."
            } else if(desc_length >= 2){
                desc = desc.substring(0..2)+"..."
            } else if(desc_length >= 1){
                desc = desc.substring(0..1)+"..."
            }

            Log.d("Data","Here is the data")

                        firebaseFirestore.collection("Notes")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Mynotes")
                        .document(note_id)
                        .set(Note_Data_Model(desc, binding.etDesc.text.toString(), imageUri, order_view_all, edit_text_data_all, task_data_all, note_id, note_color_hole, task_data_check_all, formatedDate, pinned_note, timestamp, timestamp))
                        .addOnSuccessListener {
                            Toast.makeText(this,"Data Saved in Firestore", Toast.LENGTH_SHORT).show()
                            if(!intentcalled){
                                intentcalled = true
                                val intent = Intent(this@Note_Activity, MainActivity::class.java)
                                startActivity(intent)
                            }
                            if(imageUri.isNotEmpty()){
                                uploadPhotos(desc)
                            }
                            finish()

                        }
                        .addOnFailureListener {
                            Log.d("Data","Exception Firebase: "+it.message)
                            Toast.makeText(this, "Data Failed to save", Toast.LENGTH_SHORT).show()
                        }
            if(!intentcalled){
                intentcalled = true
                val intent = Intent(this@Note_Activity, MainActivity::class.java)
                startActivity(intent)
            }


        }
//        Log.d("Data","Order: "+order_view_all)
//        Log.d("Data","ET Data: "+edit_text_data_all)
//        Log.d("Data","ETCheckbox Data: "+task_data_all.split("|&@!~~~|"))
    }

    private fun setUnderline() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){

                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(UnderlineSpan(),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )

                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){

                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(UnderlineSpan(),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setItalic() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){

                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(StyleSpan(Typeface.ITALIC),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )

                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){

                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(StyleSpan(Typeface.ITALIC),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setBold() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(StyleSpan(Typeface.BOLD),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(StyleSpan(Typeface.BOLD),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun createTask(view:View) {
        val linearLayoutBox = LinearLayout(instance)
        linearLayoutBox.orientation = LinearLayout.VERTICAL
        linearLayoutBox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        linearLayoutBox.id = View.generateViewId()
        linearLayoutBox.gravity = Gravity.CENTER

        val deletebtn = ImageView(instance)
        deletebtn.setBackgroundResource(R.drawable.ic_delete)
        deletebtn.layoutParams = ViewGroup.LayoutParams(33, 26)



        val ETbox = ETCheckbox(instance, attrs = null)
        ETbox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ETbox.id = View.generateViewId()

        var params_tv = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params_tv.setMargins(15,0,0,0)
//        var params_plus = LinearLayout.LayoutParams(20, 20)
//        params_plus.setMargins(10,0,0,0)

        var taskAddBox = LinearLayout(instance)
        taskAddBox.orientation = LinearLayout.HORIZONTAL
        taskAddBox.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        taskAddBox.id = View.generateViewId()
        taskAddBox.gravity = Gravity.CENTER_VERTICAL
        taskAddBox.setPadding(57,0,0,0)

        var imageView_plus = ImageView(instance)
        imageView_plus.setBackgroundResource(R.drawable.ic_rounded_plus_grey)
//        imageView_plus.setImageResource(R.drawable.ic_rounded_plus_grey)
        imageView_plus.layoutParams = ViewGroup.LayoutParams(45,45)
        imageView_plus.id = View.generateViewId()

        var TextView_add = TextView(instance)
        TextView_add.setText("Add Task")

        TextView_add.layoutParams = params_tv
        TextView_add.id = View.generateViewId()
        TextView_add.setTypeface(ResourcesCompat.getFont(instance, R.font.montserrat_semibold))
        TextView_add.textSize = 15F
        TextView_add.setTextColor(ContextCompat.getColor(instance, R.color.grey_400))

        taskAddBox.addView(imageView_plus)
        taskAddBox.addView(TextView_add)

        taskAddBox.alpha = 0.3f

        taskAddBox.setOnClickListener {
            createTask(it)
            taskAddBox.alpha = 0.3f
        }

//        val LayoutETbox = ConstraintLayout(this)
//        LayoutETbox.id = View.generateViewId()
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(LayoutETbox)
//        constraintSet.connect(ETbox.id, ConstraintSet.START, ETbox.id, ConstraintSet.TOP)
//        constraintSet.applyTo(LayoutETbox)

        val ET = EditText(ContextThemeWrapper(instance, R.style.Note_EditText_parent))

        ET.layoutParams= ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ET.id = View.generateViewId()
        ET.textSize = 18f

//        ET.textCursorDrawable = ContextCompat.getDrawable(this, R.drawable.cursor)
        ET.backgroundTintMode = PorterDuff.Mode.SRC_OVER
        ET.background = null
        ET.typeface = ResourcesCompat.getFont(instance, R.font.montserrat)

//        Toast.makeText(instance,""+(binding.layoutLinearAdder.focusedChild is AppCompatEditText),Toast.LENGTH_LONG).show()





        var layoutLinearAdder = findViewById<LinearLayout>(R.id.layout_linear_adder)

        var clicked = view

         if(binding.layoutLinearAdder.focusedChild is LinearLayout){
            var parent = binding.layoutLinearAdder.focusedChild as LinearLayout
            Log.d("index",""+parent.getChildAt(0))
            if(parent.getChildAt(0) is ETCheckbox){
                var childcount = parent.childCount
                Log.d("index","inside")
                var childschildid = parent.getChildAt(childcount-2).id
                var childchlid = findViewById<ETCheckbox>(childschildid)
                if(childchlid.getDataEditText()){
                    parent.addView(ETbox, childcount-1)
                    Log.d("index","inside")
                }
            }
        }else if(binding.layoutLinearAdder.focusedChild is EditText ||
                binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            var index_et = binding.layoutLinearAdder.indexOfChild(binding.layoutLinearAdder.focusedChild)
            var current_et = binding.layoutLinearAdder.getChildAt(index_et) as EditText
            if(binding.layoutLinearAdder.getChildAt(index_et-1) is LinearLayout && current_et.text.isEmpty()){
                var child = binding.layoutLinearAdder.getChildAt(index_et-1) as LinearLayout
                var childcount = child.childCount
                Log.d("index","negative index")
                if(child.getChildAt(childcount-2) is ETCheckbox){
                    var childschildid = child.getChildAt(childcount-2).id
                    var childchlid = findViewById<ETCheckbox>(childschildid)
                    if(childchlid.getDataEditText()){
                        child.addView(ETbox, childcount-1)
                    }
                }
            }else{
                if(binding.layoutLinearAdder.getChildAt(index_et+1) is LinearLayout){
                    var child = binding.layoutLinearAdder.getChildAt(index_et+1) as LinearLayout
                    var childcount = child.childCount

                    Log.d("index","positive index "+child.getChildAt(childcount-2).id)
                    if(child.getChildAt(childcount-2) is ETCheckbox){
                        var childschildid = child.getChildAt(childcount-2).id
                        var childchlid = findViewById<ETCheckbox>(childschildid)
                        if(childchlid.getDataEditText()){
                            child.addView(ETbox, childcount - 1)
                        }
                    }
                } else{
                    linearLayoutBox.addView(ETbox)
                    linearLayoutBox.addView(taskAddBox)
                    binding.layoutLinearAdder.addView(linearLayoutBox, index_et + 1)
                    if(binding.layoutLinearAdder.getChildAt(index_et+2) !is EditText){
                        current_et.setPadding(0,0,0,0)

                        ET.setPadding(0,0,0, 200)
                        binding.layoutLinearAdder.addView(ET)
                    }

                }
            }


        }else if(clicked is LinearLayout){

            var parent = clicked.parent as LinearLayout
             if(parent.getChildAt(0) is ETCheckbox){
                 var childcount = parent.childCount
                 Log.d("index","inside")
                 var childschildid = parent.getChildAt(childcount-2).id
                 var childchlid = findViewById<ETCheckbox>(childschildid)
                 if(childchlid.getDataEditText()){
                     parent.addView(ETbox, childcount-1)
                     Log.d("index","inside")
                 }
             }

         }else{
            var childcount = binding.layoutLinearAdder.childCount
            if(binding.layoutLinearAdder.getChildAt(childcount-1) is EditText &&
                !binding.layoutLinearAdder.getChildAt(childcount-1).equals(findViewById(R.id.et_desc))){
                var chket = binding.layoutLinearAdder.getChildAt(childcount - 1) as EditText
                if(chket.text.isNotEmpty()){
                    var child = binding.layoutLinearAdder.focusedChild
                    Log.d("index",""+child)

                    linearLayoutBox.addView(ETbox)
                    linearLayoutBox.addView(taskAddBox)
                    var childcout = binding.layoutLinearAdder.childCount
                    if(binding.layoutLinearAdder.getChildAt(childcout-1) is EditText){
                        var child = binding.layoutLinearAdder.getChildAt(childcout -1)
                        child.setPadding(0,0,0,0)
                    }
                    binding.layoutLinearAdder.addView(linearLayoutBox)
                    ET.setPadding(0,0,0, 200)
                    binding.layoutLinearAdder.addView(ET)
                }
            }


        }
    }

    private fun setCurrentDate() {
        var currentdate = Calendar.getInstance().time
        var DateFormat = SimpleDateFormat("EEE, MMM dd, ''yyyy", Locale.getDefault())
        formatedDate = DateFormat.format(currentdate)
        binding.createdDate.text = formatedDate

        timestamp = Timestamp.now()
    }

    private fun setColor_red() {
        dialog_style.cancel()



        if(binding.layoutLinearAdder.focusedChild is EditText ||
                binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#EA8383")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#EA8383")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }

//        Log.d("Spannable", ""+spannable_html)
    }

    private fun resetColor() {
        var childview = binding.layoutLinearAdder.focusedChild as EditText
        var spanna = childview.text
        var spantoRemove = spanna.getSpans<ForegroundColorSpan>(childview.selectionStart, childview.selectionEnd)

        for(span in spantoRemove){
            Log.d("Sapntoremove","${span}")
            if(span is CharacterStyle){
                spanna.removeSpan(span)
            }
        }

    }

    private fun setColor_blue() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#83C5EA")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#83C5EA")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }

//        Log.d("Spannable", ""+spannable_html)
    }

    private fun setColor_green() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#91EA83")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#91EA83")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }

//        Log.d("Spannable", ""+spannable_html)
    }

    private fun setColor_yellow() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#E8EA83")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#E8EA83")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }

//        Log.d("Spannable", ""+spannable_html)
    }
    private fun setColor_purple() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#C383EA")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#C383EA")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }

//        Log.d("Spannable", ""+spannable_html)
    }
    private fun setColor_pink() {
        dialog_style.cancel()

        if(binding.layoutLinearAdder.focusedChild is EditText ||
            binding.layoutLinearAdder.focusedChild is AppCompatEditText){
            if(binding.layoutLinearAdder.focusedChild is EditText){
                var childview = binding.layoutLinearAdder.focusedChild as EditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#EA83C0")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
            if(binding.layoutLinearAdder.focusedChild is AppCompatEditText){
                var childview = binding.layoutLinearAdder.focusedChild as AppCompatEditText
                if(childview.hasSelection()){
                    spannableString = SpannableStringBuilder(childview.text)
                    spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#EA83C0")),
                        childview.selectionStart,
                        childview.selectionEnd,
                        0
                    )
                    childview.setText(spannableString)
                }else{
                    Toast.makeText(this,"Text Selection is needed", Toast.LENGTH_LONG).show()
                }
            }
        }

//        Log.d("Spannable", ""+spannable_html)
    }


    private fun uploadPhotos(desc: String) {

            var uri = Uri.parse(imageUri)
            var path = "images/${auth.currentUser!!.uid}/${uri.lastPathSegment}"
            val imageRef = storageReference.child(path)
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener {
                Log.i("upload-task", "Image Uploaded $imageRef")
                val downloadUrl = imageRef.downloadUrl
                downloadUrl.addOnSuccessListener {
                    remoteUri ->
                    Log.d("remote_url",""+remoteUri)
                    updatePhotoDatabase(remoteUri.toString(),desc)
                }
            }
            uploadTask.addOnFailureListener {
                Log.e("upload-task", it.message ?: "No Message")
            }


    }

    private fun updatePhotoDatabase(remoteUri: String, desc: String) {

        if(desc.isEmpty()){
            firebaseFirestore.collection("Notes")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Mynotes")
                .document(note_id)
                .set(Note_Data_Model(binding.etTitle.text.toString(), binding.etDesc.text.toString(), remoteUri, order_view_all, edit_text_data_all, task_data_all, note_id, note_color_hole, task_data_check_all, formatedDate, pinned_note, timestamp, timestamp))
        } else{
            firebaseFirestore.collection("Notes")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Mynotes")
                .document(note_id)
                .set(Note_Data_Model(desc, binding.etDesc.text.toString(), remoteUri, order_view_all, edit_text_data_all, task_data_all, note_id, note_color_hole, task_data_check_all, formatedDate, pinned_note, timestamp, timestamp))
        }


    }

    private fun gallery_intent() {
        dialog_style.cancel()
        var intent = Intent(Intent.ACTION_PICK)
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            binding.imageViewNote.visibility = ViewGroup.VISIBLE
            binding.imageDeleteNote.visibility = ViewGroup.VISIBLE
            binding.parentofparent.visibility = ViewGroup.VISIBLE
            val parmas = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            parmas.setMargins(0,0,0,10)
            var ll = findViewById<LinearLayout>(R.id.layout_linear_adder)
            ll.layoutParams = parmas

            ll.setPadding(0, 15, 0, 0)

            var outanim = AnimationUtils.loadAnimation(this@Note_Activity, R.anim.popdown_delete_btn_image_note)
            binding.imageDeleteNote.startAnimation(outanim)
            var uri = it.data!!.data
//            val photo = Note_Image_Data_Model(path = "images/${auth.currentUser!!.uid}/${uri!!.lastPathSegment}" ,localUri = it.data!!.data.toString())
//            photos.add(photo)

            imageUri = uri.toString()

            binding.imageViewNote.setImageURI(uri)
            image_changed = true
            Log.d("uir",""+binding.imageViewNote)
        }
    }



}