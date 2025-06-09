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


class SearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)





    }
}


