package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var userRef: DatabaseReference
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etAddress: EditText
    private lateinit var etEmail: EditText
    private lateinit var tvWelcome: TextView
    private lateinit var tvRole: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {            // safety check
            logout()
            return
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etAddress = findViewById(R.id.etAddress)
        etEmail = findViewById(R.id.etEmail)
        tvWelcome = findViewById(R.id.tvWelcome)
        tvRole = findViewById(R.id.tvRole)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        loadProfile()

        btnSave.setOnClickListener { saveProfile() }
        btnLogout.setOnClickListener { logout() }
    }

    // Read the profile (JSON) from Realtime Database and fill the views
    private fun loadProfile() {
        userRef.get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserModel::class.java)
                if (user != null) {
                    etName.setText(user.name)
                    etPhone.setText(user.phone)
                    etAddress.setText(user.address)
                    etEmail.setText(user.email)
                    tvWelcome.text = "Welcome, ${user.name}"
                    tvRole.text = "Role: ${user.role}"
                } else {
                    Toast.makeText(this, "No profile data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Update only the editable fields (keeps role/email/uid untouched)
    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val address = etAddress.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and phone cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "address" to address
        )

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                tvWelcome.text = "Welcome, $name"
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}