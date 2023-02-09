package com.collide.noteit.customView

import android.content.Context
import android.content.res.TypedArray
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.util.AttributeSet
import android.util.Log
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.collide.noteit.Note_Activity
import com.collide.noteit.R

class ETCheckbox(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var attributes: TypedArray
    private var checkbox: CheckBox
    private var editText: EditText
//    private var spannableString: SpannableString

    init {
        inflate(context, R.layout.et_checkbox, this)
        attributes = context!!.obtainStyledAttributes(attrs, R.styleable.ETCheckbox,0,0)
        checkbox = findViewById(R.id.checkBox_et)
        editText = findViewById(R.id.et_task)
        checkbox.isChecked = attributes.getBoolean(R.styleable.ETCheckbox_ChkBox, false)
//        spannableString = SpannableString(editText.text.toString())
        checkbox.setOnClickListener {
//            spannableString = SpannableString(editText.text.toString())
            if(editText.text.toString() != ""){
                etEnable(checkbox.isChecked)
            }
            else{
                checkbox.isChecked = false
            }
        }
    }

    fun getDataEditText(): Boolean {
        if(editText.text.isEmpty()){
            return false
        }
        return true
    }

    private fun etEnable(checked: Boolean) {
        if(editText.text.toString() != "" && checked){
            var et_text = editText.text.toString()
            var spannableString = SpannableString(editText.text.toString())
            spannableString.setSpan(StrikethroughSpan(), 0, et_text.length, 0 )
            editText.setText(spannableString)
            editText.isEnabled = false
            Log.d("data-main", ""+editText.text.toString())
        } else if(!checked){
            var spannableString = SpannableString(editText.text.toString())
            spannableString.removeSpan(StrikethroughSpan())
            editText.setText(spannableString)
            editText.isEnabled = true
            Log.d("data-main", ""+editText.text.toString())

        }
    }

    private fun chkEmpty(): Boolean {
        return editText.text.toString() == ""
    }




}