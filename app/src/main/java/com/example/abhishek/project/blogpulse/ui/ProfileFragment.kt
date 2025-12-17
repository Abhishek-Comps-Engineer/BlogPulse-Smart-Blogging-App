package com.example.abhishek.project.blogpulse.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.abhishek.project.blogpulse.databinding.FragmentProfileBinding
import com.example.abhishek.project.blogpulse.registration.LoginActivity
import com.example.abhishek.project.blogpulse.utills.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val TAG = "ProfileFragment"

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val user = auth.currentUser

    private val dbRef = FirebaseDatabase.getInstance()
    private val userRef = dbRef.getReference(Constants.USERS)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Load data
        viewLifecycleOwner.lifecycleScope.launch {
            loadUserData()
        }

        loadSavedImage()
        setupClicks()

        return binding.root
    }


    private fun setupClicks() {

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }


    private fun loadUserData() {
        user?.let { firebaseUser ->
            userRef.child(firebaseUser.uid).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) return@addOnSuccessListener

                    binding.txtName.text =
                        snapshot.child(Constants.NAME).value?.toString() ?: "User"

                    binding.txtEmail.text =
                        snapshot.child(Constants.EMAIL).value?.toString() ?: "-"

                    binding.txtPhone.text =
                        snapshot.child(Constants.PHONE).value?.toString() ?: "-"

                    binding.txtLocation.text =
                        snapshot.child(Constants.LOCATION).value?.toString() ?: "-"

                    binding.txtBio.text =
                        snapshot.child(Constants.BIO).value?.toString()
                            ?: "No bio added yet"

                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to load profile data", it)
                }
        }
    }


    private fun loadSavedImage() {
        val prefs = requireContext()
            .getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

        val path = prefs.getString("profile_image_path", null)

        Log.d(TAG, "Saved image path: $path")

        if (path.isNullOrEmpty()) return

        val file = File(path)
        if (!file.exists()) {
            Log.e(TAG, "Profile image file not found")
            return
        }

        Glide.with(this)
            .load(file)
            .circleCrop()
            .into(binding.imgUser)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
