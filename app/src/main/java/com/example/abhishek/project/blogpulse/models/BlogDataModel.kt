package com.example.abhishek.project.blogpulse.models

data class BlogDataModel(
    val blogKey: String = "",
    val userName:String = "",
    val blogTitle: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val currentDate: String = "",
    var likeCounts :Int,
    var isLiked : Boolean,
    var likes: Map<String, Boolean> = emptyMap()

)
{
    constructor() : this("", "", "", "","","",0,false)
    constructor(likeCounts: Int,isLiked: Boolean) : this("", "", "", "","","",likeCounts,isLiked)
}
