package com.collide.noteit.recyclerAdapter

import android.content.Context
import android.media.Image
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.R
import com.collide.noteit.dataClass.Note_Data_Model
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.SnapshotParser
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.core.SnapshotHolder
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SnapshotMetadata
import com.google.firebase.firestore.local.ReferenceSet
import com.google.firebase.storage.FirebaseStorage
import okhttp3.internal.cache.DiskLruCache.Snapshot
import org.w3c.dom.Text

class NoteDisplayAdapter(options: FirestoreRecyclerOptions<Note_Data_Model>, var context: Context) :
    FirestoreRecyclerAdapter<Note_Data_Model, NoteDisplayAdapter.viewAdapter>(options) {

    private lateinit var listener: onItemClickListener

    var dataList = mutableListOf<Note_Data_Model>()
    val storageRef = FirebaseStorage.getInstance().reference

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(
        holder: viewAdapter,
        position: Int,
        note: Note_Data_Model
    ) {
        holder.title.text = note.title

        var data_note_et = note.edit_text_data_all!!.split("|&@!~~~|")

        holder.des.text = Html.fromHtml(data_note_et[0],   Html.FROM_HTML_MODE_COMPACT)
        Log.d("image_url",""+holder.des.length())
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewAdapter {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_box_layout, parent, false)
        return viewAdapter(view, listener)
    }


    class viewAdapter(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        var title: TextView
        var des: TextView
        var tag1: TextView
        var tag2: TextView
        var main_box: LinearLayout
        var imagebox: ImageView

        init{
            title = itemView.findViewById(R.id.note_title)
            des = itemView.findViewById(R.id.note_desc)
            tag1 = itemView.findViewById(R.id.tag1)
            tag2 = itemView.findViewById(R.id.tag2)
            main_box = itemView.findViewById(R.id.main_box)
            imagebox = itemView.findViewById(R.id.imagecard)

            itemView.setOnClickListener {
                var position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION && listener != null){
                    listener.onItemClick()
                }

            }

        }
    }

    interface onItemClickListener{
        fun onItemClick(documentSnapshot: DocumentSnapshot, position: Int   )
    }

    fun setOnItemClickListener(listener: onItemClickListener ){
        this.listener = listener
    }

}