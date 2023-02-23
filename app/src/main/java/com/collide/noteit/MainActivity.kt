package com.collide.noteit

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.SignUp.Avatar_Activity
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.databinding.ActivityMainBinding
import com.collide.noteit.login.LoginActivity
import com.collide.noteit.recyclerAdapter.NoteDisplayAdapter
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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private var _binding:ActivityMainBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val SHARED_PREFERS = "remoteUri"
        const val URI_ = "uri"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: DatabaseReference
    private var firebaseReference = Firebase.storage.reference

    var flag = "off"

    private lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var notedisplayadapter: NoteDisplayAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database.reference
        firebaseFirestore = Firebase.firestore


        setProfileIcon()
//        setDataList(notedisplayadapter)

        setupRecyclerView()



        binding.materialCardView.setOnClickListener {
            var intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        binding.recyclerView.addOnScrollListener(object:  RecyclerView.OnScrollListener(){
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

    override fun onStart() {
        super.onStart()
        notedisplayadapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        notedisplayadapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        notedisplayadapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        var query: Query = firebaseFirestore.collection("Notes")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("Mynotes").orderBy("title", Query.Direction.DESCENDING)
        var optino:FirestoreRecyclerOptions<Note_Data_Model> = FirestoreRecyclerOptions.Builder<Note_Data_Model>()
            .setQuery(query, Note_Data_Model::class.java)
            .build()


        notedisplayadapter = NoteDisplayAdapter(optino, this)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = notedisplayadapter
    }

    //    private fun setDataList(notedisplayadapter: NoteDisplayAdapter) {
//        var data_list = mutableListOf<Note_Data_Model>()
//
//
//        notedisplayadapter.setDataModel(data_list)
//    }
    private fun setProfileIcon() {

        if(loadProfileLocalData()){
            firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("profile_image")
                .get()
                .addOnSuccessListener {
                    var photourl = it.value.toString()

                    Log.d("User",""+photourl)

                    firebaseReference = firebaseReference.child(photourl)
                    Log.d("User",""+firebaseReference)
                    firebaseReference.downloadUrl.addOnSuccessListener {

                        Glide.with(this)
                            .load(it)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(binding.profileIcon)

                        saveProfile(it.toString())

                    }.addOnFailureListener {
                        Log.d("FireStore","Fail to Get Data" + it.message)
                    }

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



}


