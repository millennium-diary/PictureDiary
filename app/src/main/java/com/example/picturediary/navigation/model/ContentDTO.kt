package com.example.picturediary.navigation.model

data class ContentDTO (
    var contentId: String? = null,
    var groupId: String? = null,
    var explain: String? = null,
    var imageUrl : String? = null,
    var timestamp : Long? = null,
    var favoriteCount : Int = 0,
    var favorites : MutableMap<String, Boolean> = HashMap(),
    var username : String? = null,
    var uid: String? = null
)
