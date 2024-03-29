package com.collide.noteit.dataClass

import com.google.firebase.Timestamp


data class Note_Data_Model(
    var title: String? = "",
    var des: String? = "",
    var image_URL: String? = "",
    var order_view_all: String? = "",
    var edit_text_data_all: String? = "",
    var task_data_all: String? = "",
    var note_id: String? = "",
    var note_color: String? = "",
    var task_check: String? = "",
    var created_date: String? = "",
    var pinned_note: String? = "",
    var timestamp: Timestamp? = null,
    var timestamp2: Timestamp? = null
)
