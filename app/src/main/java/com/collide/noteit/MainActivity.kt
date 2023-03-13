package com.collide.noteit

import android.content.ContentValues
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.VibratorManager
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivityMainBinding
import com.collide.noteit.recyclerAdapter.NoteViewDispalyAdapter
import com.collide.noteit.tools.ProfileActivity
import com.collide.noteit.tools.loadingDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NoteViewDispalyAdapter.onNoteListener {

    private var _binding:ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteArrayListUnpinned: ArrayList<Note_Data_Model>
    private lateinit var noteArrayListPinned: ArrayList<Note_Data_Model>
    lateinit var vibarator_manager: VibratorManager
    var x1: Float = 0.0f
    var y1: Float = 0.0f
    var x2: Float = 0.0f
    var y2: Float = 0.0f
    companion object {
        const val SHARED_PREFERS = "remoteUri"
        const val URI_ = "uri"
    }
    var snapshot_count = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: DatabaseReference
    private var firebaseReference = Firebase.storage.reference
    lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var filterbottomSheetDialog: BottomSheetDialog
    var flag = "off"
    private lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var notedisplayadapterunpinned: NoteViewDispalyAdapter
    lateinit var notedisplayadapterpinned: NoteViewDispalyAdapter
    var user_data: User_Profile_Detail? = null
    var filtered = false
    var profile_Icon_value: String = ""
    var filteredLIst_unpin = arrayListOf<Note_Data_Model>()
    var filteredLIst_pinned = arrayListOf<Note_Data_Model>()
    var iconfilter = arrayListOf<Note_Data_Model>()
    var iconfilter_bool = false
    var current_query_string = ""
    private var loadingDialog = loadingDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            })

        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.searchView.background = ContextCompat.getDrawable(this, R.drawable.search_border_transition_drawable_dark)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.searchView.background = ContextCompat.getDrawable(this, R.drawable.search_border_transition_drawable)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database.reference.child("users")
        firebaseDatabase.keepSynced(true)
        firebaseFirestore = Firebase.firestore
        noteArrayListUnpinned = arrayListOf()
        noteArrayListPinned = arrayListOf()
        setupTvNote()

        binding.searchView.clearFocus()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(p0: String?): Boolean {
                filterdListUnpinned(p0)
                filterdListPinned(p0)
                return true
            }
        })
        Firebase.database.getReference("users")
            .child(auth.uid.toString())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user_data = snapshot.getValue(User_Profile_Detail::class.java)
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        setProfileIcon()
        setRecyclerView()

        var query_ff = firebaseFirestore.collection("Notes")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("Mynotes")
        var countQuery = query_ff.count()
        countQuery.get(AggregateSource.SERVER).addOnCompleteListener {
            if(it.isSuccessful){
                val snapshot = it.result
                snapshot_count = ""+snapshot.count
            } else{
                Log.d("count_query", ""+it.exception)
            }
        }
        binding.filter.setOnClickListener {
            filterItem()
        }


        binding.materialCardView.setOnClickListener {
            var intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("note_count", ""+notedisplayadapterunpinned.itemCount)
            intent.putExtra("note_pinned_count", ""+notedisplayadapterpinned.itemCount)

            var gson = Gson()
            var note_gson = gson.toJson(user_data)
            intent.putExtra("ProfileURL", profile_Icon_value)
            intent.putExtra("user_data",note_gson)
            startActivity(intent)
        }

        binding.recyclerViewPinned.addOnScrollListener(object:  RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0){

                    if(flag.equals("off")){
                        var downanim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.scroll_down_menu_anim)
                        binding.optionLayout.startAnimation(downanim)
                        flag="on"
                    }

                } else{
                    if(flag.equals("on")){
                        var downanim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.scroll_up_menu_anim)
                        binding.optionLayout.startAnimation(downanim)
                        flag="off"
                    }
                }
            }
        })

        binding.searchView.setOnQueryTextFocusChangeListener { view, b ->
            var transition: TransitionDrawable = view.background as TransitionDrawable
            if(b == true){
                transition.startTransition(300)
            } else{
                transition.reverseTransition(300)
            }
        }

        binding.optionLayout.setOnClickListener {
            val intent = Intent(this, Note_Activity::class.java)
            finish()
            startActivity(intent)
        }

        binding.btnCamera.setOnClickListener {
            var intent = Intent(this, CameraActivity::class.java)
            finish()
            startActivity(intent)
            overridePendingTransition(R.anim.right_slide_in_acitivity, R.anim.left_slide_out_acitivity)
        }
    }

    private fun filterItem() {
        filterbottomSheetDialog = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
        filterbottomSheetDialog.setContentView(R.layout.bottom_sheet_layout_dialog_filter)
        var btn_blank = filterbottomSheetDialog.findViewById<ImageView>(R.id.blank_btn)
        btn_blank?.setOnClickListener {
            if(current_query_string.isEmpty()){
                notedisplayadapterunpinned.setFilteredList(noteArrayListUnpinned)
                notedisplayadapterpinned.setFilteredList(noteArrayListPinned)
            }else{
                notedisplayadapterunpinned.setFilteredList(filteredLIst_unpin)
                notedisplayadapterpinned.setFilteredList(filteredLIst_pinned)
            }
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = false
        }
        var btn_red = filterbottomSheetDialog.findViewById<ImageView>(R.id.red_btn)
        btn_red?.setOnClickListener {
            setCurrrentRecycler()
            filtericonitem("red")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_blue = filterbottomSheetDialog.findViewById<ImageView>(R.id.blue_btn)
        btn_blue?.setOnClickListener {
            setCurrrentRecycler()
            filtericonitem("blue")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_green = filterbottomSheetDialog.findViewById<ImageView>(R.id.green_btn)
        btn_green?.setOnClickListener {
            setCurrrentRecycler()
            filtericonitem("green")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_pink = filterbottomSheetDialog.findViewById<ImageView>(R.id.pink_btn)
        btn_pink?.setOnClickListener {
            setCurrrentRecycler()
            filtericonitem("pink")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_purple = filterbottomSheetDialog.findViewById<ImageView>(R.id.purple_btn)
        btn_purple?.setOnClickListener {
            setCurrrentRecycler()
            filtericonitem("purple")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_yellow = filterbottomSheetDialog.findViewById<ImageView>(R.id.yellow_btn)
        btn_yellow?.setOnClickListener {
            setCurrrentRecycler()
            filtericonitem("yellow")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        filterbottomSheetDialog.show()
    }

    private fun setCurrrentRecycler() {
        if(current_query_string != ""){
            notedisplayadapterunpinned.setFilteredList(filteredLIst_unpin)
            notedisplayadapterpinned.setFilteredList(filteredLIst_pinned)
        } else{
            notedisplayadapterunpinned.setFilteredList(noteArrayListUnpinned)
            notedisplayadapterpinned.setFilteredList(noteArrayListPinned)
        }
    }

    private fun filtericonitem(s: String) {
        var list_pinned = notedisplayadapterpinned.getCurrentList()
        var icon_list_pin = arrayListOf<Note_Data_Model>()
        var list_unpinned = notedisplayadapterunpinned.getCurrentList()
        var icon_list_unpin = arrayListOf<Note_Data_Model>()
        for(item in list_pinned){
            if(item.note_color!!.lowercase().contains(s)){
                icon_list_pin.add(item)
            }
        }
        notedisplayadapterpinned.setFilteredList(icon_list_pin)
        for(item in list_unpinned){
            if(item.note_color!!.lowercase().contains(s)){
                icon_list_unpin.add(item)
            }
        }
        notedisplayadapterunpinned.setFilteredList(icon_list_unpin)
    }


    private fun filterdListUnpinned(newText: String?) {
        current_query_string = newText!!
        filteredLIst_unpin.clear()
        for(item in noteArrayListUnpinned){
            if(item.title!!.lowercase().contains(newText.lowercase())){
                filteredLIst_unpin.add(item)
            }
        }
            notedisplayadapterunpinned.setFilteredList(filteredLIst_unpin)
        filtered = true
        if (newText.isEmpty()){
            current_query_string = ""
            filtered = false
            notedisplayadapterunpinned.setFilteredList(noteArrayListUnpinned)
        }
    }

    private fun filterdListPinned(newText: String?) {
        current_query_string = newText!!
        filteredLIst_pinned.clear()
        for(item in noteArrayListPinned){
            if(item.title!!.lowercase().contains(newText.lowercase())){
                filteredLIst_pinned.add(item)
            }
        }
        notedisplayadapterpinned.setFilteredList(filteredLIst_pinned)
            filtered = true
        if (newText.isEmpty()){
            current_query_string = ""
            filtered = false
            notedisplayadapterpinned.setFilteredList(noteArrayListPinned)
        }
    }
    private fun setupTvNote() {
        if(noteArrayListPinned.size == 0){
            binding.tvUnpin.visibility = ViewGroup.GONE
            binding.noteText.text = "Notes"
        } else if(noteArrayListPinned.size > 0){
            binding.noteText.text = "Pinned"

            if(noteArrayListUnpinned.size == 0){
                return
            }else{
                binding.tvUnpin.visibility = ViewGroup.VISIBLE
            }

        }
    }

    private fun setRecyclerView() {
        loadingDialog.startloading()
        noteArrayListUnpinned.clear()
        noteArrayListPinned.clear()
        val ref = firebaseFirestore.collection("Notes")
            .document(auth.uid!!)
            .collection("Mynotes")
        ref.get().addOnSuccessListener { result ->
            for(value in result){
                var note = value.toObject<Note_Data_Model>()
                if(note.pinned_note == "Unpinned"){
                    noteArrayListUnpinned.add(note)
                } else{
                    noteArrayListPinned.add(note)
                }
            }
            noteArrayListPinned.sortByDescending {
                it.timestamp
            }
            noteArrayListUnpinned.sortByDescending {
                it.timestamp
            }
            if(!filtered){
                notedisplayadapterunpinned = NoteViewDispalyAdapter(noteArrayListUnpinned, this, this)
                binding.recyclerViewUnpinned.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerViewUnpinned.adapter = notedisplayadapterunpinned
                notedisplayadapterunpinned.notifyDataSetChanged()

                notedisplayadapterpinned = NoteViewDispalyAdapter(noteArrayListPinned, this, this)
                binding.recyclerViewPinned.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerViewPinned.adapter = notedisplayadapterpinned
                notedisplayadapterpinned.notifyDataSetChanged()
            }
            setupTvNote()
            loadingDialog.isDismis()
        }.addOnFailureListener {
            Log.d("it_exp",""+it.message)
        }
    }

    private fun setProfileIcon() {
        if(loadProfileLocalData()){
            firebaseDatabase.child(auth.currentUser!!.uid).child("profile_image")
                .get()
                .addOnSuccessListener {
                    Log.d("profile_Icon_value",""+it.value.toString())

                    profile_Icon_value = it.value.toString().split("/")[1]

                    Log.d("profile_Icon_value",""+profile_Icon_value)

                    when(profile_Icon_value){
                        "av1.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av1))
                        }
                        "av2.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av2))
                        }
                        "av3.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av3))
                        }
                        "av4.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av4))
                        }
                        "av5.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av5))
                        }
                        "av6.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av6))
                        }
                        "av7.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av7))
                        }
                        "av8.png"->{
                            binding.profileIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av8))
                        }
                    }
                }
        }
    }

    private fun loadProfileLocalData(): Boolean{
        var sharedPreferences = getSharedPreferences(SHARED_PREFERS, MODE_PRIVATE)
        if(sharedPreferences.getString(URI_, "")!!.isNotEmpty()){

            var remoteURI = sharedPreferences.getString(URI_, "")
            var uri = Uri.parse(remoteURI)

            Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.profileIcon)
            return false
        }else{
            return true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(_binding != null){
            _binding = null
        }
    }

    override fun onNoteClick(position: Int, view: View) {
        var note: Note_Data_Model
        if(view.transitionName == "RU"){
            val cList = notedisplayadapterunpinned.getCurrentList()
            Log.d("Data------", "$cList")
            note = cList[position]
        }else{
            val cList = notedisplayadapterpinned.getCurrentList()
            Log.d("Data------", "$cList")
            note = cList[position]
        }
        var intent = Intent(this, Note_Activity::class.java)
        var gson = Gson()
        var note_gson = gson.toJson(note)
        intent.putExtra("note_data",note_gson)
        startActivity(intent)
    }

    override fun onNoteOption(position: Int) {
        if(!filtered){
        var note = noteArrayListUnpinned[position]
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            vibarator_manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibarator_manager.defaultVibrator
        } else{
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE)
        }

        bottomSheetDialog = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_delete_pin)
            var share_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.share_note)!!
            share_btn.setOnClickListener {
                var gson = Gson()
                var note_gson = gson.toJson(note)
                createData(note_gson, note.title)
            }
        var delete_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.delete_note)!!
        delete_btn.setOnClickListener {
            var note_id = note.note_id!!
            noteArrayListUnpinned.removeAt(position)
            noteArrayListUnpinned.sortByDescending {
                it.timestamp
            }
            notedisplayadapterunpinned.notifyItemRemoved(position)
            bottomSheetDialog.dismiss()
            setupTvNote()
            Toast.makeText(this, "Note Deleted", Toast.LENGTH_LONG).show()
            firebaseFirestore.collection("Notes")
                .document(auth.uid!!)
                .collection("Mynotes")
                .document(note_id)
                .delete()
                .addOnSuccessListener {
                    setRecyclerView()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Note Unable Delete", Toast.LENGTH_LONG).show()
                }
        }
        var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
        var pin_text: TextView = bottomSheetDialog.findViewById(R.id.tv_pin)!!
            pin_text.text = "Pin Note"
        pin_note.setOnClickListener {
            var time_stamp = Timestamp.now()
            noteArrayListPinned.add(noteArrayListUnpinned[position])
            noteArrayListUnpinned.removeAt(position)
            noteArrayListPinned.sortByDescending {
                it.timestamp
            }
            noteArrayListUnpinned.sortByDescending {
                it.timestamp
            }
            notedisplayadapterunpinned.notifyItemRemoved(position)
            notedisplayadapterpinned.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
            setupTvNote()
            Toast.makeText(this, "Pinned",Toast.LENGTH_LONG).show()
            firebaseFirestore.collection("Notes")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Mynotes")
                .document(note.note_id!!)
                .update("pinned_note", "Pinned", "timestamp2", time_stamp)
                .addOnSuccessListener {
                    setRecyclerView()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Unable to Pin Note",Toast.LENGTH_LONG).show()
                }
        }
        bottomSheetDialog.show()

        } else{

            var note = filteredLIst_unpin[position]

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                vibarator_manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibarator_manager.defaultVibrator
            } else{
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE)
            }

            bottomSheetDialog = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_delete_pin)
            var share_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.share_note)!!
            share_btn.setOnClickListener {
                var gson = Gson()
                var note_gson = gson.toJson(note)
                createData(note_gson, note.title)
            }
            var delete_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.delete_note)!!
            delete_btn.setOnClickListener {
                var note_id = note.note_id!!
                filteredLIst_unpin.removeAt(position)
                filteredLIst_unpin.sortByDescending {
                    it.timestamp
                }
                notedisplayadapterunpinned.notifyItemRemoved(position)
                bottomSheetDialog.dismiss()
                setupTvNote()
                Toast.makeText(this, "Note Deleted", Toast.LENGTH_LONG).show()
                firebaseFirestore.collection("Notes")
                    .document(auth.uid!!)
                    .collection("Mynotes")
                    .document(note_id)
                    .delete()
                    .addOnSuccessListener {
                        setRecyclerView()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Note Unable Delete", Toast.LENGTH_LONG).show()
                    }
            }

            var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
            var pin_text: TextView = bottomSheetDialog.findViewById(R.id.tv_pin)!!
            pin_text.text = "Pin Note"
            pin_note.setOnClickListener {
                var time_stamp = Timestamp.now()
                filteredLIst_pinned.add(filteredLIst_unpin[position])
                filteredLIst_unpin.removeAt(position)
                filteredLIst_pinned.sortByDescending {
                    it.timestamp
                }
                filteredLIst_unpin.sortByDescending {
                    it.timestamp
                }
                notedisplayadapterunpinned.notifyItemRemoved(position)
                notedisplayadapterpinned.notifyDataSetChanged()
                bottomSheetDialog.dismiss()
                setupTvNote()
                Toast.makeText(this, "Pinned",Toast.LENGTH_LONG).show()
                firebaseFirestore.collection("Notes")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Mynotes")
                    .document(note.note_id!!)
                    .update("pinned_note", "Pinned", "timestamp2", time_stamp)
                    .addOnSuccessListener {
                        setRecyclerView()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Unable to Pin Note",Toast.LENGTH_LONG).show()
                    }
            }
            bottomSheetDialog.show()
        }
    }

    override fun onNoteOptionUnpin(position: Int) {
        if(!filtered){
        var note = noteArrayListPinned[position]
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            vibarator_manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibarator_manager.defaultVibrator
        } else{
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE)
        }
        bottomSheetDialog = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_delete_pin)
        var share_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.share_note)!!
        share_btn.setOnClickListener {
            var gson = Gson()
            var note_gson = gson.toJson(note)
            createData(note_gson, note.title)
        }
        var delete_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.delete_note)!!
        delete_btn.setOnClickListener {
            var note_id = note.note_id!!
            noteArrayListPinned.removeAt(position)
            noteArrayListPinned.sortByDescending {
                it.timestamp
            }
            notedisplayadapterpinned.notifyItemRemoved(position)
            bottomSheetDialog.dismiss()
            setupTvNote()
            Toast.makeText(this, "Note Deleted", Toast.LENGTH_LONG).show()
            firebaseFirestore.collection("Notes")
                .document(auth.uid!!)
                .collection("Mynotes")
                .document(note_id)
                .delete()
                .addOnSuccessListener {
                    setRecyclerView()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Note Unable Delete", Toast.LENGTH_LONG).show()
                }
        }

        var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
        var pin_text: TextView = bottomSheetDialog.findViewById(R.id.tv_pin)!!
            pin_text.text = "Unpin Note"
        pin_note.setOnClickListener {
            var time_stamp = Timestamp.now()
            noteArrayListUnpinned.add(noteArrayListPinned[position])
            noteArrayListPinned.removeAt(position)
            noteArrayListPinned.sortByDescending {
                it.timestamp
            }
            noteArrayListUnpinned.sortByDescending {
                it.timestamp
            }
            notedisplayadapterpinned.notifyItemRemoved(position)
            notedisplayadapterunpinned.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
            setupTvNote()

            Toast.makeText(this, "Unpinned",Toast.LENGTH_LONG).show()
            firebaseFirestore.collection("Notes")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("Mynotes")
                .document(note.note_id!!)
                .update("pinned_note", "Unpinned", "timestamp2", time_stamp)
                .addOnSuccessListener {
                    setRecyclerView()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Unable to Pin Note",Toast.LENGTH_LONG).show()

                }
        }
        bottomSheetDialog.show()
        }else{
            var note = filteredLIst_pinned[position]

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                vibarator_manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibarator_manager.defaultVibrator
            } else{
                @Suppress("DEPRECATION")
                getSystemService(VIBRATOR_SERVICE)
            }

            bottomSheetDialog = BottomSheetDialog(this, R.style.MyTransparentBottomSheetDialogTheme)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_delete_pin)
            var share_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.share_note)!!
            share_btn.setOnClickListener {
                var gson = Gson()
                var note_gson = gson.toJson(note)
                createData(note_gson, note.title)
            }
            var delete_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.delete_note)!!
            delete_btn.setOnClickListener {
                var note_id = note.note_id!!
                filteredLIst_pinned.removeAt(position)
                filteredLIst_pinned.sortByDescending {
                    it.timestamp
                }
                notedisplayadapterpinned.notifyItemRemoved(position)
                bottomSheetDialog.dismiss()
                setupTvNote()

                Toast.makeText(this, "Note Deleted", Toast.LENGTH_LONG).show()
                firebaseFirestore.collection("Notes")
                    .document(auth.uid!!)
                    .collection("Mynotes")
                    .document(note_id)
                    .delete()
                    .addOnSuccessListener {
                        setRecyclerView()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Note Unable Delete", Toast.LENGTH_LONG).show()
                    }
            }

            var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
            var pin_text: TextView = bottomSheetDialog.findViewById(R.id.tv_pin)!!
            pin_text.text = "Unpin Note"
            pin_note.setOnClickListener {
                var time_stamp = Timestamp.now()
                filteredLIst_unpin.add(filteredLIst_pinned[position])
                filteredLIst_pinned.removeAt(position)
                filteredLIst_pinned.sortByDescending {
                    it.timestamp
                }
                filteredLIst_unpin.sortByDescending {
                    it.timestamp
                }
                notedisplayadapterpinned.notifyItemRemoved(position)
                notedisplayadapterunpinned.notifyDataSetChanged()
                bottomSheetDialog.dismiss()
                setupTvNote()

                Toast.makeText(this, "Unpinned",Toast.LENGTH_LONG).show()
                firebaseFirestore.collection("Notes")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .collection("Mynotes")
                    .document(note.note_id!!)
                    .update("pinned_note", "Unpinned", "timestamp2", time_stamp)
                    .addOnSuccessListener {
                        setRecyclerView()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Unable to Pin Note",Toast.LENGTH_LONG).show()
                    }
            }
            bottomSheetDialog.show()
        }
    }

    private fun createData(noteGson: String?, title: String?) {
        try {
            val values = ContentValues()
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                title+" - "+ Timestamp.now().seconds
            )
            values.put(
                MediaStore.MediaColumns.MIME_TYPE,
                "text/plain"
            )
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/NoteIt/"
            )
            val uri: Uri? = contentResolver.insert(
                MediaStore.Files.getContentUri("external"),
                values
            )
            val outputStream: OutputStream? = contentResolver.openOutputStream(uri!!)
            outputStream!!.write(noteGson!!.toByteArray())
            outputStream.close()
            var share = Intent(Intent.ACTION_SEND)
            share.putExtra(Intent.EXTRA_STREAM, uri)
            share.type = "text/plain"
            startActivity(share)
        } catch (e: IOException) {
            Toast.makeText(this, "Fail to create file", Toast.LENGTH_SHORT).show()
        }
    }

}


