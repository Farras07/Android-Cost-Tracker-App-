package com.example.livingcosttracker
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.livingcosttracker.databinding.ActivityMainBinding
import com.example.livingcosttracker.db.AppDatabase
import com.example.livingcosttracker.db.User
import com.example.livingcosttracker.ui.home.Custom_Card_Component
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import android.widget.Spinner
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.DatePicker
import com.example.livingcosttracker.db.Cashflow
import java.util.Calendar


class AddActivity : AppCompatActivity() {
    private var selectedDate: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val mySwitch = findViewById<SwitchCompat>(R.id.mySwitch)
        val categoriesSpinner = findViewById<Spinner>(R.id.categories)
        val itemCategoriesSpinner = findViewById<Spinner>(R.id.itemCategories)
        val pickDateButton = findViewById<MaterialButton>(R.id.pickDateButton)
        val descriptionToggle = findViewById<EditText>(R.id.inputDescription)

        val amountEditText = findViewById<EditText>(R.id.inputMoney)

        amountEditText.setText("Rp. ")
        amountEditText.setSelection(amountEditText.text.length) // move cursor to end

        amountEditText.addTextChangedListener(object : TextWatcher {
            var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                isEditing = true
                val text = s.toString()

                // Prevent removing "Rp. "
                if (!text.startsWith("Rp. ")) {
                    amountEditText.setText("Rp. ")
                }

                // Ensure cursor stays at the end
                amountEditText.setSelection(amountEditText.text.length)
                isEditing = false
            }
        })


        val cashflowCategoriesItems = listOf("Categories", "Income", "Cost")
        val itemCategoriesMap = mapOf(
            "Categories" to listOf("Item Categories"),
            "Income" to listOf("Salary", "Freelance" ,"Others"),
            "Cost" to listOf("Food&Drink", "Rent", "Transportation","Investment","Others")
        )
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            cashflowCategoriesItems
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0 // Disable first (placeholder)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }

        var currentItems = itemCategoriesMap[cashflowCategoriesItems.getOrNull(0)]?.toMutableList() ?: mutableListOf()
        val itemCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentItems)
        itemCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriesSpinner.adapter = adapter
        categoriesSpinner.setSelection(0)
        itemCategoriesSpinner.adapter = itemCategoryAdapter


        categoriesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = cashflowCategoriesItems[position]
                val newItems = itemCategoriesMap[selectedCategory]?.toMutableList() ?: mutableListOf()

                itemCategoryAdapter.clear()
                itemCategoryAdapter.addAll(newItems)
                itemCategoryAdapter.notifyDataSetChanged()
                itemCategoriesSpinner.setSelection(0) // Reset selection if needed
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle no selection
            }
        }

        mySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                descriptionToggle.visibility = View.VISIBLE
            } else {
                descriptionToggle.visibility = View.GONE
            }
        }


        pickDateButton.setOnClickListener() {
            showDatePicker()
        }
        var submitAddCashflowElement = findViewById<Button>(R.id.submitAddCashflow)
        submitAddCashflowElement.setOnClickListener() {
            val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            val userId = sharedPref.getInt("userId",1)
            val total = findViewById<EditText>(R.id.inputMoney).text.toString().replace("Rp. ", "").toInt()
            var category = findViewById<Spinner>(R.id.categories).selectedItem.toString()
            var itemCategory = findViewById<Spinner>(R.id.itemCategories).selectedItem.toString()
            var description = findViewById<EditText>(R.id.inputDescription).text.toString()
            val dateString = selectedDate ?: ""
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date: Date = try {
                dateFormat.parse(dateString) ?: Date()
            } catch (e: Exception) {
                Date() // fallback to current date if parsing fails
            }
            try {
                val db = AppDatabase.getDatabase(this)
                val cashflow = Cashflow(total = total, category = category, itemCategory = itemCategory, description = description, date = date, idUser = userId)
                CoroutineScope(Dispatchers.IO).launch {
                    db.cashflowDao().insertCashflow(cashflow)
                    val balance = db.userDao().getUserBalance()
                    print(balance)
                    var newBalance = 0
                    if (category == "Income") {
                        newBalance = balance + total
                    } else {
                        newBalance = balance - total
                    }
                    db.userDao().updateUserBalanceById(newBalance)
                }
            } catch (e:  IOException) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val navToMain = Intent(this, MainActivity::class.java)
            startActivity(navToMain)  // Start the activity
        }



    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()

                // Optionally update button text
                val btnPickDate = findViewById<MaterialButton>(R.id.pickDateButton)
                btnPickDate.text = selectedDate
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
}


