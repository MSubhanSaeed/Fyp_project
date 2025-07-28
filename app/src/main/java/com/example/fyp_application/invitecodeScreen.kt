package com.example.fyp_application

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_application.databinding.ActivityInvitecodeScreenBinding

class InvitecodeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityInvitecodeScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvitecodeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Generate and set the invite code
        val generatedCode = intent.getStringExtra("Group_Code") ?: ""
        binding.inviteCodeText.text = generatedCode

        // Back arrow click
        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Share button click - shows full Android share sheet directly
        binding.sendCodeButton.setOnClickListener {
            shareInviteCode(generatedCode)
        }
    }

    private fun shareInviteCode(code: String) {
        // First copy to clipboard
        copyToClipboard(code)

        // Then show full Android share sheet
        val shareMessage = "Join my Friends Circle using this code: $code"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        // Create chooser with title
        val shareIntent = Intent.createChooser(sendIntent, "Share invite code")
        startActivity(shareIntent)
    }

    private fun copyToClipboard(code: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Invite Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}