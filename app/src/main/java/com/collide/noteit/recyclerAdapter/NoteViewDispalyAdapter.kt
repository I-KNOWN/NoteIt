package com.collide.noteit.recyclerAdapter

import android.content.Context
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.collide.noteit.R
import com.collide.noteit.dataClass.Note_Data_Model

class NoteViewDispalyAdapter(private var noteList: ArrayList<Note_Data_Model>, var context: Context, var noteInterface: onNoteListener): RecyclerView.Adapter<NoteViewDispalyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_box_layout, parent, false)
        return ViewHolder(view, noteInterface)
    }

    fun setFilteredList(noteList: ArrayList<Note_Data_Model>){
        this.noteList = noteList
        notifyDataSetChanged()
    }
    fun getCurrentList():ArrayList<Note_Data_Model>{
         return noteList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = noteList[position]
        holder.title.text = note.title
        var data_note_et = note.edit_text_data_all!!.split("|&@!~~~|")
        holder.des.text = Html.fromHtml(data_note_et[0],   Html.FROM_HTML_MODE_COMPACT)
        when(note.note_color){
            "blue" ->
                holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_blue)
            "red" ->
                holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_red)
            "green" ->
                holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_green)
            "yellow" ->
                holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_yellow)
            "pink" ->
                holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_pink)
            "purple" ->
                holder.punch_hole.setBackgroundResource(R.drawable.hole_punch_circle_purple)
        }
        if(note.image_URL != ""){
            var url = Uri.parse(note.image_URL)
            Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.imagebox)
            holder.imagebox.visibility = ImageView.VISIBLE
        }else{
            holder.imagebox.visibility = ImageView.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    class ViewHolder(itemview: View, noteInterface: onNoteListener): RecyclerView.ViewHolder(itemview) {
        var title: TextView
        var des: TextView
        var main_box: LinearLayout
        var imagebox: ImageView
        var punch_hole: ImageView

        init{
            title = itemView.findViewById(R.id.note_title)
            des = itemView.findViewById(R.id.note_desc)
            main_box = itemView.findViewById(R.id.main_box)
            imagebox = itemView.findViewById(R.id.imagecard)
            punch_hole = itemView.findViewById(R.id.punch_hole)

            itemview.setOnClickListener{
                if(noteInterface != null){
                    var pos = absoluteAdapterPosition
                    if(pos != RecyclerView.NO_POSITION){
                        noteInterface.onNoteClick(pos, it.parent as RecyclerView)
                    }
                }
            }
            itemview.setOnLongClickListener(View.OnLongClickListener {
                var parent = it.parent as RecyclerView
                if(noteInterface != null){
                    var pos = absoluteAdapterPosition
                    if(pos != RecyclerView.NO_POSITION){
                        if(parent.transitionName == "RU"){
                            noteInterface.onNoteOption(pos)
                        }else if(parent.transitionName == "RP"){
                            noteInterface.onNoteOptionUnpin(pos)
                        }

                    }
                }
                return@OnLongClickListener true
            })
        }
    }
    interface onNoteListener{
        fun onNoteClick(position: Int, view: View)
        fun onNoteOption(position: Int)
        fun onNoteOptionUnpin(position: Int)
    }

}