package com.example.eagleai

import android.graphics.Bitmap
import android.net.Uri


//user can make
sealed class ChatEvent {
    data class UpdatePrompt(val newPrompt:String ):ChatEvent()
    data class SendPrompt(val prompt:String,val bitmap: Bitmap?):ChatEvent()




}