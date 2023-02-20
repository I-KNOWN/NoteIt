package com.collide.noteit.customView

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.collide.noteit.MainActivity
import com.collide.noteit.Note_Activity
import com.collide.noteit.R

class ETCheckbox(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var attributes: TypedArray
    private var checkbox: CheckBox
    private var editText: EditText
    private var deletebtn: ImageView
//    private var spannableString: SpannableString

    init {
        inflate(context, R.layout.et_checkbox, this)
        attributes = context!!.obtainStyledAttributes(attrs, R.styleable.ETCheckbox,0,0)
        checkbox = findViewById(R.id.checkBox_et)
        editText = findViewById(R.id.et_task)
        deletebtn = findViewById(R.id.delete_btn)
        checkbox.isChecked = attributes.getBoolean(R.styleable.ETCheckbox_ChkBox, false)
//        spannableString = SpannableString(editText.text.toString())

        editText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(editText.text.isNotEmpty() && p0!!.length > 0 && p0!!.length < 2){

                    var parent = editText.parent.parent.parent.parent as LinearLayout
                    var childcount = parent.childCount

                    if(parent.getChildAt(childcount - 1 ) is LinearLayout){
                        var child = parent.getChildAt(childcount - 1) as LinearLayout
                        child.alpha = 1f
                        var alphanimation = AlphaAnimation(0.5f, 1.0f)
                        alphanimation.duration = 500
                        alphanimation.fillAfter = true
                        child.startAnimation(alphanimation)
//                        child.setOnClickListener{
//
//                            var obj = Note_Activity()
//                            obj.createTask()
//
//                        }

                    }


                }else if (editText.text.isEmpty()){
                    var parent = editText.parent.parent.parent.parent as LinearLayout
                    var childcount = parent.childCount

                    if(parent.getChildAt(childcount - 1 ) is LinearLayout){
                        var child = parent.getChildAt(childcount - 1) as LinearLayout
                        child.alpha = 1f
                        var alphanimation = AlphaAnimation(1f, 0.5f)
                        alphanimation.duration = 500
                        alphanimation.fillAfter = true
                        child.startAnimation(alphanimation)



                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                return
            }

        })

        checkbox.setOnClickListener {
//            spannableString = SpannableString(editText.text.toString())
            if(editText.text.toString() != ""){
                etEnable(checkbox.isChecked)
            }
            else{
                checkbox.isChecked = false
            }

        }
        deletebtn.setOnClickListener {
            var et = it.parent.parent.parent as ETCheckbox
            var etbox = it.parent.parent.parent.parent as LinearLayout
            var layoutbox = it.parent.parent.parent.parent.parent as LinearLayout

            if(etbox.childCount == 1){
                layoutbox.removeView(etbox)
            } else{
                etbox.removeView(et)
            }


            Log.d("click", ""+layoutbox.id)
        }

    }

    fun getDataETtext(): String{
        return editText.text.toString()
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