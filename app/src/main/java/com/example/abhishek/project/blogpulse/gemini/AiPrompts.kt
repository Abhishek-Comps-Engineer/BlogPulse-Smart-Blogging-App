package com.example.blogpulse.gemini

object AiPrompts {

    fun improveBlog(title: String, content: String): String =
        """
        You are a professional blog editor.
        Improve grammar, clarity, and flow without changing meaning.

        Return strictly in this format:

        TITLE:
        <improved title>

        CONTENT:
        <improved content>

        Blog Title:
        $title

        Blog Content:
        $content
        """.trimIndent()
}
