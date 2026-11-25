package com.example.dompetku

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dompetku.databinding.FragmentExpenseBinding
import com.google.gson.Gson
import java.util.Calendar

class ExpenseFragment : Fragment() {

    private var _binding: FragmentExpenseBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: SharedPrefManager
    private var expenseCategories: List<Category> = emptyList()
    private var editTransaction: Transaction? = null
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = SharedPrefManager(requireContext())

        arguments?.getString("transaction_json")?.let { json ->
            editTransaction = gson.fromJson(json, Transaction::class.java)
        }

        setupCategorySpinner()
        setupDatePicker()
        setupSaveButton()

        editTransaction?.let { transaction ->
            binding.etExpenseAmount.setText(transaction.amount.toLong().toString())
            binding.etExpenseDescription.setText(transaction.description)
            binding.etExpenseDate.setText(transaction.date)
            binding.btnSaveExpense.text = "Ubah Pengeluaran"

            val categoryIndex = expenseCategories.indexOfFirst { it.id == transaction.categoryId }
            if (categoryIndex >= 0) {
                binding.spinnerExpenseCategory.setSelection(categoryIndex)
            }
        }
    }

    private fun setupCategorySpinner() {
        expenseCategories = prefManager.getCategoriesByType("Pengeluaran")

        if (expenseCategories.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Belum ada kategori pengeluaran. Silakan tambah kategori terlebih dahulu.",
                Toast.LENGTH_LONG
            ).show()
        }

        val categoryNames = expenseCategories.map { it.name }
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerExpenseCategory.adapter = spinnerAdapter
    }

    private fun setupDatePicker() {
        binding.etExpenseDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.etExpenseDate.setText(selectedDate)
            }, year, month, day).show()
        }
        binding.etExpenseDate.isFocusable = false
    }

    private fun setupSaveButton() {
        binding.btnSaveExpense.setOnClickListener {
            val amountStr = binding.etExpenseAmount.text.toString().trim()
            val description = binding.etExpenseDescription.text.toString().trim()
            val date = binding.etExpenseDate.text.toString().trim()
            val selectedPosition = binding.spinnerExpenseCategory.selectedItemPosition

            if (amountStr.isEmpty()) {
                binding.etExpenseAmount.error = "Jumlah tidak boleh kosong"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                binding.etExpenseDescription.error = "Deskripsi tidak boleh kosong"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                binding.etExpenseDate.error = "Tanggal tidak boleh kosong"
                return@setOnClickListener
            }
            if (expenseCategories.isEmpty() || selectedPosition < 0) {
                Toast.makeText(requireContext(), "Pilih kategori terlebih dahulu",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etExpenseAmount.error = "Masukkan jumlah yang valid"
                return@setOnClickListener
            }

            val selectedCategory = expenseCategories[selectedPosition]

            if (editTransaction != null) {
                val updatedTransaction = editTransaction!!.copy(
                    categoryId = selectedCategory.id,
                    categoryName = selectedCategory.name,
                    amount = amount,
                    description = description,
                    date = date
                )
                prefManager.updateTransaction(updatedTransaction)
                Toast.makeText(requireContext(), "Pengeluaran berhasil diubah",
                    Toast.LENGTH_SHORT).show()
            } else {
                val transaction = Transaction(
                    categoryId = selectedCategory.id,
                    categoryName = selectedCategory.name,
                    amount = amount,
                    description = description,
                    date = date,
                    type = "Pengeluaran"
                )
                prefManager.addTransaction(transaction)
                Toast.makeText(requireContext(), "Pengeluaran berhasil ditambahkan",
                    Toast.LENGTH_SHORT).show()
            }

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
