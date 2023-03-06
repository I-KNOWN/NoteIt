package com.collide.noteit.recyclerAdapter

import android.content.Context
import android.media.Image
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.collide.noteit.R
import com.collide.noteit.dataClass.Note_Data_Model
import com.google.android.material.card.MaterialCardView
import org.w3c.dom.Text

class NoteDisplayAdapter_dupe(var context: Context) :
    RecyclerView.Adapter<NoteDisplayAdapter_dupe.viewAdapter>() {

    var dataList = mutableListOf<Note_Data_Model>()


    internal fun setDataList(dataList: MutableList<Note_Data_Model>){

        this.dataList = dataList
//        notifyItemInserted(dataList.size - 1)
        Log.d("data-list-msg",""+this.dataList)
        notifyItemInserted(dataList.size - 1)
    }

    internal fun setDataModel(dataList: MutableList<Note_Data_Model>){

        this.dataList = dataList

        Log.d("data-list-msg",""+this.dataList)

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewAdapter {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_box_layout, parent, false)
        return viewAdapter(view)
    }

    override fun onBindViewHolder(holder: viewAdapter, position: Int) {

        holder.title.text = dataList[position].title
        var des_str = dataList[position].des

//
//
//        if(des_str?.length > 58 ){
//                var res = des_str?.subSequence(0, 58).toString()
//                res = res + "..."
//                holder.imagebox.visibility = ImageView.VISIBLE
//                holder.des.text = res
//        } else if (des_str?.length > 150){
//                var res = des_str?.subSequence(0, 149).toString()
//                res = res + "..."
//                holder.des.text = res
//        }

//        holder.main_box.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.main_recyclerview_note) )

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class viewAdapter(itemView: View) : RecyclerView.ViewHolder(itemView){
        var title: TextView
        var des: TextView
        var main_box: LinearLayout
        var imagebox: ImageView

        init{
            title = itemView.findViewById(R.id.note_title)
            des = itemView.findViewById(R.id.note_desc)

            main_box = itemView.findViewById(R.id.main_box)
            imagebox = itemView.findViewById(R.id.imagecard)
        }
    }

}