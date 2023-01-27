package com.collide.noteit.SignUp

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.collide.noteit.R
import com.collide.noteit.dataClass.User_Profile_Detail
import com.collide.noteit.databinding.ActivitySignupBinding
import com.collide.noteit.login.LoginActivity
import com.collide.noteit.tools.loadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private var _binding: ActivitySignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var loadingDialog = loadingDialog(this)

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
                binding.txtEmail.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red_100)))
                binding.txtEmail.setEndIconDrawable(R.drawable.ic_error)
            }

        }

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
                                userCreation(auth.currentUser!!, "EMAIL |", name)
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
    private fun userCreation(user: FirebaseUser, provider: String, name: String) {
        val random = (1..8).random()
        val userData = User_Profile_Detail(user.uid, name, user.email, "User_Icon/av${random}"+".png", provider)
        Log.d("User", ""+userData)
        database.child("users").child(user.uid).setValue(userData)
    }

}