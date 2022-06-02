package com.example.picturediary.navigation.model


data class ObjectDTO(
    var fullDraw: String? = null,
    var objId: Int? = null,
    var left: Float? = null,
    var right: Float? = null,
    var top: Float? = null,
    var bottom: Float? = null,
    var drawObjWhole: ByteArray? = null,
    var drawObjOnly: ByteArray? = null,
    var replaceDraw: ByteArray? = null,
    var motion: String? = null,
)
