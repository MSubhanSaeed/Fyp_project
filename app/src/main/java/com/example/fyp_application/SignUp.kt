package com.example.fyp_application

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivitySignUpBinding
import com.example.fyp_application.model.UserDto
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

        setupCheckBox()

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

            val call = RetrofitClient.getInstance().getUserApiService().signUp(user)
            call.enqueue(object : Callback<Int> {
                override fun onResponse(call: Call<Int>, response: Response<Int>) {
                    if (response.isSuccessful) {
                        // Assuming the API returns 1 for success, 0 for failure
                        if (response.body() == 1) {
                            Toast.makeText(this@SignUp, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignUp, LoginScreen::class.java))
                        } else {
                            Toast.makeText(this@SignUp, "Sign Up Failed. Server returned failure code.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignUp, "Failed: ${response.code()} - ${response.message()}", Toast.LENGTH_LONG).show()
                        val errorBody = response.errorBody()?.string()
                        Log.e("SignUp", "Error Response: $errorBody")
                    }
                }

                override fun onFailure(call: Call<Int>, t: Throwable) {
                    val errorMsg = "Network Failure: ${t.message}"
                    Toast.makeText(this@SignUp, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("SignUp", errorMsg, t)
                }
            })
        }
    }

    private fun setupCheckBox() {
        val checkBox = binding.checkBox2
        val text = "By creating an account, you agree to our \nTerms and Conditions"
        val spannableString = SpannableString(text)

        val start = text.indexOf("Terms and Conditions")
        val end = start + "Terms and Conditions".length

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@SignUp, "Terms and Conditions clicked!", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignUp, R.color.teal_700)
                ds.isUnderlineText = true
            }
        }

        spannableString.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        checkBox.text = spannableString
        checkBox.movementMethod = LinkMovementMethod.getInstance()
        checkBox.highlightColor = Color.TRANSPARENT
    }
}
