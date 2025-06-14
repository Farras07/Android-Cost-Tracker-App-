package com.example.livingcosttracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var etType: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var etDate: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_transaction)

        // Bind views
        etAmount = findViewById(R.id.etAmount)
        etType = findViewById(R.id.edittype)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etDate = findViewById(R.id.etDate)
        etDescription = findViewById(R.id.etDescription)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)


        val amountString = intent.getStringExtra("amount")
        val type = intent.getStringExtra("type")
        val category = intent.getStringExtra("category")
        val date = intent.getStringExtra("date")
        val description = intent.getStringExtra("description")
        val transactionId = intent.getIntExtra("id", -1)


        etAmount.setText(amountString)
        etType.setText(type)
        etDate.setText(date)
        etDescription.setText(description)


        val categoryFromIntent = intent.getStringExtra("itemCategory") ?: category
        val categoryList = listOf("Food", "Transport", "Shopping", "Bills")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        spinnerCategory.adapter = adapter
        // Set selection based on retrieved category
        val selectedIndex = categoryList.indexOf(categoryFromIntent)
        if (selectedIndex >= 0) {
            spinnerCategory.setSelection(selectedIndex)
        } else {
            spinnerCategory.setSelection(0)
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
            val newType = etType.text.toString()
            val newCategory = spinnerCategory.selectedItem.toString()
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
                Toast.makeText(this, "Delete clicked for ID: $transactionId", Toast.LENGTH_SHORT).show()
                // CashflowDao.deleteById(transactionId)
                // finish()
            } else {
                Toast.makeText(this, "ID transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}