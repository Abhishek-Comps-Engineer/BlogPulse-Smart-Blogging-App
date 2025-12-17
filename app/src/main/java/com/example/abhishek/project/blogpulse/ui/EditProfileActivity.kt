package com.example.abhishek.project.blogpulse.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.abhishek.project.blogpulse.databinding.ActivityEditProfileBinding
import com.example.abhishek.project.blogpulse.models.UserData
import com.example.abhishek.project.blogpulse.utills.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    private val binding : ActivityEditProfileBinding by lazy{
        ActivityEditProfileBinding.inflate(layoutInflater)
    }

    private val auth = FirebaseAuth.getInstance()
    private val user  = auth.currentUser
    private val dbRef = FirebaseDatabase.getInstance()
    private val userRef = dbRef.getReference(Constants.USERS)


    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val localPath = copyImageToInternalStorage(it)
                saveLocalImagePath(localPath)
                showImageFromPath(localPath)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            updateUserProfile()
        }



        loadSavedImage()
        loadUserData()

        binding.imgProfile.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(filesDir, "profile_image.jpg")

        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        return file.absolutePath
    }

    private fun saveLocalImagePath(path: String) {
        val prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("profile_image_path", path)
            .apply()
    }

    private fun showImageFromPath(path: String) {
        Glide.with(this)
            .load(File(path))
            .into(binding.imgProfile)
    }


    private fun loadSavedImage() {
        val prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        val path = prefs.getString("profile_image_path", null)

        path?.let {
            Glide.with(this)
                .load(File(it))
                .into(binding.imgProfile)
        }
    }


    private fun updateUserProfile() {

        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userData = UserData(
            name = name,
            phone = phone,
            location = location,
            bio = bio
        )
        updateProfileFirebase(userData)
    }

    private fun loadUserData() {
        user?.let {
            userRef.child(it.uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    binding.etName.setText(snapshot.child(Constants.NAME).value?.toString())
                    binding.etPhone.setText(snapshot.child(Constants.PHONE).value?.toString())
                    binding.etLocation.setText(snapshot.child(Constants.LOCATION).value?.toString())
                    binding.etBio.setText(snapshot.child(Constants.BIO).value?.toString())
                }
            }
        }
    }


    private fun updateProfileFirebase(userData: UserData) {
        user?.let {
            val userId = it.uid

            val userMap = mapOf(
                Constants.NAME to userData.name,
                Constants.PHONE to userData.phone,
                Constants.LOCATION to userData.location,
                Constants.BIO to userData.bio
            )

            userRef.child(userId)
                .updateChildren(userMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Profile update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}