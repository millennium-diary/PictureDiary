package com.example.picturediary.navigation.model

data class UserDTO (
    var uid : String? = null,
    var username : String? = null,
    var userGroups : ArrayList<String>? = null
)