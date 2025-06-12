package com.example.livingcosttracker
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.livingcosttracker.converter.Converters
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.livingcosttracker.db.AppDatabase
import com.example.livingcosttracker.db.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.log

class ProfileActivity : AppCompatActivity(){
    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val inputNameSection = findViewById<EditText>(R.id.inputUsername)
        val inputIncomeMonthlySection = findViewById<EditText>(R.id.inputIncomeMonthly)
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val submitButton = findViewById<MaterialButton>(R.id.submitUpdateProfile)

        val converters = Converters()
        lateinit var user: User
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@ProfileActivity)
            user = db.userDao().getUser()
            inputNameSection.setText(user.username)
            val incomeFormatted = converters.formatRupiah(user.fix_monthly_income)

            var userPercentageSaving = String.format("%.1f", user.savingPercentage).toFloat()
            for (i in 0 until toggleGroup.childCount) {
                val button = toggleGroup.getChildAt(i) as MaterialButton
                val tagValue = button.tag.toString().toFloatOrNull()
                Log.d("tag", tagValue.toString())
                Log.d("saving", userPercentageSaving.toString())

                if (tagValue == userPercentageSaving) {
                    button.isChecked = true
                }
            }

            inputIncomeMonthlySection.setText(incomeFormatted)
            inputIncomeMonthlySection.setSelection(inputIncomeMonthlySection.text.length) // move cursor to end

            inputIncomeMonthlySection.addTextChangedListener(object : TextWatcher {
                var isEditing = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (isEditing) return

                    isEditing = true
                    val original = s.toString()

                    // Remove "Rp. " and all non-digit characters
                    val clean = original.replace("Rp. ", "").replace(".", "").trim()

                    if (clean.isNotEmpty()) {
                        try {
                            val formatted = converters.formatCurrency(clean.toInt())
                            inputIncomeMonthlySection.setText("Rp. $formatted")
                        } catch (e: NumberFormatException) {
                            inputIncomeMonthlySection.setText("Rp. ")
                        }
                    } else {
                        // When all numbers are deleted, keep "Rp. "
                        inputIncomeMonthlySection.setText("Rp. ")
                    }

                    // Move cursor to the end
                    inputIncomeMonthlySection.setSelection(inputIncomeMonthlySection.text.length)
                    isEditing = false
                }

            })


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
        submitButton.setOnClickListener() {

            val username = inputNameSection.text.toString()

            val incomeString = inputIncomeMonthlySection.text.toString()
            if(incomeString == "Rp. ") {
                Toast.makeText(this, "Please Fill Your Income", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val income = inputIncomeMonthlySection.text.toString().replace("Rp. ", "")
                .replace(".", "")
                .toInt()

            if (username == ""){
                Toast.makeText(this, "Fill Username Field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(income == null || income == 0) {
                Toast.makeText(this, "0 Income is not allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedButtonId = toggleGroup.checkedButtonId
            val selectedButton = findViewById<MaterialButton>(selectedButtonId)
            val newSavingPercentage = selectedButton.tag.toString().toFloat()

            try {
                val db = AppDatabase.getDatabase(this)
                lifecycleScope.launch {
                    if(username != user.username) db.userDao().updateUsername(username)
                    if (income != user.fix_monthly_income) db.userDao().updateUserMonthlyIncome(income)
                    if (newSavingPercentage != user.savingPercentage) db.userDao().updateUserSavingPercentage(newSavingPercentage)
                }
            } catch (e: IOException) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val navToMain = Intent(this, MainActivity::class.java)
            startActivity(navToMain)  // Start the activity
        }



    }
}