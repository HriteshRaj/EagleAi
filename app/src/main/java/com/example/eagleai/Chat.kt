package com.example.eagleai

import android.graphics.Bitmap
import android.net.Uri

data class Chat(
    val prompt:String,
    val bitmap: Bitmap?,
    val isFromUser:Boolean
)
