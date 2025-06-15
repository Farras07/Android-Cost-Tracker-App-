package com.example.livingcosttracker

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.livingcosttracker.db.AppDatabase
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var cashflowCategorySpinner: Spinner
    private lateinit var spinnerItemCategory: Spinner
    private lateinit var etDate: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Bind views
        etAmount = findViewById(R.id.etAmount)
        cashflowCategorySpinner = findViewById(R.id.cashlowCategorySpinner)
        spinnerItemCategory = findViewById(R.id.spinnerItemCategory)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)


        val amountString = intent.getStringExtra("amount")
        val CashflowCategoryType = intent.getStringExtra("type")
        val itemcategory = intent.getStringExtra("itemCategory")
        val date = intent.getStringExtra("date")
        val description = intent.getStringExtra("description")
        val transactionId = intent.getIntExtra("id", -1)

        val db = AppDatabase.getDatabase(this)


        etAmount.setText(amountString)
        etDate.setText(date)
        etDescription.setText(description)

        val cashflowCategories = listOf("Categories", "Income", "Cost")
        val itemCategoriesMap = mapOf(
            "Categories" to listOf("Item Categories"),
            "Income" to listOf("Salary", "Freelance" ,"Others"),
            "Cost" to listOf("Food&Drink", "Rent", "Transportation","Investment","Others")
        )
        val adapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            cashflowCategories
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

        val indexCurrentCategories = cashflowCategories.indexOf(CashflowCategoryType)
        if (indexCurrentCategories == -1) {
            Toast.makeText(this, "index categories not found", Toast.LENGTH_SHORT).show()
        }

        var currentItems = itemCategoriesMap[cashflowCategories.getOrNull(indexCurrentCategories)]?.toMutableList() ?: mutableListOf()
        val itemCategoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currentItems)
        itemCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cashflowCategorySpinner.adapter = adapter
        cashflowCategorySpinner.setSelection(indexCurrentCategories)
        spinnerItemCategory.adapter = itemCategoryAdapter

        cashflowCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = cashflowCategories[position]
                val newItems = itemCategoriesMap[selectedCategory]?.toMutableList() ?: mutableListOf()

                itemCategoryAdapter.clear()
                itemCategoryAdapter.addAll(newItems)
                itemCategoryAdapter.notifyDataSetChanged()
                val itemIndex = currentItems.indexOf(itemcategory)
                spinnerItemCategory.setSelection(itemIndex)

                if (itemIndex == -1) {
                    spinnerItemCategory.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Optional: handle no selection
            }
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val existingDateText = etDate.text.toString()
            if (existingDateText.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    calendar.time = sdf.parse(existingDateText) ?: calendar.time
                } catch (e: Exception) {
                }
            }

            val datePicker = DatePickerDialog(this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etDate.setText(sdf.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val newAmountString = etAmount.text.toString()
            val newCategory = spinnerItemCategory.selectedItem.toString()
            val newDate = etDate.text.toString()
            val newDescription = etDescription.text.toString()

            val newAmountDouble = newAmountString.replace("Rp. ", "").replace(".", "").toDoubleOrNull() // Hapus format Rp. dan titik sebelum konversi

            if (newAmountDouble == null) {
                Toast.makeText(this, "Jumlah tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newCategory.isBlank() || newDate.isBlank()) {
                Toast.makeText(this, "Mohon lengkapi semua bidang wajib", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (transactionId != -1) {
                Toast.makeText(this, "Update clicked for ID: $transactionId", Toast.LENGTH_SHORT).show()

                // val updatedCashflow = CashflowActivity(
                //     id = transactionId,
                //     amount = newAmountDouble,
                //     // type = newType,
                //     category = newCategory,
                //     itemCategory = newCategory,
                //     date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(newDate) ?: Date(),
                //     description = newDescription
                // )
                // CashflowDao.update(updatedCashflow)
                // finish()
            } else {
                Toast.makeText(this, "ID transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            if (transactionId != -1) {
                try {
                    lifecycleScope.launch {
                        db.cashflowDao().deleteCashflowById(transactionId.toString())
                    }
                    Toast.makeText(this,"Deleted Cashflow Successfully" , Toast.LENGTH_SHORT).show()
                    finish()
                }
                catch(e: IOException) {
                    Toast.makeText(this, "Deleted Unsuccessfully", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            } else {
                Toast.makeText(this, "ID transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}