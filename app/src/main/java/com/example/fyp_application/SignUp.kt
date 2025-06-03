package com.example.fyp_application

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivitySignUpBinding
import com.example.fyp_application.model.UserDto
import com.example.fyp_application.model.SignupResponse
import com.example.fyp_application.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginTextView: TextView = findViewById(R.id.loginText)
        val fullText = "If you have an account? Login"
        val spannableString = SpannableString(fullText)
        val loginStart = fullText.indexOf("Login")
        val loginEnd = loginStart + "Login".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@SignUp, LoginScreen::class.java))
            }
            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(this@SignUp, R.color.teal_700)
            }
        }

        spannableString.setSpan(clickableSpan, loginStart, loginEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginTextView.text = spannableString
        loginTextView.movementMethod = LinkMovementMethod.getInstance()

        binding.appCompatButton.setOnClickListener {
            val name = binding.editText.text.toString().trim()
            val email = binding.editText2.text.toString().trim()
            val password = binding.editText3.text.toString().trim()
            val confirmPassword = binding.editText4.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!binding.checkBox2.isChecked) {
                Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = UserDto(name, email, password)

            RetrofitClient.getInstance().getUserApiService().signUp(user).enqueue(object : Callback<SignupResponse> {
                override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                    if (response.isSuccessful) {
                        val message = response.body()?.message ?: "No message"
                        Log.d("API", "Signup response message: $message")

                        if (message.equals("True", ignoreCase = true)) {
                            startActivity(Intent(this@SignUp, LoginScreen::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@SignUp, "Signup failed: $message", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@SignUp, "Signup failed: ${response.code()}", Toast.LENGTH_LONG).show()
                        Log.e("API", "Signup failed with code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                    Toast.makeText(this@SignUp, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
                    Log.e("API", "Network error", t)
                }
            })
        }
    }
}
