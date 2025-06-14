package com.example.livingcosttracker.ui.home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.livingcosttracker.EditTransactionActivity
import com.example.livingcosttracker.R
import com.example.livingcosttracker.db.Cashflow
import java.text.SimpleDateFormat
import java.util.*

class CashflowAdapter(
    private val context: Context,
    private val transactions: List<Cashflow>
) : RecyclerView.Adapter<CashflowAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: TextView = itemView.findViewById(R.id.cardCashlowTotalText)
        val tvCategory: TextView = itemView.findViewById(R.id.cardCashlowTitleText)
        val tvDate: TextView = itemView.findViewById(R.id.pickDateButton)
        //val tvDesc: TextView = itemView.findViewById(R.id.)
        init {
            itemView.setOnClickListener {
                val transaction = transactions[adapterPosition]

                val intent = Intent(context, EditTransactionActivity::class.java)
                intent.putExtra("id", transaction.id)
                intent.putExtra("amount", transaction.amount.toString())
                intent.putExtra("category", transaction.category)
                intent.putExtra("itemCategory", transaction.itemCategory)
                intent.putExtra("date", formatDate(transaction.date))
                intent.putExtra("description", transaction.description)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.component_card_cashflow, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvAmount.text = "Rp. ${transaction.amount.toString()}"
        holder.tvCategory.text = "${transaction.category} - ${transaction.itemCategory}"
        holder.tvDate.text = formatDate(transaction.date)
    }

    override fun getItemCount(): Int = transactions.size

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }
}