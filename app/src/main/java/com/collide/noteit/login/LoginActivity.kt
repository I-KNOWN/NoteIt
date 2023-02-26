package com.collide.noteit.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import com.collide.noteit.BuildConfig
import com.collide.noteit.MainActivity
import com.collide.noteit.R
import com.collide.noteit.SignUp.Avatar_Activity
import com.collide.noteit.SignUp.SignupActivity
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivityLoginBinding
import com.collide.noteit.tools.loadingDialog
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.permissionx.guolindev.PermissionX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    //Binding
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    //Google Login
    private lateinit var googleSignInClient: GoogleSignInClient
    //Facebook Login
    private var callbackManager = CallbackManager.Factory.create()

    //Realtime Database
    private lateinit var database: DatabaseReference

    // loading
    private var loadingDialog = loadingDialog(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        FirebaseApp.initializeApp(this)

        setContentView(binding.root)
        auth = Firebase.auth
        database = Firebase.database.reference

        checkAndRequestPerm()

        binding.tvForgotPassword.setOnClickListener {
            if(emailValidation(binding.txtEmailEdit.text.toString())){
                auth.sendPasswordResetEmail(binding.txtEmailEdit.text.toString())
                Toast.makeText(this, "Password Rest has been sent the the Email", Toast.LENGTH_SHORT).show()
            } else{
                binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_100)))
                binding.txtEmail.setEndIconDrawable(R.drawable.ic_error)
            }
        }

        binding.loginBtn.setOnClickListener {
            loadingDialog.startloading()
            val email = binding.txtEmailEdit.text.toString()
            val password = binding.txtPasswordEdit.text.toString()
            if(email != "" && password != ""){
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            if(auth.currentUser!!.isEmailVerified){
                                loadingDialog.isDismis()
                                updateUI_Google(auth.currentUser)
                            }else{
                                loadingDialog.isDismis()
                                Toast.makeText(this, "Email is not yet verified", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            loadingDialog.isDismis()

                            Toast.makeText(this, "Check Email and password", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else{
                loadingDialog.isDismis()

                Toast.makeText(this, "Fill Both Email and Password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signUpEmail.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            val pair1 = androidx.core.util.Pair<View, String>(binding.logoView, "logo_big")
            val pair2 = androidx.core.util.Pair<View, String>(binding.loginGoogleBtn, "google_login_eq")
            val pair3 = androidx.core.util.Pair<View, String>(binding.loginFacebookBtn, "facebook_login_eq")
            val pair4 = androidx.core.util.Pair<View, String>(binding.textView3, "cred_text1")
            val pair5 = androidx.core.util.Pair<View, String>(binding.signUpEmail, "cred_text2")
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1,pair2,pair3 ).toBundle()
            startActivity(intent, option)
        }

        binding.txtEmailEdit.doAfterTextChanged {
            if(emailValidation(it.toString())){
                binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_100)))
                binding.txtEmail.setEndIconDrawable(R.drawable.ic_check)
            } else{
                binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_100)))
                binding.txtEmail.setEndIconDrawable(R.drawable.ic_error)
            }
        }

        binding.loginFacebookBtn.setOnClickListener {
            if(auth.currentUser != null){

                updateUI_Facebook(auth.currentUser)
            }
            else{
                loadingDialog.startloading()
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            }
        }

        //Facebook Login
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("User", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
                loadingDialog.isDismis()
                Log.d("User", "facebook:onCancel")
            }
            override fun onError(error: FacebookException) {
                loadingDialog.isDismis()
                Log.d("User", "facebook:onError", error)
            }
        })

        //Google Login
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.WEB_CLIENT_ID))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.loginGoogleBtn.setOnClickListener {
            loadingDialog.startloading()
            signIn()
        }
    }

    private fun checkAndRequestPerm() {
        PermissionX.init(this)
            .permissions(Manifest.permission.CAMERA)
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show()
                }
            }    }

    private fun emailValidation(charseq: String): Boolean {
        return charseq != "" && Patterns.EMAIL_ADDRESS.matcher(charseq).matches()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("User", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("User", "signInWithCredential:success")
                    val user = auth.currentUser!!
                    val firebaseUser = auth.currentUser
                    database.child("users")
                        .get()
                        .addOnSuccessListener {
                            if(!it.hasChild(firebaseUser!!.uid)){
                                Log.d("User", "User Created")
                                userCreation(auth.currentUser!!, "FACEBOOK |")
                                loadingDialog.isDismis()
                                updateUI_avatar()
                            }
                        }
                    loadingDialog.isDismis()
                    updateUI_Facebook(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("User", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "You have previously logged in with another service provider.",
                        Toast.LENGTH_SHORT).show()
                    loadingDialog.isDismis()
                    updateUI_Facebook(null)
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI_Google(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        getResult.launch(signInIntent)
    }

    private val getResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d("User", "FirebaseAuthentication" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    loadingDialog.isDismis()
                    Log.d("User", "Google Failed to SignIn", e)
                }

            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            database.child("users")
                .get()
                .addOnSuccessListener {
                    if(!it.hasChild(firebaseUser!!.uid)){
                        Log.d("User", "User Created")
                        userCreation(auth.user!!,"GOOGLE |")
                        loadingDialog.isDismis()
                        updateUI_avatar()
                    } else{
                        loadingDialog.isDismis()
                        updateUI_Google(firebaseUser)
                    }
                }


        }
    }

    private fun userCreation(user: FirebaseUser, provider: String) {
        val random = (1..8).random()
        var currentdate = Calendar.getInstance().time
        var DateFormat = SimpleDateFormat("EEE, MMM dd, ''yyyy", Locale.getDefault())
        var formatedDate = DateFormat.format(currentdate)
        val user_data = User_Profile_Detail(user.uid, user.displayName, user.email, "User_Icon/av${random}"+".png", provider, formatedDate)
        database.child("users").child(user.uid).setValue(user_data)
    }

    private fun updateUI_Google(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)
        }
    }

    private fun updateUI_Facebook(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null && !AccessToken.getCurrentAccessToken()!!.isExpired) {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }

    private fun updateUI_avatar(){
        val intent = Intent(this, Avatar_Activity::class.java)
        intent.putExtra("loc","Login")
        finish()
        val pair1 = androidx.core.util.Pair<View, String>(binding.logoView, "logo_big")
        val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1 ).toBundle()
        startActivity(intent, option)
    }

}