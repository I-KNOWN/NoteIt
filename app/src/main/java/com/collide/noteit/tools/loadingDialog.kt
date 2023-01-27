package com.collide.noteit.tools

import android.app.Activity
import android.app.AlertDialog
import com.collide.noteit.R

class loadingDialog(val mActivity: Activity) {

    private lateinit var isdialog: AlertDialog

    fun startloading(){
        val inflater = mActivity.layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_item, null)

        val builder = AlertDialog.Builder(mActivity)
        builder.setView(dialogView)
        builder.setCancelable(false)

        isdialog = builder.create()
        isdialog.show()

    }

    fun isDismis(){
        isdialog.dismiss()
    }

}