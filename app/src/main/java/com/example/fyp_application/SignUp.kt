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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fyp_application.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {
    // Declare the binding variable
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        // Set the content view to the root of the binding object
        setContentView(binding.root)

        // Set up the CheckBox with clickable "Terms and Conditions" text
        setupCheckBox()

        // Set OnClickListener for the SignUp Button
        binding.appCompatButton.setOnClickListener {
            // Navigate to the HomeScreen
            val intent = Intent(this, LoginScreen::class.java)
            startActivity(intent)
        }
    }

    private fun setupCheckBox() {
        // Access the CheckBox using View Binding
        val checkBox: CheckBox = binding.checkBox2

        // Full text for CheckBox
        val checkBoxText = "By creating an account, you agree to our \nTerms and Conditions"

        // Create a SpannableString for CheckBox
        val checkBoxSpannableString = SpannableString(checkBoxText)

        // Define the start and end index of the clickable text for CheckBox
        val checkBoxStartIndex = checkBoxText.indexOf("Terms and Conditions")
        val checkBoxEndIndex = checkBoxStartIndex + "Terms and Conditions".length

        // Make the text clickable for CheckBox
        val checkBoxClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Display a Toast message
                Toast.makeText(this@SignUp, "Terms and Conditions clicked!", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                // Customize the appearance of the clickable text
                ds.color = ContextCompat.getColor(this@SignUp, R.color.teal_700) // Set text color
                ds.isUnderlineText = true // Add underline
            }
        }

        // Apply the ClickableSpan to the specific part of the text for CheckBox
        checkBoxSpannableString.setSpan(checkBoxClickableSpan, checkBoxStartIndex, checkBoxEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString to the CheckBox
        checkBox.text = checkBoxSpannableString

        // Enable clicking on the text for CheckBox
        checkBox.movementMethod = LinkMovementMethod.getInstance()

        // Disable highlighting of the text when clicked (optional) for CheckBox
        checkBox.highlightColor = Color.TRANSPARENT
    }
}