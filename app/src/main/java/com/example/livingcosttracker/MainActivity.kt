package com.example.livingcosttracker
import com.example.livingcosttracker.converter.Converters
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.livingcosttracker.databinding.ActivityMainBinding
import com.example.livingcosttracker.db.AppDatabase
import com.example.livingcosttracker.db.User
import com.example.livingcosttracker.ui.home.Custom_Card_Component
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import com.example.livingcosttracker.ui.home.HeaderInfoUser
import java.util.Calendar
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val cardAddElement = findViewById<FloatingActionButton>(R.id.addCashflowButton)
        cardAddElement.setOnClickListener(){
            val navToAddCashflow = Intent(this, AddActivity::class.java)
            startActivity(navToAddCashflow)  // Start the activity

        }

//        val navView: BottomNavigationView = binding.navView

//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
        val headerCashflowCardContainer = findViewById<FrameLayout>(R.id.headerCashflowCardContainer)
        headerCashflowCardContainer.setOnClickListener() {
            val navToCashflowActivity = Intent(this, CashflowActivity::class.java)
            startActivity(navToCashflowActivity)  // Start the activity
        }

        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val converters = Converters()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Months are 0-based
            val formattedMonth = if (month < 10) "0$month" else "$month"
            val yearMonth = "$year-$formattedMonth"


            val db = AppDatabase.getDatabase(this@MainActivity)
            val user = db.userDao().getUser()
            val balance = user.balance
            val balanceThisMonth = db.cashflowDao().getBalanceThisMonth(yearMonth) ?: 0
            val balanceThisMonthFormatted = converters.formatRupiah(balanceThisMonth)

            val userInfoText = findViewById<TextView>(R.id.userInfoText)
            userInfoText.text = "${user.username}"

            val headerName = findViewById<TextView>(R.id.headerTextContent)
            headerName.text = "Hola! ${user.username}"

//            val balanceSection = findViewById<TextView>(R.id.balance)
//            balanceSection.text = "Rp. ${balance}"

            val balanceThisMonthSection = findViewById<TextView>(R.id.balanceThisMonth)

            balanceThisMonthSection.text = balanceThisMonthFormatted

            val toSpendSection = findViewById<TextView>(R.id.toSpendSection)
            var userPercentageSaving = String.format("%.1f", user.savingPercentage).toFloat()
            var userSaving = (user.fix_monthly_income * userPercentageSaving).toInt()
            var moneyToSpend = 0

            if (balanceThisMonth != 0) {
                moneyToSpend = balanceThisMonth - userSaving
            }

            if (balanceThisMonth < userSaving && balanceThisMonth != 0){
                toSpendSection.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.redLips))
            }
            if (balanceThisMonth == userSaving || balanceThisMonth == 0) {
                toSpendSection.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))

            }


//            val userMoneyToSpend = user.fix_monthly_income * user.savingPercentage


            val savingCard = findViewById<Custom_Card_Component>(R.id.savingCard)
            val expenseCard = findViewById<Custom_Card_Component>(R.id.expenseCard)

            val userExpense = db.cashflowDao().getCashflowExpenseByMonth(yearMonth) ?: 0
            val userExpenseFormatted = converters.formatRupiah(userExpense)
            expenseCard.setCardContent(userExpenseFormatted)

//            val savingPercentage = user.savingPercentage * 100
            val userSavingFormatted = converters.formatRupiah(userSaving)

            savingCard.setCardContent(userSavingFormatted)
            if (userSaving > balanceThisMonth) {
                savingCard.setTextColor(R.color.redLips, this@MainActivity) // `this` is the context (e.g., Activity)

            }
            val moneyToSpendFormatted = converters.formatRupiah(moneyToSpend)

            toSpendSection.text = moneyToSpendFormatted

            val userInfoState = getSharedPreferences("auth_prefs", MODE_PRIVATE)
            userInfoState.edit().apply {
                putInt("userId", user.id)
                apply()
            }
            val infoCardText = findViewById<TextView>(R.id.infoCardText)
            val cashflowContainerCards = findViewById<LinearLayout>(R.id.cashflowCardContainer)
            val inflater = LayoutInflater.from(this@MainActivity)
            val cashflowList = db.cashflowDao().getAllCashflow().first().reversed()

            if (cashflowList.isNotEmpty()) {
                infoCardText.visibility = View.GONE
                val highlightsCashflow = cashflowList.take(3)
                highlightsCashflow.forEach { item ->
                    val cardView = inflater.inflate(R.layout.component_card_cashflow, cashflowContainerCards, false)
                    cardView.id = item.id
                    val cardTitle = cardView.findViewById<TextView>(R.id.cardCashlowTitleText)
                    val cardTotal = cardView.findViewById<TextView>(R.id.cardCashlowTotalText)
                    val cardDesc = cardView.findViewById<TextView>(R.id.cardCashlowDescText)
                    val cardItemCategory = cardView.findViewById<TextView>(R.id.cardCashlowCategoryText)
                    val cardIcon = cardView.findViewById<ImageView>(R.id.cardCashlowIcon)

                    cardTitle.text = "${item.category}"
                    cardItemCategory.text = "${item.itemCategory}"
                    val totalFormatted = converters.formatRupiah(item.total)

                    if (item.category != "Income") {
                        cardTotal.text = "-${totalFormatted}"
                        cardTotal.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.redLips))
                    }
                    else {
                        cardTotal.text = "+${totalFormatted}"
                        cardTotal.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.greenMoney))
                    }

                    if (item.description == "") cardDesc.text = "No desc"
                    else cardDesc.text = "${item.description}"


                    if (item.category != "Income") {
                        cardIcon.setImageResource(R.drawable.group)
                    }
                    cashflowContainerCards.addView(cardView)
                }
            }


        }
    }

}