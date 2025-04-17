package com.example.fyp_application

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fyp_application.databinding.ActivityLoginScreenBinding

class LoginScreen : AppCompatActivity() {

    // Declare the binding variable
    private lateinit var binding: ActivityLoginScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)

        // Set the content view to the root of the binding object
        setContentView(binding.root)

        // Set up the CheckBox with clickable "terms and condition" text
        setupCheckBox()

        // Set up the TextView with clickable "Signup" text
        setupSignupTextView()

        // Set OnClickListener for the Login Button
        binding.appCompatButton.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
        }
    }

    private fun setupCheckBox() {
        val checkBox: CheckBox = binding.checkBox2
        val checkBoxText = "By creating an account, you agree to our terms and condition"
        val checkBoxSpannableString = SpannableString(checkBoxText)

        val checkBoxStartIndex = checkBoxText.indexOf("terms and condition")
        val checkBoxEndIndex = checkBoxStartIndex + "terms and condition".length

        val checkBoxClickableSpan = object : ClickableSpan() {
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
            checkBoxClickableSpan,
            checkBoxStartIndex,
            checkBoxEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        checkBox.text = checkBoxSpannableString
        checkBox.movementMethod = LinkMovementMethod.getInstance()
        checkBox.highlightColor = Color.TRANSPARENT
    }

    private fun setupSignupTextView() {
        val textViewSignup = binding.textViewSignup
        val signupText = "Don't have an account? Signup"
        val signupSpannableString = SpannableString(signupText)

        val signupStartIndex = signupText.indexOf("Signup")
        val signupEndIndex = signupStartIndex + "Signup".length

        val signupClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginScreen, SignUp::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@LoginScreen, R.color.teal_700)
                ds.isUnderlineText = true
            }
        }

        signupSpannableString.setSpan(
            signupClickableSpan,
            signupStartIndex,
            signupEndIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textViewSignup.text = signupSpannableString
        textViewSignup.movementMethod = LinkMovementMethod.getInstance()
        textViewSignup.highlightColor = Color.TRANSPARENT
    }
}
