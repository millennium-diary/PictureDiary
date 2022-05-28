package com.example.picturediary.navigation.model


data class ObjectDTO(
    var fullDraw: String? = null,
    var objId: Int? = null,
    var startX: Float? = null,
    var startY: Float? = null,
    var width: Float? = null,
    var height: Float? = null,
    var drawObjWhole: ByteArray? = null,
    var drawObjOnly: ByteArray? = null,
    var replaceDraw: ByteArray? = null,
    var motion: String? = null
)
