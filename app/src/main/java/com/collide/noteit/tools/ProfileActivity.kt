package com.collide.noteit.tools

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.collide.noteit.R
import com.collide.noteit.SignUp.Avatar_Activity
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        var photourl = ""
        firebaseDatabase = Firebase.database.reference
        Log.d("User",""+auth.currentUser!!.uid)
        firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("profile_image")
            .get()
            .addOnSuccessListener {
                photourl = it.value.toString()

                Log.d("User",""+photourl)

                firebaseReference = firebaseReference.child(photourl)
                Log.d("User",""+firebaseReference)
                firebaseReference.downloadUrl.addOnSuccessListener {
                    Picasso.get().load(it).into(binding.iview)

                }.addOnFailureListener {
                    Log.d("FireStore","Fail to Get Data" + it.message)
                }

            }
//

//        firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("profile_image")
//            .get()
//            .addOnSuccessListener {
//
//                photourl = it.value.toString()
//
//                Log.d("User", ""+it.value)
//            }




        binding.openAvatar.setOnClickListener {
            var intent = Intent(this, Avatar_Activity::class.java)
            startActivity(intent)
        }

        getLoginProvider()

//        var uri = Uri.parse("android.resource://$packageName/${R.drawable.google_logo}")
//        firebaseReference = firebaseReference.child("Users_Profile/icon_User_basic")
//        firebaseReference.putFile(uri).addOnCompleteListener {
//            Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener {
//            Toast.makeText(this,"fail", Toast.LENGTH_SHORT).show()
//        }

        binding.logoutBtn.setOnClickListener {
            var intent = Intent(this, LoginActivity::class.java)
            finish()
            auth.signOut()
            startActivity(intent)
        }



        binding.LinkGoogle.setOnClickListener {

            if(!provider.contains("GOOGLE")){
                linkingGoogle()
            }

        }

        binding.LinkFB.setOnClickListener {

            if(!provider.contains("FACEBOOK")){
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



    }
    private fun getLoginProvider() {
        firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("provider")
            .get()
            .addOnSuccessListener {
                provider = it.value.toString()

                if(provider.contains("GOOGLE")){
                    binding.LinkGoogle.text = "Google Linked"
                }

                if(provider.contains("FACEBOOK")){
                    binding.LinkFB.text = "Facebook Linked"
                }
            }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }


    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.currentUser!!.linkWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "linkWithCredential:success")
                    val user = task.result?.user

                    provider = provider+" FACEBOOK |"
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


    private fun linkingGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient.signInIntent

        getResult.launch(signInIntent)



    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                try {

                    val account = task.getResult(ApiException::class.java)
                    val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                    auth.currentUser!!.linkWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d("User", "linkWithCredential:success")
                                provider = provider+" GOOGLE |"
                                firebaseDatabase.child("users").child(auth.currentUser!!.uid).child("provider")
                                    .setValue(provider)

                                getLoginProvider()


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