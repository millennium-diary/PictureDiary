package com.example.picturediary.navigation.model

data class UserDTO (
    var email : String? = null,
    var uid : String? = null,
    var userGroups : ArrayList<String>? = null
)