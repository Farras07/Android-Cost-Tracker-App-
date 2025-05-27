package com.example.livingcosttracker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.livingcosttracker.db.AppDatabase
import com.example.livingcosttracker.db.User
import com.example.livingcosttracker.ui.home.Custom_Card_Component
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.*
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val inputIncome = findViewById<EditText>(R.id.income)
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@RegisterActivity)
            val user = db.userDao().getUser()

            if(user?.id != null) {
                val navToMain = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(navToMain)  // Start the activity
            }

        }

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            for (i in 0 until group.childCount) {
                val button = group.getChildAt(i) as MaterialButton
                println(isChecked)
                if (button.id == checkedId) {
                    println(button.id)
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedButtonColor))
                } else {
                    button.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedButtonColor))

                }
            }
        }


        val btnSubmit = findViewById<Button>(R.id.submit)
        btnSubmit.setOnClickListener {
            // Handle button click here
            val inputNameElement = findViewById<EditText>(R.id.name)
            val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
            val name = inputNameElement.text.toString()
            val income = inputIncome.text.toString().toIntOrNull()

            if(income == null || income == 0) {
                Toast.makeText(this, "0 Income is not allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedButtonId = toggleGroup.checkedButtonId
            if (selectedButtonId == View.NO_ID) {
                Toast.makeText(this, "Please select a savings percentage", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedButton = findViewById<MaterialButton>(selectedButtonId)
            val savingPercentage = selectedButton.tag.toString().toFloat()
            try {
                val db = AppDatabase.getDatabase(this)
                val user = User(username = name, fix_monthly_income = income, savingPercentage = savingPercentage, balance = 0)
                CoroutineScope(Dispatchers.IO).launch {
                    db.userDao().insertUser(user)
                }
            } catch (e:  IOException) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val navToMain = Intent(this, MainActivity::class.java)
            startActivity(navToMain)  // Start the activity
        }
    }
}
