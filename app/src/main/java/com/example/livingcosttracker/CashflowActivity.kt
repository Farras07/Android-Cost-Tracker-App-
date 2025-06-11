    package com.example.livingcosttracker
    import java.text.SimpleDateFormat
    import java.util.*
    import java.time.LocalDate
    import java.time.format.DateTimeFormatter
    import android.content.Intent
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.view.GestureDetector
    import android.view.MotionEvent
    import android.view.View
    import android.view.ViewGroup
    import android.animation.ValueAnimator
    import android.graphics.Typeface
    import android.os.Build
    import android.util.Log
    import android.view.LayoutInflater
    import android.widget.*
    import androidx.annotation.RequiresApi
    import androidx.appcompat.app.AppCompatActivity
    import androidx.cardview.widget.CardView
    import androidx.core.content.ContextCompat
    import androidx.lifecycle.lifecycleScope
    import com.example.livingcosttracker.converter.Converters
    import com.example.livingcosttracker.db.AppDatabase
    import com.example.livingcosttracker.ui.home.Custom_Card_Component
    import com.example.livingcosttracker.db.User
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.button.MaterialButtonToggleGroup
    import kotlinx.coroutines.*
    import java.io.IOException

    class CashflowActivity : AppCompatActivity() {

        private var originalHeight: Int = 0
        private var originalTranslationY: Float = 0f
        private var isExpanded = false

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_cashflow)

            val mockCardCashflowList = findViewById<LinearLayout>(R.id.mockCardCashflowDateCategoryContainer)
            mockCardCashflowList.visibility = View.GONE
            val converters = Converters()


            val mockFilterCard = findViewById<LinearLayout>(R.id.mockFilter)
            mockFilterCard.visibility = View.GONE

            val cashflowSearch = findViewById<LinearLayout>(R.id.search_button)
            cashflowSearch.setOnClickListener(){
                val navToSearchPage = Intent(this, SearchActivity::class.java)
                startActivity(navToSearchPage)  // Start the activity
            }


            val cashflowSectionView = findViewById<CardView>(R.id.cashflowListSection)
            val cashflowFilterSectionView = findViewById<LinearLayout>(R.id.cashflowFilterSection)
            val inflater = LayoutInflater.from(this)
            val cashflowListContainerElement = findViewById<LinearLayout>(R.id.cashflowContentContainer)
            val headerTextElement = findViewById<TextView>(R.id.headerTextContent)
            val expenseCard = findViewById<Custom_Card_Component>(R.id.expenseCard)
            val balanceCard = findViewById<Custom_Card_Component>(R.id.balanceCard)

            val filterMonthScroll = findViewById<HorizontalScrollView>(R.id.filterMonthScroll)
            filterMonthScroll.post {
                filterMonthScroll.fullScroll(View.FOCUS_RIGHT)
            }


            var selectedFilterCard: View? = null

            lifecycleScope.launch{
                val filter: String? = intent.getStringExtra("filterType")
                if (filter != null) {

                }
                else {
                    val db = AppDatabase.getDatabase(this@CashflowActivity)
                    val yearMonthsList = db.cashflowDao().getYearMonthCashflow()
                    val currentDate = LocalDate.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
                    val formattedDate = currentDate.format(formatter)

                    yearMonthsList.forEach{ yearMonth ->
                        val filterMonthCard = inflater.inflate(R.layout.card_month_filter, cashflowFilterSectionView, false)
                        filterMonthCard.setTag(R.id.textFilterMonth, yearMonth)

                        val monthName = getOnlyRealMonthName(yearMonth, "MM-dd")
                        val textMonthSection = filterMonthCard.findViewById<TextView>(R.id.textFilterMonth)
                        textMonthSection.text = monthName
                        textMonthSection.setTypeface(null, Typeface.BOLD)

                        filterMonthCard.setOnClickListener { view ->
                            val tag = view.getTag(R.id.textFilterMonth) as String
                            val formattedMonthYear = changeYearMonthToMonthYear(tag)
                            headerTextElement.text = "Cashflow On $formattedMonthYear"

                            selectedFilterCard?.setBackgroundColor(android.graphics.Color.TRANSPARENT)

                            selectedFilterCard = view
                            view.setBackgroundColor(ContextCompat.getColor(this@CashflowActivity, R.color.orange)) // or any highlight

                            lifecycleScope.launch(Dispatchers.IO) {
                                val cashflowList = db.cashflowDao().getCashflowByYearMonth(tag)
                                var totalExpense = 0
                                var balance = 0

                                cashflowList.forEach{ cashflow ->
                                    if(cashflow.category == "Cost") {
                                        totalExpense += cashflow.total
                                        balance -= cashflow.total
                                    }
                                    else balance += cashflow.total

                                }
                                val totalExpenseFormatted = converters.formatRupiah(totalExpense)
                                val balanceFormatted = converters.formatRupiah(balance)

                                val sortedCashflows = cashflowList.sortedBy { it.date }
                                var previousFormattedDate: String? = null


                                withContext(Dispatchers.Main) {
                                    cashflowListContainerElement.removeAllViewsInLayout()
                                    expenseCard.setCardContent(totalExpenseFormatted)
                                    balanceCard.setCardContent(balanceFormatted)
                                }
                                sortedCashflows.forEach{cashflow ->
                                    withContext(Dispatchers.Main) {
                                        val currentFormattedDate = SimpleDateFormat("dd MMMM", Locale.ENGLISH).format(cashflow.date)
                                        val cashflowListContainer = inflater.inflate(R.layout.component_card_cashflow_list, cashflowListContainerElement, false)
                                        val cardDate = cashflowListContainer.findViewById<TextView>(R.id.cashflowCardDateText)

                                        cardDate.text = currentFormattedDate
                                        cashflowListContainerElement.addView(cashflowListContainer)

                                        if (currentFormattedDate == previousFormattedDate) cardDate.visibility = View.GONE
                                        else cardDate.visibility = View.VISIBLE

                                        previousFormattedDate = currentFormattedDate




                                        val cashflowCardListContainerElement = cashflowListContainer.findViewById<LinearLayout>(R.id.cashflowCardListContainer)
                                        val cashflowCardListContainer = inflater.inflate(R.layout.component_card_cashflow, cashflowCardListContainerElement, false)

                                        val cardTitle = cashflowCardListContainer.findViewById<TextView>(R.id.cardCashlowTitleText)
                                        val cardTotal = cashflowCardListContainer.findViewById<TextView>(R.id.cardCashlowTotalText)
                                        val cardDesc = cashflowCardListContainer.findViewById<TextView>(R.id.cardCashlowDescText)
                                        val cardItemCategory = cashflowCardListContainer.findViewById<TextView>(R.id.cardCashlowCategoryText)
                                        val cardIcon = cashflowCardListContainer.findViewById<ImageView>(R.id.cardCashlowIcon)

                                        cardTitle.text = "${cashflow.category}"
                                        cardItemCategory.text = "${cashflow.itemCategory}"
                                        val totalFormatted = converters.formatRupiah(cashflow.total)

                                        if (cashflow.category != "Income") {
                                            cardTotal.text = "-${totalFormatted}"
                                            cardTotal.setTextColor(ContextCompat.getColor(this@CashflowActivity, R.color.redLips))
                                        }
                                        else {
                                            cardTotal.text = "+${totalFormatted}"
                                            cardTotal.setTextColor(ContextCompat.getColor(this@CashflowActivity, R.color.greenMoney))
                                        }

                                        if (cashflow.description == "") cardDesc.text = "No desc"
                                        else cardDesc.text = "${cashflow.description}"


                                        if (cashflow.category != "Income") {
                                            cardIcon.setImageResource(R.drawable.group)
                                        }

                                        cashflowCardListContainerElement.addView(cashflowCardListContainer)


                                    }
                                }


                            }
                        }
                        if (yearMonth == formattedDate) {
                            filterMonthCard.performClick()
                        }

                        cashflowFilterSectionView.addView(filterMonthCard)
                    }

                }



            }

            // Save the original height and translationY after layout
            cashflowSectionView.post {
                originalHeight = cashflowSectionView.height
                originalTranslationY = cashflowSectionView.translationY
            }

            val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 != null && e2 != null) {
                        val deltaY = e1.y - e2.y
                        if (deltaY > 100 && Math.abs(velocityY) > 100) {
                            // Swipe up
                            expandCardToFullHeight(cashflowSectionView)
                            return true
                        } else if (deltaY < -100 && Math.abs(velocityY) > 100) {
                            // Swipe down
                            collapseCardToOriginalHeight(cashflowSectionView)
                            return true
                        }
                    }
                    return false
                }
            })

            cashflowSectionView.setOnTouchListener { v, event ->
                gestureDetector.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_UP) v.performClick()
                true
            }
        }

        private fun expandCardToFullHeight(view: CardView) {
            val params = view.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            view.layoutParams = params
            view.translationY = 0f
            isExpanded = true
        }

        private fun collapseCardToOriginalHeight(view: CardView) {
            val params = view.layoutParams
            params.height = originalHeight
            view.layoutParams = params
            view.translationY = originalTranslationY
            isExpanded = false
        }
        private fun animateCardHeight(card: CardView, fromHeight: Int, toHeight: Int, duration: Long = 300) {
            val animator = ValueAnimator.ofInt(fromHeight, toHeight)
            animator.duration = duration
            animator.addUpdateListener { animation ->
                val value = animation.animatedValue as Int
                val layoutParams = card.layoutParams
                layoutParams.height = value
                card.layoutParams = layoutParams
            }
            animator.start()
        }

        private fun getOnlyRealMonthName(yearMonth: String,originFormat: String): String {
            val getMonth = yearMonth.split("-")[1]

            val inputFormat = SimpleDateFormat(originFormat, Locale.getDefault())
            val date = inputFormat.parse("$getMonth-01")

            val outputFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
            val monthName = outputFormat.format(date!!)
            return monthName
        }
        private fun changeYearMonthToMonthYear(inputDate: String): String{
            val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)

            val date = inputFormat.parse(inputDate)
            val formattedMonthYear = outputFormat.format(date!!)
            return formattedMonthYear
        }
    }

