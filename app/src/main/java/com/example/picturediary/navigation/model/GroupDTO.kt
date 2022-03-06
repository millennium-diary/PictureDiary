package com.example.picturediary.navigation.model

data class GroupDTO (
    var grpid : String? = null,
    var grpname : String? = null,
    var creator : String? = null,
    var timestamp : Long? = null,
    var shareWith : ArrayList<String>? = null,
    var contentDTO: ContentDTO? = null
)