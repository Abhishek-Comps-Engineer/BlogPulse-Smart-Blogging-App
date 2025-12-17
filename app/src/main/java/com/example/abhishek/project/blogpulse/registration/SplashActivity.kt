package com.example.abhishek.project.blogpulse.registration

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.abhishek.project.blogpulse.R
import com.example.abhishek.project.blogpulse.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SplashActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private var currentUser: FirebaseUser? = auth.currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if ( currentUser == null ) {
            Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()

            }, 2000)
        }
    }

    override fun onStart() {
        super.onStart()
        if ( currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}