package com.example.abhishek.project.blogpulse.models

data class UserData (
      var name: String,
      var email: String,
      var location: String,
      var phone : String,
      var bio : String,
){
    constructor():this("","","","","")
    constructor(name: String, email: String):this(name,email,"","","")
    constructor(name: String,phone: String, location:String, bio:String):this(name,"",location,phone,bio)
}


