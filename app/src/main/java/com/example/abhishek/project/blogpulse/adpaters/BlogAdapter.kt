package com.example.abhishek.project.blogpulse.adpaters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.abhishek.project.blogpulse.databinding.ItemFeedsBinding
import com.example.abhishek.project.blogpulse.models.BlogDataModel
import com.example.abhishek.project.blogpulse.R
import com.example.abhishek.project.blogpulse.utills.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BlogAdapter(private val items: List<BlogDataModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val user  = auth.currentUser
    private val dbRef = FirebaseDatabase.getInstance()
    private val blogsRef = dbRef.getReference(Constants.BLOGS)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFeedsBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BlogViewHolder(private val binding: ItemFeedsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(blogItemData: BlogDataModel) {
            binding.userNameFeed.text = blogItemData.userName
            binding.titleFeed.text = blogItemData.blogTitle
            binding.desciptionFeed.text = blogItemData.description
            binding.dateFeed.text = blogItemData.currentDate
            binding.likeCount.text = blogItemData.likeCounts.toString()

            val currentUserUid = user?.uid ?: return

            // Check if current user already liked this blog
            blogsRef.child(blogItemData.blogKey).child("likes").child(currentUserUid)
                .get()
                .addOnSuccessListener { snapshot ->
                    blogItemData.isLiked = snapshot.exists()
                    binding.likeIcon.setImageResource(
                        if (blogItemData.isLiked) R.drawable.ic_liked_favourite
                        else R.drawable.ic_favourite
                    )
                }

            binding.likeIcon.setOnClickListener {
                if (!blogItemData.isLiked) {
                    // Increment like count
                    val newLikeCount = blogItemData.likeCounts + 1
                    blogItemData.likeCounts = newLikeCount
                    blogItemData.isLiked = true

                    // Update UI
                    binding.likeCount.text = newLikeCount.toString()
                    binding.likeIcon.setImageResource(R.drawable.ic_liked_favourite)

                    // Update Firebase
                    val updates = mapOf<String, Any>(
                        "likeCounts" to newLikeCount,
                        "likes/$currentUserUid" to true
                    )
                    blogsRef.child(blogItemData.blogKey).updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(binding.root.context, "Liked!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(binding.root.context, "Failed to like: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(binding.root.context, "You already liked this!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
