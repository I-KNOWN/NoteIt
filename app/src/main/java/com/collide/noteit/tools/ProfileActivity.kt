package com.collide.noteit.tools

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.collide.noteit.MainActivity
import com.collide.noteit.Note_Activity
import com.collide.noteit.R
import com.collide.noteit.SignUp.Avatar_Activity
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivityProfileBinding
import com.collide.noteit.login.LoginActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class ProfileActivity : AppCompatActivity() {

    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth


    private lateinit var googleSignInClient: GoogleSignInClient


    private var callbackManager = CallbackManager.Factory.create()
    private var account1: GoogleSignInAccount? = null
    private var firebaseReference = Firebase.storage.reference

    private lateinit var firebaseDatabase: DatabaseReference

    private lateinit var provider: String
    var TAG = "CHK"
    // Note Data From Intent
    companion object{
        var count_note: String = ""
        var count_pinned_note: String = ""
        lateinit var user_data: User_Profile_Detail
        var profile_icon = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w: Window = window
            w.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database.reference
        // Getting Intent Data
        if(intent.getStringExtra("note_count").toString() != ""){
            var gson = Gson()
            var user_data_string = intent.getStringExtra("user_data")
            if(gson.fromJson(user_data_string, User_Profile_Detail::class.java) != null){

                profile_icon = intent.getStringExtra("ProfileURL")!!
                setPreProflie(profile_icon!!)
                count_note = intent.getStringExtra("note_count").toString()
                count_pinned_note = intent.getStringExtra("note_pinned_count").toString()
                user_data = gson.fromJson(user_data_string, User_Profile_Detail::class.java)
            }

        }

        binding.cardView2.setOnClickListener {
            readfile()
        }


        if(intent.getStringExtra("change")!= null){
            var loc = intent.getStringExtra("change")

            setPreProflie(loc!!)

        }

        if(intent.getStringExtra("index_pos") != null){
            var data = intent.getStringExtra("index_pos").toString()
            var data_int = data.toInt()

            var data_string = "av${data_int}.png"


            profile_icon = data_string

        }

//
//        else{
//            firebaseReference = user_data.profile_image?.let { firebaseReference.child(it) }!!
//            Log.d("User",""+firebaseReference)
//            firebaseReference.downloadUrl.addOnSuccessListener {
//                Picasso.get().load(it).into(binding.avatarIcon)
//                Log.d("Profile_Activity_inside","updated: "+ it)
//            }.addOnFailureListener {
//                Log.d("FireStore","Fail to Get Data" + it.message)
//            }
//        }

        Log.d("Snapshot",""+ count_note)

        binding.tvJoinedDate.text = "Joined ${user_data.joined_date}"

        if(count_note.length == 1){
            binding.noteCount.setText("00${count_note}")
        } else if (count_note.length == 2){
            binding.noteCount.setText("0${count_note}")
        } else{
            binding.noteCount.text = count_note
        }

        if(count_pinned_note.length == 1){
            binding.pinnedCount.setText("00${count_pinned_note}")
        } else if (count_pinned_note.length == 2){
            binding.pinnedCount.setText("0${count_pinned_note}")
        } else{
            binding.pinnedCount.text = count_pinned_note
        }

        binding.nameTvAc.setText(user_data.name)
        binding.emailTvAc.setText(user_data.email)
//        binding.tvJoinedDate.setText()

//        var photourl = ""

////
//
////        firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("profile_image")
////            .get()
////            .addOnSuccessListener {
////
////                photourl = it.value.toString()
////
////                Log.d("User", ""+it.value)
////            }
//
//
//
//

        binding.backBtnMain.setOnClickListener{
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.backBtnMainTxt.setOnClickListener {
            var intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.avatarSelection.setOnClickListener {
            var num:Int = profile_icon[2].digitToInt()
            var intent = Intent(this, Avatar_Activity::class.java)
            intent.putExtra("loc","Profile")
            intent.putExtra("icon_pos",num)
            finish()
            startActivity(intent)
        }
//
//
////        var uri = Uri.parse("android.resource://$packageName/${R.drawable.google_logo}")
////        firebaseReference = firebaseReference.child("Users_Profile/icon_User_basic")
////        firebaseReference.putFile(uri).addOnCompleteListener {
////            Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show()
////        }.addOnFailureListener {
////            Toast.makeText(this,"fail", Toast.LENGTH_SHORT).show()
////        }
//
//        binding.logoutBtn.setOnClickListener {
//            var intent = Intent(this, LoginActivity::class.java)
//            finish()
//            auth.signOut()
//            startActivity(intent)
//        }
//
//
//
        binding.googleLinkBtn.setOnClickListener {
            Log.d("testing_linking","Inside method click gL")

            if(!user_data.provider!!.contains("GOOGLE")){
                linkingGoogle()
            }

        }
//
        binding.facebookLinkBtn.setOnClickListener {
            Log.d("testing_linking","Inside method click fb")

            if(!user_data.provider!!.contains("FACEBOOK")){
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            }

        }
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }
            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })


        getLoginProvider()
        binding.SignOut.setOnClickListener{
            auth.signOut()
            UpdateUI()
        }
    }

    private fun readfile() {
        var intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("text/plain")
        galleryLauncher.launch(intent)
    }

    var galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            var filepath =it.data!!.dataString
            var uri = Uri.parse(filepath)
            var reader: BufferedReader?
            val builder = StringBuilder()
            try {
                reader = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)))
                var line: String? = ""
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            var intent = Intent(this, Note_Activity::class.java)
            intent.putExtra("note_data",builder.toString())
            intent.putExtra("intent_main","Profile")
            startActivity(intent)
        }
    }

    private fun setPreProflie(loc: String) {
        when(loc){
            "av1.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av1))
            }
            "av2.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av2))
            }
            "av3.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av3))
            }
            "av4.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av4))
            }
            "av5.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av5))
            }
            "av6.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av6))
            }
            "av7.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av7))
            }
            "av8.png"->{
                binding.avatarIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.av8))
            }

        }
    }

    private fun UpdateUI() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun getLoginProvider() {
        Log.d("testing_linking","Inside method LoginProvider")
        Log.d("testing_linking",""+ user_data.provider)
        if(user_data.provider!!.contains("GOOGLE")){

            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    binding.googleTxt.text = "Linked"
                    binding.googleTxt.setTextColor(resources.getColor(R.color.black_300, null))
                    binding.googleLinkBtn.setBackgroundResource(R.drawable.link_btn_linked)
                    binding.googleIcon.alpha = 0.3f
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    binding.googleTxt.text = "Linked"
                    binding.googleLinkBtn.alpha = 0.3f

                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }
            binding.googleLinkBtn.setOnClickListener(null)


        }
        if(user_data.provider!!.contains("FACEBOOK")){
            Log.d("testing_linking", "Facebook inside ")

            when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    binding.facebookTxt.text = "Linked"
                    binding.facebookTxt.setTextColor(resources.getColor(R.color.black_300, null))
                    binding.facebookLinkBtn.setBackgroundResource(R.drawable.link_btn_linked)
                    binding.fbLogo.alpha = 0.3f
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    binding.facebookTxt.text = "Linked"
                    binding.facebookLinkBtn.alpha = 0.3f

                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }

            binding.facebookLinkBtn.setOnClickListener(null)
        }

    }
//
//
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
//
//
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "linkWithCredential:success")
                    val user = task.result?.user

                    var data = user_data.provider+" FACEBOOK |"
                    firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("provider")
                        .setValue(data)
                    Firebase.database.getReference("users")
                        .child(auth.uid.toString())
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                user_data = snapshot.getValue(User_Profile_Detail::class.java)!!
                                Log.d("testing_linking", "${user_data.provider}")
                                getLoginProvider()

                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })


                } else {
                    Log.w(TAG, "linkWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
//
//
    private fun linkingGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.WEB_CLIENT_ID))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient.signInIntent

        getResult.launch(signInIntent)



    }
//
    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                try {

                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                    Log.d("testing_linking","Cred: "+credential)
                    auth.currentUser!!.linkWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("User", "linkWithCredential:success")
                                var data = user_data.provider+" GOOGLE |"
                                firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("provider")
                                    .setValue(data)
                                Firebase.database.getReference("users")
                                    .child(auth.uid.toString())
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            user_data = snapshot.getValue(User_Profile_Detail::class.java)!!
                                            getLoginProvider()

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })



                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }


                } catch (e: ApiException) {
                    Log.d("TAG", "Google Failed to SignIn", e)
                }

            }
        }
}