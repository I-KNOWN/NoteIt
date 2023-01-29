package com.collide.noteit.dataClass

import java.util.Date

data class Note_Image_Data_Model(

    var localUri: String = "",
    var remoteUri: String = "",
    var dateTaken: Date = Date(),
    var id: String = ""
)
