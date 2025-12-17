package com.example.abhishek.project.blogpulse.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.abhishek.project.blogpulse.BuildConfig
import com.example.abhishek.project.blogpulse.databinding.BottomSheetAiBinding
import com.example.blogpulse.gemini.AiPrompts
import com.example.blogpulse.gemini.GeminiClient
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class AiBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnAiImprove.setOnClickListener { improveBlog() }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun improveBlog() {
        binding.btnAiImprove.isEnabled = false

        val title = arguments?.getString(ARG_TITLE).orEmpty()
        val content = arguments?.getString(ARG_CONTENT).orEmpty()

        if (content.isBlank()) {
            toast("Content is empty")
            binding.btnAiImprove.isEnabled = true
            return
        }

        lifecycleScope.launch {
            binding.btnAiImprove.isEnabled = false

            try {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    throw IllegalStateException("Gemini API key is missing")
                }
                Log.d("GeminiKeyCheck", "API key exists = true")

                val prompt = AiPrompts.improveBlog(title, content)

                val aiResponse = GeminiClient.generate(
                    prompt = prompt,
                    apiKey = BuildConfig.GEMINI_API_KEY
                )

                val improvedTitle = aiResponse
                    .substringAfter("TITLE:", "")
                    .substringBefore("CONTENT:", "")
                    .trim()
                    .ifBlank { title }

                val improvedContent = aiResponse
                    .substringAfter("CONTENT:", "")
                    .trim()
                    .ifBlank { content }

                parentFragmentManager.setFragmentResult(
                    RESULT_KEY,
                    Bundle().apply {
                        putString(KEY_TITLE, improvedTitle)
                        putString(KEY_CONTENT, improvedContent)
                    }
                )

                dismiss()

            } catch (e: Exception) {
                // Log detailed error
                Log.e("GeminiError", "Failed to generate AI content", e)
                // Show user-friendly message
                toast(e.message ?: "Failed to improve blog via AI. Please try again.")
            } finally {
                // Always re-enable button
                binding.btnAiImprove.isEnabled = true
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        const val RESULT_KEY = "ai_blog_result"
        const val KEY_TITLE = "key_title"
        const val KEY_CONTENT = "key_content"

        private const val ARG_TITLE = "arg_title"
        private const val ARG_CONTENT = "arg_content"

        fun newInstance(title: String, content: String): AiBottomSheetFragment {
            return AiBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_CONTENT, content)
                }
            }
        }
    }
}
