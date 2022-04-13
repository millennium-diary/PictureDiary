package com.example.picturediary.navigation.model

import android.net.Uri

data class UserDTO (
    var uid : String? = null,
    var imageUrl: String? = null,
    var username : String? = null,
    var message : String? = null,
    var userGroups : ArrayList<String>? = null
)