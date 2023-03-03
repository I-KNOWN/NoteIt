package com.collide.noteit.SignUp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.collide.noteit.MainActivity
import com.collide.noteit.R
import com.collide.noteit.dataClass.Avatar_Selection_Model
import com.collide.noteit.databinding.ActivityAvatarBinding
import com.collide.noteit.recyclerAdapter.AvatarSelectionAdapter
import com.collide.noteit.tools.ProfileActivity
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



        setupadapter()

//        databaseStorage.reference.child("User_Icon/").listAll()
//            .addOnSuccessListener {
//                for(item in it.items){
//                    item.downloadUrl.addOnSuccessListener {
//                        var urlstr = it.toString()
//                        dataList.add(Avatar_Selection_Model(item.name, urlstr ))
//                        Log.d("usericon", ""+dataList)
//                        avatarSelectionAdapter.setDataList(dataList)
//                    }
//                }
//            }

        avatarSelectionAdapter.setOnItemClickListener(object: AvatarSelectionAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                avatarSelectionAdapter.setIndexPosition(position)
            }

        })

        Log.d("Avatar_Activity_testing", ""+intent.getStringExtra("Loc").equals("Login"))

        if(intent.getStringExtra("loc").equals("Login")){
            binding.textView2.text = "Login"
            binding.avatarSelectorBtn.setOnClickListener {

                Log.d("User", ""+avatarSelectionAdapter.index_position)
                val profile_image_name = "User_Icon/"+dataList[avatarSelectionAdapter.index_position].icon_name

                databaseReference.child("users").child(auth.currentUser!!.uid).child("profile_image").setValue(profile_image_name)
                    .addOnSuccessListener {
                        var intent = Intent(this, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                    }


            }
        } else{
            binding.textView2.text = "Save"
            var pos = intent.getIntExtra("icon_pos", 1) - 1
            avatarSelectionAdapter.setIndexPosition(pos)

            binding.avatarSelectorBtn.setOnClickListener {

                Log.d("User", ""+avatarSelectionAdapter.index_position)
                val profile_image_name = "User_Icon/"+dataList[avatarSelectionAdapter.index_position].icon_name

                databaseReference.child("users").child(auth.currentUser!!.uid).child("profile_image").setValue(profile_image_name)
                    .addOnSuccessListener {

                    }
                var intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("change","${dataList[avatarSelectionAdapter.index_position].icon_name}")
                intent.putExtra("index_pos","${avatarSelectionAdapter.getIndexPosition() + 1}" )
                finish()
                startActivity(intent)

            }
        }





    }

    private fun setupadapter() {
        dataList.add(Avatar_Selection_Model("av1.png", R.drawable.av1))
        dataList.add(Avatar_Selection_Model("av2.png", R.drawable.av2))
        dataList.add(Avatar_Selection_Model("av3.png", R.drawable.av3))
        dataList.add(Avatar_Selection_Model("av4.png", R.drawable.av4))
        dataList.add(Avatar_Selection_Model("av5.png", R.drawable.av5))
        dataList.add(Avatar_Selection_Model("av6.png", R.drawable.av6))
        dataList.add(Avatar_Selection_Model("av7.png", R.drawable.av7))
        dataList.add(Avatar_Selection_Model("av8.png", R.drawable.av8))
        avatarSelectionAdapter.setDataList(dataList)
    }
}