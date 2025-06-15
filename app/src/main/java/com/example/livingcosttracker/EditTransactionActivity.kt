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
import com.example.livingcosttracker.db.Cashflow
import androidx.lifecycle.lifecycleScope
import com.example.livingcosttracker.db.AppDatabase
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity() {


    private lateinit var etTotal: TextInputEditText
    private lateinit var cashflowCategorySpinner: Spinner
    private lateinit var spinnerItemCategory: Spinner
    private lateinit var etDate: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button


    private var originalCashflowType: String? = null
    private var originalItemCategory: String? = null
    private var transactionId: Int = -1
    private var idUser: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)


        etTotal = findViewById(R.id.etTotal)
        cashflowCategorySpinner = findViewById(R.id.cashlowCategorySpinner)
        spinnerItemCategory = findViewById(R.id.spinnerItemCategory)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)


        val amountString = intent.getStringExtra("amount")
        val CashflowCategoryType = intent.getStringExtra("type")
        val itemcategory = intent.getStringExtra("itemCategory")
        val totalStringFromIntent = intent.getStringExtra("total")
        originalCashflowType = intent.getStringExtra("category")
        originalItemCategory = intent.getStringExtra("itemCategory")
        val date = intent.getStringExtra("date")
        val description = intent.getStringExtra("description")
        transactionId = intent.getIntExtra("id", -1)
        idUser = intent.getIntExtra("idUser", -1)

        val db = AppDatabase.getDatabase(this)


        if (idUser == -1) {
            Toast.makeText(this, "ID Pengguna tidak ditemukan. Gagal memuat data.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val cleanTotalString = totalStringFromIntent?.replace("Rp. ", "")?.replace(".", "")?.replace(",", ".")
        etTotal.setText(cleanTotalString)
        etDate.setText(date)
        etDescription.setText(description)

        val cashflowCategories = listOf("Categories", "Income", "Cost")
        val itemCategoriesMap = mapOf(
            "Categories" to listOf("Item Categories"),
            "Income" to listOf("Salary", "Freelance" ,"Others"),
            "Cost" to listOf("Food&Drink", "Rent", "Transportation","Investment","Others")
        )

        val cashflowCategoryAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cashflowCategories) {
            override fun isEnabled(position: Int) = position != 0
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
        cashflowCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cashflowCategorySpinner.adapter = cashflowCategoryAdapter

        val initialCashflowCategoryIndex = cashflowCategories.indexOf(originalCashflowType)
        if (initialCashflowCategoryIndex != -1) {
            cashflowCategorySpinner.setSelection(initialCashflowCategoryIndex)
        } else {
            cashflowCategorySpinner.setSelection(0)
        }


        val initialItemCategoryList = itemCategoriesMap[originalCashflowType]?.toMutableList() ?: mutableListOf("Item Categories")
        val itemCategoryAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, initialItemCategoryList) {
            override fun isEnabled(position: Int) = position != 0
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
        itemCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerItemCategory.adapter = itemCategoryAdapter

        val initialItemCategoryIndex = initialItemCategoryList.indexOf(originalItemCategory)
        if (initialItemCategoryIndex != -1) {
            spinnerItemCategory.setSelection(initialItemCategoryIndex)
        } else {
            spinnerItemCategory.setSelection(0)
        }

        cashflowCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = cashflowCategories[position]
                val newItems = itemCategoriesMap[selectedCategory]?.toMutableList() ?: mutableListOf("Item Categories")

                itemCategoryAdapter.clear()
                itemCategoryAdapter.addAll(newItems)
                itemCategoryAdapter.notifyDataSetChanged()

                if (selectedCategory == originalCashflowType) {
                    val index = newItems.indexOf(originalItemCategory)
                    if (index != -1) {
                        spinnerItemCategory.setSelection(index)
                    } else {
                        spinnerItemCategory.setSelection(0)
                    }
                } else {
                    spinnerItemCategory.setSelection(0)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (etDate.text.toString().isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    calendar.time = sdf.parse(etDate.text.toString()) ?: calendar.time
                } catch (e: Exception) {
                    Log.e("EditTransactionActivity", "Error parsing existing date: ${e.message}")
                }
            }

            val datePicker = DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                etDate.setText(sdf.format(selectedDate.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val newTotalString = etTotal.text?.toString().orEmpty()
            val newCashflowType = cashflowCategorySpinner.selectedItem?.toString().orEmpty()
            val newItemCategory = spinnerItemCategory.selectedItem?.toString().orEmpty()
            val newDateString = etDate.text?.toString().orEmpty()
            val newDescription = etDescription.text?.toString().orEmpty()

            val cleanedTotalString = newTotalString.replace("Rp. ", "").replace(".", "").replace(",", ".")
            val newTotalDouble = cleanedTotalString.toDoubleOrNull()

            if (newTotalDouble == null || newTotalDouble <= 0) {
                Toast.makeText(this, "Total Invalid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newTotalInt = newTotalDouble.toInt()

            if (newCashflowType == "Categories" || newItemCategory == "Item Categories" || newDateString.isBlank()) {
                Toast.makeText(this, "Fill All!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (transactionId != -1) {
                lifecycleScope.launch {
                    try {
                        val parsedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(newDateString) ?: Date()

                        val updatedCashflow = Cashflow(
                            id = transactionId,
                            idUser = idUser,
                            total = newTotalInt,
                            category = newCashflowType,
                            itemCategory = newItemCategory,
                            date = parsedDate,
                            description = newDescription
                        )

                        db.cashflowDao().updateCashflow(updatedCashflow)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditTransactionActivity, "Successfully Updated!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } catch (e: Exception) {
                        Log.e("EditTransactionActivity", "Failed to Update Transaction: ${e.message}", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditTransactionActivity, "Failed to Update Transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "ID Transaction Not Found", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "ID Transaction Not Found", Toast.LENGTH_SHORT).show()
            }
        }
    }
}