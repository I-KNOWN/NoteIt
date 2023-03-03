package com.collide.noteit

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibratorManager
import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView.OnQueryTextListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.SignUp.Avatar_Activity
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivityMainBinding
import com.collide.noteit.login.LoginActivity
import com.collide.noteit.recyclerAdapter.NoteDisplayAdapter
import com.collide.noteit.recyclerAdapter.NoteViewDispalyAdapter
import com.collide.noteit.tools.ProfileActivity
//import com.facebook.AccessToken
//import com.facebook.CallbackManager
//import com.facebook.FacebookCallback
//import com.facebook.FacebookException
//import com.facebook.login.LoginManager
//import com.facebook.login.LoginResult
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firestore.v1.AggregationResult
import com.google.gson.Gson
import com.squareup.picasso.Picasso
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val scoresRef = Firebase.database.getReference("users")
//        scoresRef.keepSynced(true)









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
//        setDataList(notedisplayadapter)

//        setupRecyclerView()
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

//        query_ff.addSnapshotListener(MetadataChanges.INCLUDE){ querySnapshot, e->
//            if(e != null){
//                return@addSnapshotListener
//            }
//            for(change in querySnapshot!!.documentChanges){
//                if(change.type == DocumentChange.Type.ADDED){
//                    Toast.makeText(this,"here: "+change.document.data.size, Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

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

        binding.addNoteBtn.setOnClickListener {
            val intent = Intent(this, Note_Activity::class.java)
            startActivity(intent)
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
            filtericonitem("red")
            filterbottomSheetDialog.dismiss()

            iconfilter_bool = true
        }

        var btn_blue = filterbottomSheetDialog.findViewById<ImageView>(R.id.blue_btn)
        btn_blue?.setOnClickListener {
            filtericonitem("blue")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_cyan = filterbottomSheetDialog.findViewById<ImageView>(R.id.cyan_btn)
        btn_cyan?.setOnClickListener {
            filtericonitem("cyan")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_dblue = filterbottomSheetDialog.findViewById<ImageView>(R.id.dblue_btn)
        btn_dblue?.setOnClickListener {
            filtericonitem("dblue")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_green = filterbottomSheetDialog.findViewById<ImageView>(R.id.green_btn)
        btn_green?.setOnClickListener {
            filtericonitem("green")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_orange = filterbottomSheetDialog.findViewById<ImageView>(R.id.orange_btn)
        btn_orange?.setOnClickListener {
            filtericonitem("orange")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_pink = filterbottomSheetDialog.findViewById<ImageView>(R.id.pink_btn)
        btn_pink?.setOnClickListener {
            filtericonitem("pink")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }
        var btn_purple = filterbottomSheetDialog.findViewById<ImageView>(R.id.purple_btn)
        btn_purple?.setOnClickListener {
            filtericonitem("purple")
            filterbottomSheetDialog.dismiss()
            iconfilter_bool = true
        }

        filterbottomSheetDialog.show()

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


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                x1 = event.x
                y1 = event.y

            }
            MotionEvent.ACTION_UP ->{
                x2 = event.x
                y2 = event.y
                if(x1 < x2){
                    var intent = Intent(this, CameraActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        return super.onTouchEvent(event)

    }

    private fun filterdListUnpinned(newText: String?) {
        current_query_string = newText!!
        filteredLIst_unpin.clear()
        for(item in noteArrayListUnpinned){
            if(item.title!!.lowercase().contains(newText!!.lowercase())){
                filteredLIst_unpin.add(item)
            }
        }
            notedisplayadapterunpinned.setFilteredList(filteredLIst_unpin)
        filtered = true
        if (newText!!.isEmpty()){
            current_query_string = ""
            filtered = false
            notedisplayadapterunpinned.setFilteredList(noteArrayListUnpinned)
        }
    }

    private fun filterdListPinned(newText: String?) {
        current_query_string = newText!!
        filteredLIst_pinned.clear()
        for(item in noteArrayListPinned){
            if(item.title!!.lowercase().contains(newText!!.lowercase())){
                filteredLIst_pinned.add(item)
            }
        }
        notedisplayadapterpinned.setFilteredList(filteredLIst_pinned)
            filtered = true
        if (newText!!.isEmpty()){
            current_query_string = ""
            filtered = false
            notedisplayadapterpinned.setFilteredList(noteArrayListPinned)
        }
    }
    private fun setupTvNote() {
        if(noteArrayListPinned.size == 0){
            binding.tvUnpin.visibility = ViewGroup.GONE
            binding.noteText.setText("Notes")
        } else if(noteArrayListPinned.size > 0){
            binding.noteText.setText("Pinned")

            if(noteArrayListUnpinned.size == 0){
                return
            }else{
                binding.tvUnpin.visibility = ViewGroup.VISIBLE
            }

        }
    }

    private fun setRecyclerView() {
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
            var DateFormat = SimpleDateFormat("EEE, MMM dd, ''yyyy", Locale.getDefault())
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
        }.addOnFailureListener {
            Log.d("it_exp",""+it.message)
        }
    }



//    private fun setupRecyclerView() {
//        var query: Query = firebaseFirestore.collection("Notes")
//            .document(FirebaseAuth.getInstance().currentUser!!.uid)
//            .collection("Mynotes").orderBy("title", Query.Direction.DESCENDING).whereEqualTo("pinned_note", "Unpinned")
//        var optino:FirestoreRecyclerOptions<Note_Data_Model> = FirestoreRecyclerOptions.Builder<Note_Data_Model>()
//            .setQuery(query, Note_Data_Model::class.java)
//            .build()
//
//
//        notedisplayadapter = NoteDisplayAdapter(optino, this)
//        binding.recyclerViewUnpinned.layoutManager = GridLayoutManager(this, 2)
//        binding.recyclerViewUnpinned.adapter = notedisplayadapter
//        notedisplayadapter.notifyDataSetChanged()
//    }

    //    private fun setDataList(notedisplayadapter: NoteDisplayAdapter) {
//        var data_list = mutableListOf<Note_Data_Model>()
//
//
//        notedisplayadapter.setDataModel(data_list)
//    }

    private fun setProfileIcon() {

        if(loadProfileLocalData()){
            firebaseDatabase.child(auth.currentUser!!.uid).child("profile_image")
                .get()
                .addOnSuccessListener {
                    profile_Icon_value = it.value.toString().split("/")[1]

                    Log.d("User",""+profile_Icon_value)

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


//                    firebaseReference = firebaseReference.child(photourl)
//                    Log.d("User",""+firebaseReference)
//                    firebaseReference.downloadUrl.addOnSuccessListener {
//
//                        Glide.with(this)
//                            .load(it)
//                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
//                            .into(binding.profileIcon)
//
//                        saveProfile(it.toString())
//
//                    }.addOnFailureListener {
//                        Log.d("FireStore","Fail to Get Data" + it.message)
//                    }

                }
        }


    }

    private fun saveProfile(remote: String) {
        var sharedPreferences = getSharedPreferences(SHARED_PREFERS, MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.putString(URI_, remote)
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
            note = noteArrayListUnpinned[position]
        }else{
            note = noteArrayListPinned[position]
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

                }
                .addOnFailureListener {
                    Toast.makeText(this, "Note Unable Delete", Toast.LENGTH_LONG).show()
                }
        }

        var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
        var pin_text: TextView = bottomSheetDialog.findViewById(R.id.tv_pin)!!
        pin_text.setText("Pin Note")
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
            pin_text.setText("Pin Note")
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

                }
                .addOnFailureListener {
                    Toast.makeText(this, "Note Unable Delete", Toast.LENGTH_LONG).show()
                }
        }

        var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
        var pin_text: TextView = bottomSheetDialog.findViewById(R.id.tv_pin)!!
        pin_text.setText("Unpin Note")
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
            pin_text.setText("Unpin Note")
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

}


