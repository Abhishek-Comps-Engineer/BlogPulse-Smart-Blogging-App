package com.example.abhishek.project.blogpulse.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.abhishek.project.blogpulse.databinding.ActivityAblogBinding
import com.example.abhishek.project.blogpulse.models.BlogDataModel
import com.example.abhishek.project.blogpulse.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ABlogActivity : AppCompatActivity() {

    private val TAG = "ABlogActivityDebug"

    private val db = FirebaseDatabase.getInstance()
    private val usersRef = db.getReference("users")
    private val blogRef = db.getReference("blogs")
    private val titleMapRef = db.getReference("title_to_blogKey") // Optional mapping

    private val auth: FirebaseAuth? = FirebaseAuth.getInstance()

    private val binding: ActivityAblogBinding by lazy {
        ActivityAblogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()

        Log.d(TAG, "Activity created")


        binding.btnAddBlog.setOnClickListener {
            saveBlogToFirebase()
        }
    }

    private fun saveBlogToFirebase() {
        val blogTitle = binding.etTitle.text.toString().trim()
        val blogDescription = binding.etDescription.text.toString().trim()
        val imageUrl = "Not added yet"

        if (blogTitle.isBlank() || blogDescription.isBlank()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser: FirebaseUser = auth?.currentUser ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.uid
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(UserData::class.java)
                if (userData == null) {
                    Toast.makeText(this@ABlogActivity, "User data not found", Toast.LENGTH_SHORT).show()
                    return
                }

                val userName = userData.name ?: "Anonymous"
                val blogKey = blogRef.push().key
                if (blogKey == null) {
                    Toast.makeText(this@ABlogActivity, "Failed to generate blog key", Toast.LENGTH_SHORT).show()
                    return
                }


                val blogMap = BlogDataModel(
                    blogKey = blogKey,
                    userName = userName,
                    blogTitle = blogTitle,
                    description = blogDescription,
                    imageUrl = imageUrl,
                    currentDate = currentDate,
                    likeCounts = 0 ,
                    isLiked = false
                )


                // Use updateChildren on blogKey (atomic)
                blogRef.child(blogKey).setValue(blogMap)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val safeTitle = sanitizeKey(blogTitle)
                            titleMapRef.child(safeTitle).setValue(blogKey)

                            Toast.makeText(this@ABlogActivity, "Blog added successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.e(TAG, "Blog upload FAILED", task.exception)
                            Toast.makeText(this@ABlogActivity, "Blog upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ABlogActivity, "Firebase error: ${error.message}", Toast.LENGTH_SHORT).show()
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
            .replace(" ", "_") // optional
    }
}
