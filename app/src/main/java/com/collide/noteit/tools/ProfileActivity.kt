package com.collide.noteit.tools

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.collide.noteit.R
import com.collide.noteit.SignUp.Avatar_Activity
import com.collide.noteit.dataClass.Note_Data_Model
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivityProfileBinding
import com.collide.noteit.databinding.ActivitySignupBinding
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
import com.squareup.picasso.Picasso

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
        lateinit var user_data: User_Profile_Detail
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firebaseDatabase = Firebase.database.reference
        // Getting Intent Data
        if(intent.getStringExtra("note_count").toString() != ""){
            var gson = Gson()
            var user_data_string = intent.getStringExtra("user_data")
            if(gson.fromJson(user_data_string, User_Profile_Detail::class.java) != null){
                count_note = intent.getStringExtra("note_count").toString()
                user_data = gson.fromJson(user_data_string, User_Profile_Detail::class.java)
            }

        }
        Log.d("Snapshot",""+ count_note)

        binding.tvJoinedDate.text = "Joined ${user_data.joined_date}"

        if(count_note.length == 1){
            binding.noteCount.setText("00${count_note}")
        } else if (count_note.length == 2){
            binding.noteCount.setText("0${count_note}")
        } else{
            binding.noteCount.text = count_note
        }

        binding.nameTvAc.setText(user_data.name)
        binding.emailTvAc.setText(user_data.email)
//        binding.tvJoinedDate.setText()

//        var photourl = ""
        firebaseReference = user_data.profile_image?.let { firebaseReference.child(it) }!!
        Log.d("User",""+firebaseReference)
        firebaseReference.downloadUrl.addOnSuccessListener {
            Picasso.get().load(it).into(binding.avatarIcon)

        }.addOnFailureListener {
            Log.d("FireStore","Fail to Get Data" + it.message)
        }
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
        binding.avatarSelection.setOnClickListener {
            var intent = Intent(this, Avatar_Activity::class.java)
            intent.putExtra("loc","Profile")
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

    private fun UpdateUI() {
        var intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    private fun getLoginProvider() {
        Log.d("testing_linking","Inside method LoginProvider")
        Log.d("testing_linking",""+ user_data.provider)
        if(user_data.provider!!.contains("GOOGLE")){
            binding.googleTxt.text = "Linked"
            binding.googleLinkBtn.alpha = 0.5f
            binding.googleLinkBtn.setOnClickListener(null)
        }
        if(user_data.provider!!.contains("FACEBOOK")){
            binding.facebookTxt.text = "Linked"
            binding.facebookLinkBtn.alpha = 0.5f
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
                        .setValue(provider)

                    getLoginProvider()

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