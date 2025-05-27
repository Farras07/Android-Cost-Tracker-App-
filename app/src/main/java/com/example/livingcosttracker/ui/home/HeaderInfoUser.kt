package com.example.livingcosttracker.ui.home

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.DrawableRes
import com.example.livingcosttracker.R

class HeaderInfoUser @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

//    private val balanceSection: TextView
    private val toSpendSection: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card_component, this, true)
//        balanceSection = findViewById(R.id.balance)
        toSpendSection = findViewById(R.id.toSpendSection)


        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.custom_card_component,
            0, 0
        ).apply {
            try {
                var balanceSectionValue = getString(R.styleable.custom_card_component_cardTitle) ?: "0"
            } finally {
                recycle()
            }
        }
    }

//    fun setBalance(text: String) {
//        balanceSection.text = text
//    }

    fun settoSpend(text: String) {
        toSpendSection.text = text
    }
}
