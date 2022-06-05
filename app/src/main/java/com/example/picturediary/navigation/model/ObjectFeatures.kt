package com.example.picturediary.navigation.model

import android.graphics.Bitmap

data class ObjectFeatures (
    var left: Float? = null,
    var right: Float? = null,
    var top: Float? = null,
    var bottom: Float? = null,
    var drawObjWhole: Bitmap? = null,
    var originalDraw: Bitmap? = null,
)