package com.example.dompetku

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPrefManager(context: Context) {

    val sp: SharedPreferences = context.getSharedPreferences("dataSP", Context.MODE_PRIVATE)

    fun simpanKeSP(categories: ArrayList<Category>) {
        val gson = Gson()
        val json = gson.toJson(categories)
        sp.edit().putString("spCategory", json).apply()
    }

    fun saveCategories(categories: ArrayList<Category>) {
        simpanKeSP(categories)
    }

    fun getCategories(): ArrayList<Category> {
        val _arCategory = ArrayList<Category>()
        val gson = Gson()
        val isiSP = sp.getString("spCategory", null)
        val type = object : TypeToken<ArrayList<Category>>() {}.type

        if (isiSP != null) {
            _arCategory.addAll(gson.fromJson(isiSP, type))
        }
        return _arCategory
    }

    fun addCategory(category: Category) {
        val categories = getCategories()
        categories.add(category)
        saveCategories(categories)
    }

    fun updateCategory(category: Category) {
        val categories = getCategories()
        val index = categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            categories[index] = category
            saveCategories(categories)
        }
    }

    fun deleteCategory(categoryId: String) {
        val categories = getCategories()
        categories.removeAll { it.id == categoryId }
        saveCategories(categories)
    }

    fun getCategoriesByType(type: String): ArrayList<Category> {
        val filtered = getCategories().filter { it.type.equals(type, ignoreCase = true) }
        return ArrayList(filtered)
    }

    fun simpanTransaksiKeSP(transactions: ArrayList<Transaction>) {
        val gson = Gson()
        val json = gson.toJson(transactions)
        sp.edit().putString("spTransaction", json).apply()
    }

    fun saveTransactions(transactions: ArrayList<Transaction>) {
        simpanTransaksiKeSP(transactions)
    }

    fun getTransactions(): ArrayList<Transaction> {
        val _arTransaction = ArrayList<Transaction>()
        val gson = Gson()
        val isiSP = sp.getString("spTransaction", null)
        val type = object : TypeToken<ArrayList<Transaction>>() {}.type

        if (isiSP != null) {
            _arTransaction.addAll(gson.fromJson(isiSP, type))
        }
        return _arTransaction
    }

    fun addTransaction(transaction: Transaction) {
        val transactions = getTransactions()
        transactions.add(transaction)
        saveTransactions(transactions)
    }

    fun updateTransaction(transaction: Transaction) {
        val transactions = getTransactions()
        val index = transactions.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            transactions[index] = transaction
            saveTransactions(transactions)
        }
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }

    fun getTransactionsByType(type: String): ArrayList<Transaction> {
        val filtered = getTransactions().filter { it.type.equals(type, ignoreCase = true) }
        return ArrayList(filtered)
    }

    fun getTransactionsByMonth(year: Int, month: Int): ArrayList<Transaction> {
        val filtered = getTransactions().filter {
            val parts = it.date.split("-")
            if (parts.size >= 2) {
                parts[0].toIntOrNull() == year && parts[1].toIntOrNull() == month
            } else {
                false
            }
        }
        return ArrayList(filtered)
    }

    fun getTotalIncome(): Double {
        return getTransactionsByType("Pemasukan").sumOf { it.amount }
    }

    fun getTotalExpense(): Double {
        return getTransactionsByType("Pengeluaran").sumOf { it.amount }
    }

    fun getBalance(): Double {
        return getTotalIncome() - getTotalExpense()
    }

    fun clearAllData() {
        sp.edit().clear().apply()
    }
}
