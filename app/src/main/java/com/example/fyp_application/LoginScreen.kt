package com.example.fyp_application

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivityLoginScreenBinding
import com.example.fyp_application.dto.LoginUserEntity
import com.example.fyp_application.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginScreen : AppCompatActivity() {

    private lateinit var binding: ActivityLoginScreenBinding
    private lateinit var sharedPref: SharedPreferences
    private val TAG = "LoginScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Auto-login if already logged in
        if (sharedPref.getInt("userid", -1) > 0) {
            redirectToHomeScreen()
            return
        }

        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCheckBox()
        setupSignupTextView()

        // Initial button state
        setLoginButtonEnabled(false)

        // Enable login only when terms are accepted
        binding.checkBox2.setOnCheckedChangeListener { _, isChecked ->
            setLoginButtonEnabled(isChecked)
        }

        binding.appCompatButton.setOnClickListener {
            if (!binding.checkBox2.isChecked) {
                Toast.makeText(
                    this,
                    "Please accept the Terms and Conditions to continue",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val email = binding.editText.text.toString().trim()
            val password = binding.editText2.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }
    }

    private fun setLoginButtonEnabled(enabled: Boolean) {
        binding.appCompatButton.isEnabled = enabled
    }


    private fun redirectToHomeScreen() {
        val intent = Intent(this, HomeScreen::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.editText.error = "Email is required"
                binding.editText.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editText.error = "Please enter a valid email"
                binding.editText.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.editText2.error = "Password is required"
                binding.editText2.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        setLoginButtonEnabled(false)
        binding.appCompatButton.text = "Logging in..."

        val loginUser = LoginUserEntity(email, password)
        Log.d(TAG, "Attempting login for: $email")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = RetrofitClient.getInstance().getUserApiService().loginUser(loginUser)
                val response = call.execute()

                withContext(Dispatchers.Main) {
                    binding.appCompatButton.text = "Login"
                    setLoginButtonEnabled(binding.checkBox2.isChecked)

                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!

                        with(sharedPref.edit()) {
                            putInt("userid", user.userId)
                            putString("userName", user.name)
                            putString("userEmail", user.email)
                            apply()
                        }

                        Log.d(TAG, "Login successful, redirecting to Home.")
                        redirectToHomeScreen()
                    } else {
                        Log.w(TAG, "Login failed: ${response.message()}")
                        Toast.makeText(
                            this@LoginScreen,
                            "Invalid email or password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.appCompatButton.text = "Login"
                    setLoginButtonEnabled(binding.checkBox2.isChecked)
                    Toast.makeText(
                        this@LoginScreen,
                        "Network error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun setupCheckBox() {
        val checkBox: CheckBox = binding.checkBox2
        val fullText = "By creating an account, you agree to our terms and condition"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Optional: Launch Terms and Conditions Activity
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@LoginScreen, R.color.teal_700)
                ds.isUnderlineText = true
            }
        }

        val startIndex = fullText.indexOf("terms and condition")
        val endIndex = startIndex + "terms and condition".length

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        checkBox.text = spannableString
        checkBox.movementMethod = LinkMovementMethod.getInstance()
        checkBox.highlightColor = Color.TRANSPARENT
    }

    private fun setupSignupTextView() {
        val fullText = "Don't have an account? Signup"
        val spannableString = SpannableString(fullText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginScreen, SignUp::class.java))
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@LoginScreen, R.color.teal_700)
                ds.isUnderlineText = true
            }
        }

        val startIndex = fullText.indexOf("Signup")
        val endIndex = startIndex + "Signup".length

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.textViewSignup.text = spannableString
        binding.textViewSignup.movementMethod = LinkMovementMethod.getInstance()
        binding.textViewSignup.highlightColor = Color.TRANSPARENT
    }
}
