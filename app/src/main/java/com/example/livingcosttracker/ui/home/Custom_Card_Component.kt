package com.example.livingcosttracker.ui.home

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.DrawableRes
import com.example.livingcosttracker.R

class Custom_Card_Component @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val icon: ImageView
    private val title: TextView
    private val content: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card_component, this, true)
        icon = findViewById(R.id.infoCardIcon)
        title = findViewById(R.id.infoCardTitle)
        content = findViewById(R.id.infoCardContent)


        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.custom_card_component,
            0, 0
        ).apply {
            try {
                title.text = getString(R.styleable.custom_card_component_cardTitle) ?: "Title"
                content.text = getString(R.styleable.custom_card_component_cardContent) ?: "Content"
                val contentTextSize= getDimension(R.styleable.custom_card_component_cardContentTextSize, 20f)
                content.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentTextSize)
                icon.setImageResource(getResourceId(R.styleable.custom_card_component_cardIcon, R.drawable.ic_launcher_foreground))
            } finally {
                recycle()
            }
        }
    }

    fun setCardTitle(text: String) {
        title.text = text
    }

    fun setCardContent(text: String) {
        content.text = text
    }

    fun setCardIcon(@DrawableRes resId: Int) {
        icon.setImageResource(resId)
    }
}
