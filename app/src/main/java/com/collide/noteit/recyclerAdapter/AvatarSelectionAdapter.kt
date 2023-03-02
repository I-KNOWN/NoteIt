package com.collide.noteit.recyclerAdapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.collide.noteit.R
import com.collide.noteit.dataClass.Avatar_Selection_Model
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class AvatarSelectionAdapter(var context: Context):
    RecyclerView.Adapter<AvatarSelectionAdapter.ViewHolder>() {


    private lateinit var mListener: onItemClickListener
    private var _drawable: Drawable? = null
    var index_position = 0

    var dataList = emptyList<Avatar_Selection_Model>()

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setIndexPosition(position: Int){
        index_position = position
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    internal fun setDataList(dataList: List<Avatar_Selection_Model>){
        this.dataList = dataList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView){

        var imageview: ImageView
        var cardview: MaterialCardView
        var cardview2: MaterialCardView

        init{
            imageview = itemView.findViewById(R.id.avatar_selection_view)
            cardview = itemView.findViewById(R.id.avatar_selection_view_card)
            cardview2 = itemView.findViewById(R.id.iv_check)

            cardview.setOnClickListener {
                listener.onItemClick(adapterPosition)

            }


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view =  LayoutInflater.from(parent.context).inflate(R.layout.avatar_layout, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var data = dataList[position]

        holder.imageview.setImageDrawable(ContextCompat.getDrawable(context, data.icon_uri!!))

//        val uri = Uri.parse(data.icon_uri)
//
//
//
//        Picasso
//            .get()
//            .load(uri)
//            .into(holder.imageview)


        if(index_position  == position){


            var random_number = (1..5).random()

//            when(random_number){
//                1 ->
//                    _drawable = ContextCompat.getDrawable(context,R.drawable.circular_avatar_selected1)
//                2 ->
//                    _drawable = ContextCompat.getDrawable(context,R.drawable.circular_avatar_selected2)
//                3 ->
//                    _drawable = ContextCompat.getDrawable(context,R.drawable.circular_avatar_selected3)
//                4 ->
//                    _drawable = ContextCompat.getDrawable(context,R.drawable.circular_avatar_selected4)
//                5 ->
//                    _drawable = ContextCompat.getDrawable(context,R.drawable.circular_avatar_selected5)
//
//            }


            when(random_number){
                1 ->
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#697EEC"))

                2 ->
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#DA69EC"))

                3 ->
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#ECA069"))

                4 ->
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#EC6971"))

                5 ->
                    holder.cardview.setCardBackgroundColor(Color.parseColor("#ECE769"))


            }


            holder.cardview2.visibility = View.VISIBLE

        } else {
//            var border_drawable = ContextCompat.getDrawable(context,R.drawable.circular_avatar_selection)
//
//            holder.imageview.setImageDrawable(border_drawable)

            holder.cardview.setCardBackgroundColor(Color.parseColor("#DEDEDE"))

            holder.cardview2.visibility = View.INVISIBLE

        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}