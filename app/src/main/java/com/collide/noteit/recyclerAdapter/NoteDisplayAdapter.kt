package com.collide.noteit.recyclerAdapter

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.MainActivity
import com.collide.noteit.Note_Activity
import com.collide.noteit.R
import com.collide.noteit.dataClass.Note_Data_Model
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.firebase.ui.firestore.SnapshotParser
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.core.SnapshotHolder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.local.ReferenceSet
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import okhttp3.internal.cache.DiskLruCache.Snapshot
import org.w3c.dom.Text

class NoteDisplayAdapter(options: FirestoreRecyclerOptions<Note_Data_Model>, var context: Context) :
    FirestoreRecyclerAdapter<Note_Data_Model, NoteDisplayAdapter.viewAdapter>(options) {

    lateinit var bottomSheetDialog: BottomSheetDialog
    var dataList = mutableListOf<Note_Data_Model>()
    val storageRef = FirebaseStorage.getInstance().reference
    val db = Firebase.firestore
    var auth = Firebase.auth
    lateinit var vibarator_manager: VibratorManager
    var firebaseFirestore = Firebase.firestore


    override fun onDataChanged() {
        notifyDataSetChanged()
        super.onDataChanged()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(
        holder: viewAdapter,
        position: Int,
        note: Note_Data_Model
    ) {
            holder.main_box.setOnClickListener {

                var intent = Intent(context, Note_Activity::class.java)
                var gson = Gson()
                var note_gson = gson.toJson(note)
                intent.putExtra("note_data",note_gson)
                context.startActivity(intent)
            }
            holder.main_box.setOnLongClickListener {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    vibarator_manager = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibarator_manager.defaultVibrator
                } else{
                    @Suppress("DEPRECATION")
                    context.getSystemService(VIBRATOR_SERVICE)
                }


                bottomSheetDialog = BottomSheetDialog(context, R.style.MyTransparentBottomSheetDialogTheme)
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_delete_pin)

                var delete_btn: LinearLayout = bottomSheetDialog.findViewById(R.id.delete_note)!!
                delete_btn.setOnClickListener {
                    var note_id = note.note_id!!
                    db.collection("Notes")
                        .document(auth.uid!!)
                        .collection("Mynotes")
                        .document(note_id)
                        .delete()
                        .addOnSuccessListener {
                            notifyDataSetChanged()
                            bottomSheetDialog.dismiss()
                            Toast.makeText(context, "Note Deleted", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Note Unable Delete", Toast.LENGTH_LONG).show()
                        }

                }
                var pin_note: LinearLayout = bottomSheetDialog.findViewById(R.id.pin_note)!!
                pin_note.setOnClickListener {
                    firebaseFirestore.collection("Notes")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .collection("Mynotes")
                        .document(note.note_id!!)
                        .update("pinned_note", "Pinned")
                        .addOnSuccessListener {

                            notifyItemRemoved(position)
                            bottomSheetDialog.dismiss()
                            Toast.makeText(context, "Pinned",Toast.LENGTH_LONG).show()

                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Unable to Pin Note",Toast.LENGTH_LONG).show()

                        }
                }

                bottomSheetDialog.show()
                return@setOnLongClickListener true
            }

            holder.title.text = note.title

            var data_note_et = note.edit_text_data_all!!.split("|&@!~~~|")

            holder.des.text = Html.fromHtml(data_note_et[0],   Html.FROM_HTML_MODE_COMPACT)
            Log.d("Data-note","desc: "+ data_note_et[0])
            Log.d("image_url",""+holder.des)
            Log.d("Data-note","color: "+note.note_color)
            when(note.note_color){
                "blue" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_blue)
                }
                "red" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_red)

                }
                "cyan" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_cyan)

                }
                "dblue" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_dblue)

                }
                "green" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_green)

                }
                "orange" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_orange)

                }
                "pink" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_pink)

                }
                "purple" ->{
                    holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_purple)

                }
            }

            if(note.image_URL != ""){

                var url = Uri.parse(note.image_URL)

                Log.d("image_url",""+url.toString())

                Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(holder.imagebox)
                holder.imagebox.visibility = ImageView.VISIBLE



//            var data = note.image_URL!!.split("|")
//            var path_string = data[0]
//            Log.d("data",""+path_string)
//
//            storageRef.child(path_string)
//                .downloadUrl
//                .addOnSuccessListener {
//                    Glide.with(context)
//                        .load(it)
//                        .diskCacheStrategy(DiskCacheStrategy.DATA)
//                        .into(holder.imagebox)
//
//                    holder.imagebox.visibility = ImageView.VISIBLE
//                }.addOnFailureListener {
//
//                }
            }

    }

    override fun getSnapshots(): ObservableSnapshotArray<Note_Data_Model> {
        return super.getSnapshots()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewAdapter {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_box_layout, parent, false)
        return viewAdapter(view)
    }


    class viewAdapter(itemView: View) : RecyclerView.ViewHolder(itemView){
        var title: TextView
        var des: TextView
        var tag1: TextView
        var tag2: TextView
        var main_box: LinearLayout
        var imagebox: ImageView
        var punch_hole: ImageView

        init{
            title = itemView.findViewById(R.id.note_title)
            des = itemView.findViewById(R.id.note_desc)
            tag1 = itemView.findViewById(R.id.tag1)
            tag2 = itemView.findViewById(R.id.tag2)
            main_box = itemView.findViewById(R.id.main_box)
            imagebox = itemView.findViewById(R.id.imagecard)
            punch_hole = itemView.findViewById(R.id.punch_hole)



        }
    }


}