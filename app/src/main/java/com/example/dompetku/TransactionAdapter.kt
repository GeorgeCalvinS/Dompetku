package com.example.dompetku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.R
import com.example.dompetku.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.util.Locale

class TransactionAdapter(
    private var transactions: MutableList<Transaction>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit,
    private val showActions: Boolean = true
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    inner class TransactionViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.binding.apply {
            tvTransactionDesc.text = transaction.description
            tvTransactionCategory.text = transaction.categoryName
            tvTransactionDate.text = transaction.date
            tvTransactionAmount.text = currencyFormat.format(transaction.amount)
            tvTransactionType.text = transaction.type

            if (transaction.type.equals("Pemasukan", ignoreCase = true)) {
                tvTransactionAmount.setTextColor(
                    ContextCompat.getColor(root.context, R.color.green)
                )
                tvTransactionType.setTextColor(
                    ContextCompat.getColor(root.context, R.color.green)
                )
            } else {
                tvTransactionAmount.setTextColor(
                    ContextCompat.getColor(root.context, R.color.red)
                )
                tvTransactionType.setTextColor(
                    ContextCompat.getColor(root.context, R.color.red)
                )
            }

            if (showActions) {
                btnEditTransaction.visibility = View.VISIBLE
                btnDeleteTransaction.visibility = View.VISIBLE
                btnEditTransaction.setOnClickListener { onEdit(transaction) }
                btnDeleteTransaction.setOnClickListener { onDelete(transaction) }
            } else {
                btnEditTransaction.visibility = View.GONE
                btnDeleteTransaction.visibility = View.GONE
            }
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }
}
