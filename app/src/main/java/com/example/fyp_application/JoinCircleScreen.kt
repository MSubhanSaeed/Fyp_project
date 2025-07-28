package com.example.fyp_application

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivityJoinCircleScreenBinding
import com.example.fyp_application.dto.MemberEntity
import com.example.fyp_application.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinCircleScreen : AppCompatActivity() {

    private lateinit var binding: ActivityJoinCircleScreenBinding
    private lateinit var codeInputs: List<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinCircleScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ─── Back Arrow ────────────────────────────────────────────────────────
        binding.backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // ─── Code-input boxes ─────────────────────────────────────────────────
        codeInputs = listOf(
            binding.code1, binding.code2, binding.code3,
            binding.code4, binding.code5, binding.code6
        )
        setupCodeInputs()

        // ─── Submit button ────────────────────────────────────────────────────
        binding.submitButton.setOnClickListener {
            val groupCode = codeInputs.joinToString("") { it.text.toString().trim() }
            if (groupCode.length == 6) addMemberToGroup(groupCode)
            else Toast.makeText(this, "Enter full 6-digit code", Toast.LENGTH_SHORT).show()
        }
    }

    /** Sends join-request to backend */
    private fun addMemberToGroup(groupCode: String) {
        val userId = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            .getInt("userid", -1)

        val member = MemberEntity(userId, groupCode, "Member")

        RetrofitClient.getInstance()
            .getUserApiService()
            .addGroupMember(member)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@JoinCircleScreen,
                            "Joined group successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        val err = runCatching { response.errorBody()?.string() }
                            .getOrNull() ?: "HTTP ${response.code()}"
                        Toast.makeText(
                            this@JoinCircleScreen,
                            "Failed: $err",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(
                        this@JoinCircleScreen,
                        "Network error: ${t.localizedMessage ?: "Unknown"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /** Handles focus-jumping & submit-button state for 6 code boxes */
    private fun setupCodeInputs() {
        for (i in codeInputs.indices) {
            val current = codeInputs[i]

            current.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    when {
                        s?.length == 1 && i < codeInputs.lastIndex ->
                            codeInputs[i + 1].requestFocus()
                        s?.isEmpty() == true && i > 0 ->
                            codeInputs[i - 1].requestFocus()
                    }

                    // Enable/disable submit button
                    val ready = codeInputs.all { it.text.length == 1 }
                    binding.submitButton.isEnabled = ready
                    binding.submitButton.backgroundTintList = if (ready)
                        ColorStateList.valueOf(Color.parseColor("#0F6D4D"))
                    else
                        ContextCompat.getColorStateList(this@JoinCircleScreen, R.color.gray)
                }
            })
        }
    }
}
