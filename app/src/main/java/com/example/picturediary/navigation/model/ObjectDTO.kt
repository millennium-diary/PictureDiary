package com.example.picturediary.navigation.model


data class ObjectDTO(
    var fullDraw: String? = null,
    var objId: Int? = null,
    var drawObjWhole: ByteArray? = null,
    var drawObjOnly: ByteArray? = null,
    var motion: String? = null
)
