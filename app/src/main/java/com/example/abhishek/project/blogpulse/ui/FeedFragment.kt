package com.example.abhishek.project.blogpulse.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abhishek.project.blogpulse.adpaters.BlogAdapter
import com.example.abhishek.project.blogpulse.databinding.FragmentFeedBinding
import com.example.abhishek.project.blogpulse.models.BlogDataModel
import com.example.abhishek.project.blogpulse.utills.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BlogAdapter
    private val itemList = mutableListOf<BlogDataModel>()   // FIXED

    private val databaseRef = FirebaseDatabase.getInstance()

    private val blogRef = databaseRef.getReference(Constants.BLOGS)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        binding.addBlogFloatBtn.setOnClickListener {
            startActivity(Intent(requireContext(), ABlogActivity::class.java))
        }

        setupRecyclerView()

        loadDataFromFirebase()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = BlogAdapter(itemList)
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFeed.adapter = adapter
    }

    private fun loadDataFromFirebase() {
        blogRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {

                itemList.clear()

                for (snapshot in snapshots.children) {
                    val itemBlog = snapshot.getValue(BlogDataModel::class.java)
                    if (itemBlog != null) {
                        itemList.add(itemBlog)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG","Failed to read value")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
