package com.example.picturediary.navigation.model

import android.graphics.Bitmap


data class DrawingDTO(
    var drawId: String? = null,
    var user: String? = null,
    var content: String? = null,
    var image: ByteArray? = null
)
