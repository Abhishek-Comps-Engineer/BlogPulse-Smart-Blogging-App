package com.example.abhishek.project.blogpulse.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.abhishek.project.blogpulse.databinding.ActivityRegisterBinding
import com.example.abhishek.project.blogpulse.models.UserData
import com.example.abhishek.project.blogpulse.ui.MainActivity
import com.example.abhishek.project.blogpulse.utills.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var name : String

    private lateinit var email: String
    private lateinit var password : String
    private var checkBox = false

    private var auth = FirebaseAuth.getInstance()
    private lateinit var database: FirebaseDatabase
    private val binding : ActivityRegisterBinding by lazy {
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.createAccountButton.setOnClickListener {
            signUp()
        }

        database = FirebaseDatabase.getInstance()


    }


    private fun signUp() {
        name = binding.fullnameEdittext.text.toString()
        email = binding.emailEdittext.text.toString()
        password = binding.passwordEdittext.text.toString()
        checkBox = binding.termsCheckbox.isChecked

        if ( name.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please, enter all the fields", Toast.LENGTH_SHORT).show()
            return
        }else if (!checkBox){
            Toast.makeText(this,"Please check the \"Terms and Condition\"",Toast.LENGTH_SHORT).show()
            return
        }else{
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
                if ( it.isSuccessful){

                    val user = auth.currentUser
                    Log.d("RegisterActivity",user.toString())

                    if ( user != null) {
                        val userId = user.uid
                        var note = UserData(name,email)
                        database.getReference(Constants.USERS)
                            .child(userId)
                            .setValue(note)
                            .addOnSuccessListener {
                                Log.d("RegisterActivity", "Database write SUCCESS")
                            }
                            .addOnFailureListener { e ->
                                Log.e("RegisterActivity", "Database write FAILED: ${e.message}")
                            }
                            .addOnCompleteListener {
                                Log.d("RegisterActivity", "Database write COMPLETED")
                            }
                    }
                    Toast.makeText(this,"Account created successfully", Toast.LENGTH_SHORT).show()
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Toast.makeText(this,"Account creation failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this,exception.localizedMessage, Toast.LENGTH_SHORT).show()

            }
        }
    }

}

