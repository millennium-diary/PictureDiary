package com.example.picturediary.navigation.model

import android.graphics.Bitmap


data class DrawingDTO(
    var drawId: String? = null,
    var user: String? = null,
    var image: Bitmap? = null,
    var group: String? = null
)