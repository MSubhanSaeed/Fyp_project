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
    private lateinit var sharPref: SharedPreferences
    private val TAG = "LoginScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Redirect to Home if user is already logged in
        if (sharPref.getInt("userid", -1) > 0) {
            redirectToHomeScreen()
            return
        }

        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCheckBox()
        setupSignupTextView()

        binding.appCompatButton.setOnClickListener {
            val email = binding.editText.text.toString().trim()
            val password = binding.editText2.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }
    }

    private fun redirectToHomeScreen() {
        val intent = Intent(this, HomeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.editText.error = "Email is required"
            binding.editText.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.editText2.error = "Password is required"
            binding.editText2.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editText.error = "Please enter a valid email"
            binding.editText.requestFocus()
            return false
        }

        return true
    }

    private fun loginUser(email: String, password: String) {
        binding.appCompatButton.isEnabled = false
        binding.appCompatButton.text = "Logging in..."

        val loginUser = LoginUserEntity(email, password)
        Log.d(TAG, "Sending login request with email: $email")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = RetrofitClient.getInstance()
                    .getUserApiService()
                    .loginUser(loginUser)

                val response = call.execute()

                withContext(Dispatchers.Main) {
                    binding.appCompatButton.isEnabled = true
                    binding.appCompatButton.text = "Login"

                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!

                        with(sharPref.edit()) {
                            putInt("userid", user.userId)
                            putString("userName", user.name)
                            putString("userEmail", user.email)
                            apply()
                        }

                        Log.d(TAG, "Login successful. User data saved.")
                        redirectToHomeScreen()
                    } else {
                        Log.d(TAG, "Login failed: ${response.message()}")
                        Toast.makeText(
                            this@LoginScreen,
                            "Invalid email or password",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    binding.appCompatButton.isEnabled = true
                    binding.appCompatButton.text = "Login"
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
        val checkBoxText = "By creating an account, you agree to our terms and condition"
        val checkBoxSpannableString = SpannableString(checkBoxText)

        val startIndex = checkBoxText.indexOf("terms and condition")
        val endIndex = startIndex + "terms and condition".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@LoginScreen, "Terms and Conditions clicked!", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@LoginScreen, R.color.teal_700)
                ds.isUnderlineText = true
            }
        }

        checkBoxSpannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        checkBox.text = checkBoxSpannableString
        checkBox.movementMethod = LinkMovementMethod.getInstance()
        checkBox.highlightColor = Color.TRANSPARENT
    }

    private fun setupSignupTextView() {
        val textViewSignup = binding.textViewSignup
        val signupText = "Don't have an account? Signup"
        val spannableString = SpannableString(signupText)

        val startIndex = signupText.indexOf("Signup")
        val endIndex = startIndex + "Signup".length

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

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textViewSignup.text = spannableString
        textViewSignup.movementMethod = LinkMovementMethod.getInstance()
        textViewSignup.highlightColor = Color.TRANSPARENT
    }
}
