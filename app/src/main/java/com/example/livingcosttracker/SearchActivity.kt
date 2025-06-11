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
import kotlinx.coroutines.withContext
import java.util.Calendar


class SearchActivity : AppCompatActivity() {
    private var selectedStartDate: String? = null
    private var selectedEndDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val categoriesSpinner = findViewById<Spinner>(R.id.categories)
        val itemCategoriesSpinner = findViewById<Spinner>(R.id.itemCategories)

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

                // Insert "All" at the top (default selection)
                if (selectedCategory != "Categories") {
                    newItems.add(0, "All")
                }

                itemCategoryAdapter.clear()
                itemCategoryAdapter.addAll(newItems)
                itemCategoryAdapter.notifyDataSetChanged()

                // Set selection to "All" by default
                itemCategoriesSpinner.setSelection(0)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No-op
            }
        }

        val categoryElement = findViewById<Spinner>(R.id.categories)
        val itemCategoryElement = findViewById<Spinner>(R.id.itemCategories)


        val submitAddFilter = findViewById<MaterialButton>(R.id.submitAddFilter)
        submitAddFilter.setOnClickListener{
            var categorySelected = categoryElement.selectedItem.toString()
            var itemCategorySelected = itemCategoryElement.selectedItem.toString()

            Log.d("categorySelected", categorySelected)


            var cashflowData: List<Cashflow>
            if (categorySelected != "Categories") {
                lifecycleScope.launch {
                    if (itemCategorySelected != "Item Categories") {
                        val intent = Intent(this@SearchActivity, CashflowActivity::class.java)
                        intent.putExtra("filterType", "itemCategories")
                        intent.putExtra("query", categorySelected)
                        startActivity(intent)
//                        cashflowData = getCashflowByItemCategories(itemCategorySelected)
//                        withContext(Dispatchers.Main){
//                            Log.d("cashflowData", cashflowData.toString())
//                            print(cashflowData)
//                        }
                    }
                    else {
                        val intent = Intent(this@SearchActivity, CashflowActivity::class.java)
                        intent.putExtra("filterType", "category")
                        intent.putExtra("query", categorySelected)
                        startActivity(intent)
//                        cashflowData = getCashflowByCategories(categorySelected)
//                        withContext(Dispatchers.Main){
//                            Log.d("cashflowData", cashflowData.toString())
//                            print(cashflowData)
//                        }
                    }


                }

            }

        }






    }
    private fun showDatePicker(dateType: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Save to the class-level variable
                if (dateType == "Start"){
                    this.selectedStartDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                    // Update button text
                    val btnPickDate = findViewById<MaterialButton>(R.id.pickStartDateButton)
                    btnPickDate.text = this.selectedStartDate
                }
                else {
                    this.selectedEndDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"

                    // Update button text
                    val btnPickDate = findViewById<MaterialButton>(R.id.pickEndDateButton)
                    btnPickDate.text = this.selectedEndDate
                }

            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private suspend fun getCashflowByCategories(category: String): List<Cashflow> {
        val db = AppDatabase.getDatabase(this@SearchActivity)
        return withContext(Dispatchers.IO) {
            db.cashflowDao().getCashflowByCategories(category)
        }
    }
    private suspend fun getCashflowByItemCategories(itemCategory: String): List<Cashflow> {
        val db = AppDatabase.getDatabase(this@SearchActivity)
        return withContext(Dispatchers.IO) {
            db.cashflowDao().getCashflowByItemCategories(itemCategory)
        }
    }


}


