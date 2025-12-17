package com.example.abhishek.project.blogpulse.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.abhishek.project.blogpulse.databinding.ActivityLoginBinding
import com.example.abhishek.project.blogpulse.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    private lateinit var email : String
    private lateinit var password : String
    private val binding : ActivityLoginBinding by lazy{
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.loginButton.setOnClickListener{

            email = binding.emailEdittext.text.toString()
            password = binding.passwordEdittext.text.toString()

            if( email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Please, enter all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                signIn()
            }
        }
        binding.signupPromptText.setOnClickListener{
            startActivity(Intent (this, RegisterActivity::class.java))
            finish()
        }
    }

    fun signIn() {
        auth.signInWithEmailAndPassword(email,password).addOnCompleteListener{
            if (it.isSuccessful){
                Toast.makeText(this,"Login Successful 😊", Toast.LENGTH_SHORT).show()
                var intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}