package com.collide.noteit.tools

import android.content.Context
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class Utility{

    fun fireStoreReference() : CollectionReference{
        var currentUser = FirebaseAuth.getInstance().currentUser
        return FirebaseFirestore.getInstance().collection("Notes")
            .document(currentUser!!.uid).collection("my_notes")
    }


}