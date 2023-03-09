package com.collide.noteit.SignUp

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.collide.noteit.MainActivity
import com.collide.noteit.R
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivitySignupBinding
import com.collide.noteit.login.LoginActivity
import com.collide.noteit.tools.loadingDialog
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class SignupActivity : AppCompatActivity() {

    private var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var loadingDialog = loadingDialog(this)
    private var callbackManager = CallbackManager.Factory.create()
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        database = Firebase.database.reference

        binding.txtEmailEdit.doAfterTextChanged {
            if(emailValidation(it.toString())){
                binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green_100)))
                binding.txtEmail.setEndIconDrawable(R.drawable.ic_check)
            }
            else{
                when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_note)))
                        binding.txtEmail.setEndIconDrawable(R.drawable.ic_error)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_100)))
                        binding.txtEmail.setEndIconDrawable(R.drawable.ic_error)
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
                }
            }

        }

        binding.loginFacebookBtn.setOnClickListener {

            loadingDialog.startloading()
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))

        }
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("User_login_fab", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }
            override fun onCancel() {
                loadingDialog.isDismis()
                Log.d("User_login_fab", "facebook:onCancel")
            }
            override fun onError(error: FacebookException) {
                loadingDialog.isDismis()
                Log.d("User_login_fab", "facebook:onError", error)
            }
        })
        binding.tvTermsConditionLink.setOnClickListener {
            val uri = Uri.parse("https://www.freeprivacypolicy.com/live/9282435d-917d-4a95-af64-c3b4058541f0")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }

        binding.logInBtn.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)
            val pair1 = androidx.core.util.Pair<View, String>(binding.logoView, "logo_big")
            val pair2 = androidx.core.util.Pair<View, String>(binding.loginGoogleBtn, "google_login_eq")
            val pair3 = androidx.core.util.Pair<View, String>(binding.loginFacebookBtn, "facebook_login_eq")
//            val pair4 = androidx.core.util.Pair<View, String>(binding.textView3, "cred_text1")
//            val pair5 = androidx.core.util.Pair<View, String>(binding.signUpEmail, "cred_text2")
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pair1,pair2,pair3 ).toBundle()
            startActivity(intent, option)

//            onBackPressedDispatcher.onBackPressed()

        }

        binding.signupBtn.setOnClickListener {
            loadingDialog.startloading()
            if(binding.chkBoxTermConditionEnabler.isChecked){
                var name = binding.txtNameEdit.text.toString()
                var email = binding.txtEmailEdit.text.toString()
                var password = binding.txtConfirmPasswordEdit.text.toString()
                if(name != "" && emailValidation(email) && password != ""){
                    emailSignUp(name, email, password)
                }else{
                    loadingDialog.isDismis()
                    Toast.makeText(this, "Fill all the inputs", Toast.LENGTH_LONG).show()
                }
            }else{
                loadingDialog.isDismis()

                Toast.makeText(this, "You have to check the Terms and Conditions", Toast.LENGTH_LONG).show()
            }
        }
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

                    } else{
                        loadingDialog.isDismis()
                        updateUI_Google(firebaseUser)
                    }
                }


        }
    }
    private fun updateUI_Google(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("User_login_fab", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("User_login_fab", "signInWithCredential:success")
                    val user = auth.currentUser!!
                    val firebaseUser = auth.currentUser
                    database.child("users")
                        .get()
                        .addOnSuccessListener {
                            if(!it.hasChild(firebaseUser!!.uid)){
                                Log.d("User_login_fab", "User Created")
                                userCreation(auth.currentUser!!, "FACEBOOK |")
                                loadingDialog.isDismis()
                                updateUI_avatar()
                            }else{
                                loadingDialog.isDismis()
                                updateUI_Facebook(user)
                            }
                        }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("User_login_fab", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "You have previously logged in with another service provider.",
                        Toast.LENGTH_SHORT).show()
                    loadingDialog.isDismis()
                    updateUI_Facebook(null)
                }
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

    private fun emailSignUp(name: String,email: String,password: String ) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){task ->
                if (task.isSuccessful){
                    val firebaseUser = auth.currentUser
                    database.child("users")
                        .get()
                        .addOnSuccessListener {
                            if(!it.hasChild(firebaseUser!!.uid)){
                                Log.d("User", "User Created")
                                userCreation(auth.currentUser!!, "EMAIL |")
                                auth.currentUser!!.sendEmailVerification()
                                loadingDialog.isDismis()
                                Toast.makeText(this, "Email Verification Sent!!!", Toast.LENGTH_SHORT).show()
                            }
                        }.addOnFailureListener {
                            loadingDialog.isDismis()
                            Toast.makeText(this, "User Failed to create", Toast.LENGTH_LONG).show()
                        }
                } else{
                    loadingDialog.isDismis()
                    Toast.makeText(this, "User Failed to create", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun emailValidation(charseq: String): Boolean {
        return charseq != "" && Patterns.EMAIL_ADDRESS.matcher(charseq).matches()
    }
    private fun userCreation(user: FirebaseUser, provider: String) {
        val random = (1..8).random()
        var currentdate = Calendar.getInstance().time
        var DateFormat = SimpleDateFormat("EEE, MMM dd, ''yyyy", Locale.getDefault())
        var formatedDate = DateFormat.format(currentdate)
        val user_data = User_Profile_Detail(user.uid, user.displayName, user.email, "User_Icon/av${random}"+".png", provider, formatedDate)
        database.child("users").child(user.uid).setValue(user_data).addOnCompleteListener {
            loadingDialog.isDismis()
            updateUI_avatar()
        }
    }

}