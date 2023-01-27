package com.collide.noteit.SignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collide.noteit.MainActivity
import com.collide.noteit.dataClass.Avatar_Selection_Model
import com.collide.noteit.databinding.ActivityAvatarBinding
import com.collide.noteit.recyclerAdapter.AvatarSelectionAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class Avatar_Activity : AppCompatActivity() {

    private var _binding: ActivityAvatarBinding? = null
    val binding get() = _binding!!

    private lateinit var avatarSelectionAdapter: AvatarSelectionAdapter
    private var dataList = mutableListOf<Avatar_Selection_Model>()


    // Firebase Realtime
    private lateinit var databaseReference: DatabaseReference

    // Firebase Storage
    private lateinit var databaseStorage: FirebaseStorage

    // Firebase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAvatarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        databaseStorage = FirebaseStorage.getInstance()
        databaseReference = Firebase.database.reference

        avatarSelectionAdapter = AvatarSelectionAdapter(applicationContext)
        binding.avatarView.layoutManager = GridLayoutManager(applicationContext, 2)
        binding.avatarView.adapter = avatarSelectionAdapter

        databaseStorage.reference.child("User_Icon/").listAll()
            .addOnSuccessListener {
                for(item in it.items){
                    item.downloadUrl.addOnSuccessListener {
                        var urlstr = it.toString()
                        dataList.add(Avatar_Selection_Model(item.name, urlstr ))
                        Log.d("usericon", ""+dataList)
                        avatarSelectionAdapter.setDataList(dataList)
                    }
                }
            }

        avatarSelectionAdapter.setOnItemClickListener(object: AvatarSelectionAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                avatarSelectionAdapter.setIndexPosition(position)
            }

        })

        binding.avatarSelectorBtn.setOnClickListener {

            Log.d("User", ""+avatarSelectionAdapter.index_position)
            val profile_image_name = "User_Icon/"+dataList[avatarSelectionAdapter.index_position].icon_name

            databaseReference.child("users").child(auth.currentUser!!.uid).child("profile_image").setValue(profile_image_name)
            var intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)

        }



    }
}