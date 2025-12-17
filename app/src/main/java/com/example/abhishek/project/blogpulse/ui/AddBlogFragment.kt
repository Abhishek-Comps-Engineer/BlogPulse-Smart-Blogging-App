package com.example.abhishek.project.blogpulse.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.abhishek.project.blogpulse.databinding.FragmentAddBlogBinding
import com.example.abhishek.project.blogpulse.models.BlogDataModel
import com.example.abhishek.project.blogpulse.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AddBlogFragment : Fragment() {

    private val TAG = "AddBlogFragment"

    private var _binding: FragmentAddBlogBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val db by lazy {
        FirebaseDatabase.getInstance()
    }

    private val usersRef by lazy { db.getReference("users") }
    private val blogRef by lazy { db.getReference("blogs") }
    private val titleMapRef by lazy { db.getReference("title_to_blogKey") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBlogBinding.inflate(inflater, container, false)

        binding.fabAi.setOnClickListener {

            val title = binding.etTitle.text.toString()
            val content = binding.etDescription.text.toString()

            if (content.isBlank()) {
                toast("Write something first")
                return@setOnClickListener
            }

            AiBottomSheetFragment
                .newInstance(title, content)
                .show(parentFragmentManager, "AiBottomSheet")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            AiBottomSheetFragment.RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->

            val improvedTitle =
                bundle.getString(AiBottomSheetFragment.KEY_TITLE).orEmpty()

            val improvedContent =
                bundle.getString(AiBottomSheetFragment.KEY_CONTENT).orEmpty()

            binding.etTitle.setText(improvedTitle)
            binding.etDescription.setText(improvedContent)

            toast("AI improved your blog")
        }

        binding.btnAddBlog.setOnClickListener {
            saveBlogToFirebase()
        }
    }

    private fun saveBlogToFirebase() {
        val blogTitle = binding.etTitle.text.toString().trim()
        val blogDescription = binding.etDescription.text.toString().trim()
        val imageUrl = "Not added yet"

        if (blogTitle.isBlank() || blogDescription.isBlank()) {
            toast("All fields are required")
            return
        }

        val currentUser: FirebaseUser = auth.currentUser ?: run {
            toast("User not logged in")
            return
        }

        val userId = currentUser.uid
        val currentDate = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(Date())

        usersRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserData::class.java)

                    if (userData == null) {
                        toast("User data not found")
                        return
                    }

                    val userName = userData.name ?: "Anonymous"
                    val blogKey = blogRef.push().key ?: run {
                        toast("Failed to generate blog key")
                        return
                    }

                    val blogData = BlogDataModel(
                        blogKey = blogKey,
                        userName = userName,
                        blogTitle = blogTitle,
                        description = blogDescription,
                        imageUrl = imageUrl,
                        currentDate = currentDate,
                        likeCounts = 0,
                        isLiked = false
                    )

                    blogRef.child(blogKey)
                        .setValue(blogData)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                val safeTitle = sanitizeKey(blogTitle)
                                titleMapRef.child(safeTitle).setValue(blogKey)

                                toast("Blog added successfully")
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            } else {
                                Log.e(TAG, "Blog upload failed", task.exception)
                                toast("Blog upload failed")
                            }
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    toast("Firebase error: ${error.message}")
                }
            })
    }

    private fun sanitizeKey(key: String): String {
        return key.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("/", "_")
            .replace(" ", "_")
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
