package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val rbSeller = findViewById<RadioButton>(R.id.rbSeller)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvGoLogin = findViewById<TextView>(R.id.tvGoLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val role = if (rbSeller.isChecked) "Seller" else "Buyer"

            // ---- Validation ----
            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                toast("Please fill name, phone, email and password")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                toast("Invalid email format")
                return@setOnClickListener
            }
            if (password.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            btnRegister.isEnabled = false

            // 1) Create the account in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid
                    val user = UserModel(uid, name, email, phone, address, role)

                    // 2) Save the profile in Realtime Database under users/<UID>
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener {
                            toast("Account created successfully")
                            startActivity(Intent(this, HomeActivity::class.java))
                            finishAffinity()
                        }
                        .addOnFailureListener {
                            btnRegister.isEnabled = true
                            toast("Saved account but failed to save profile: ${it.message}")
                        }
                }
                .addOnFailureListener {
                    btnRegister.isEnabled = true
                    toast("Registration failed: ${it.message}")
                }
        }

        tvGoLogin.setOnClickListener { finish() }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}