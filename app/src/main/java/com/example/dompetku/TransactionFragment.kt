package com.example.dompetku

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dompetku.R
import com.example.dompetku.databinding.FragmentTransactionBinding
import com.google.gson.Gson

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: SharedPrefManager
    private lateinit var adapter: TransactionAdapter
    private val gson = Gson()

    private var allTransactions: List<Transaction> = emptyList()
    private var currentFilter = "Semua"
    private var currentSearch = ""
    private var sortByDateDesc = true
    private var sortByAmountDesc = true
    private var sortBy = "date"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = SharedPrefManager(requireContext())

        setupRecyclerView()
        setupSearch()
        setupFilter()
        setupSort()

        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        if (::prefManager.isInitialized) {
            loadTransactions()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            mutableListOf(),
            onEdit = { transaction -> editTransaction(transaction) },
            onDelete = { transaction -> deleteTransaction(transaction) }
        )
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTransactions.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearch = s.toString().trim()
                applyFiltersAndSort()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilter() {
        val filterOptions = arrayOf("Semua", "Pemasukan", "Pengeluaran")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            filterOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFilter.adapter = spinnerAdapter

        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = filterOptions[position]
                applyFiltersAndSort()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSort() {
        binding.btnSortDate.setOnClickListener {
            sortBy = "date"
            sortByDateDesc = !sortByDateDesc
            binding.btnSortDate.text = if (sortByDateDesc) "Tanggal (Desc)" else "Tanggal (Asc)"
            applyFiltersAndSort()
        }

        binding.btnSortAmount.setOnClickListener {
            sortBy = "amount"
            sortByAmountDesc = !sortByAmountDesc
            binding.btnSortAmount.text = if (sortByAmountDesc) "Jumlah (Desc)" else "Jumlah (Asc)"
            applyFiltersAndSort()
        }
    }

    private fun loadTransactions() {
        allTransactions = prefManager.getTransactions()
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        var filtered = allTransactions.toList()

        filtered = when (currentFilter) {
            "Pemasukan" -> filtered.filter { it.type.equals("Pemasukan", ignoreCase = true) }
            "Pengeluaran" -> filtered.filter { it.type.equals("Pengeluaran", ignoreCase = true) }
            else -> filtered
        }

        if (currentSearch.isNotEmpty()) {
            filtered = filtered.filter {
                it.description.contains(currentSearch, ignoreCase = true) ||
                it.categoryName.contains(currentSearch, ignoreCase = true)
            }
        }

        filtered = if (sortBy == "amount") {
            if (sortByAmountDesc) filtered.sortedByDescending { it.amount }
            else filtered.sortedBy { it.amount }
        } else {
            if (sortByDateDesc) filtered.sortedByDescending { it.date }
            else filtered.sortedBy { it.date }
        }

        adapter.updateData(filtered)

        if (filtered.isEmpty()) {
            binding.tvEmptyTransaction.visibility = View.VISIBLE
            binding.rvTransactions.visibility = View.GONE
        } else {
            binding.tvEmptyTransaction.visibility = View.GONE
            binding.rvTransactions.visibility = View.VISIBLE
        }
    }

    private fun editTransaction(transaction: Transaction) {
        val bundle = Bundle().apply {
            putString("transaction_json", gson.toJson(transaction))
        }

        if (transaction.type.equals("Pemasukan", ignoreCase = true)) {
            findNavController().navigate(R.id.action_transaction_to_income, bundle)
        } else {
            findNavController().navigate(R.id.action_transaction_to_expense, bundle)
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi \"${transaction.description}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                prefManager.deleteTransaction(transaction.id)
                Toast.makeText(requireContext(), "Transaksi berhasil dihapus",
                    Toast.LENGTH_SHORT).show()
                loadTransactions()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
