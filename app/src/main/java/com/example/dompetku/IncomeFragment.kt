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
import com.example.dompetku.databinding.FragmentIncomeBinding
import com.google.gson.Gson
import java.util.Calendar

class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: SharedPrefManager
    private var incomeCategories: List<Category> = emptyList()
    private var editTransaction: Transaction? = null
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncomeBinding.inflate(inflater, container, false)
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
            binding.etIncomeAmount.setText(transaction.amount.toLong().toString())
            binding.etIncomeDescription.setText(transaction.description)
            binding.etIncomeDate.setText(transaction.date)
            binding.btnSaveIncome.text = "Ubah Pemasukan"

            val categoryIndex = incomeCategories.indexOfFirst { it.id == transaction.categoryId }
            if (categoryIndex >= 0) {
                binding.spinnerIncomeCategory.setSelection(categoryIndex)
            }
        }
    }

    private fun setupCategorySpinner() {
        incomeCategories = prefManager.getCategoriesByType("Pemasukan")

        if (incomeCategories.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Belum ada kategori pemasukan. Silakan tambah kategori terlebih dahulu.",
                Toast.LENGTH_LONG
            ).show()
        }

        val categoryNames = incomeCategories.map { it.name }
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIncomeCategory.adapter = spinnerAdapter
    }

    private fun setupDatePicker() {
        binding.etIncomeDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, y, m, d ->
                val selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                binding.etIncomeDate.setText(selectedDate)
            }, year, month, day).show()
        }
        binding.etIncomeDate.isFocusable = false
    }

    private fun setupSaveButton() {
        binding.btnSaveIncome.setOnClickListener {
            val amountStr = binding.etIncomeAmount.text.toString().trim()
            val description = binding.etIncomeDescription.text.toString().trim()
            val date = binding.etIncomeDate.text.toString().trim()
            val selectedPosition = binding.spinnerIncomeCategory.selectedItemPosition

            if (amountStr.isEmpty()) {
                binding.etIncomeAmount.error = "Jumlah tidak boleh kosong"
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                binding.etIncomeDescription.error = "Deskripsi tidak boleh kosong"
                return@setOnClickListener
            }
            if (date.isEmpty()) {
                binding.etIncomeDate.error = "Tanggal tidak boleh kosong"
                return@setOnClickListener
            }
            if (incomeCategories.isEmpty() || selectedPosition < 0) {
                Toast.makeText(requireContext(), "Pilih kategori terlebih dahulu",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etIncomeAmount.error = "Masukkan jumlah yang valid"
                return@setOnClickListener
            }

            val selectedCategory = incomeCategories[selectedPosition]

            if (editTransaction != null) {
                val updatedTransaction = editTransaction!!.copy(
                    categoryId = selectedCategory.id,
                    categoryName = selectedCategory.name,
                    amount = amount,
                    description = description,
                    date = date
                )
                prefManager.updateTransaction(updatedTransaction)
                Toast.makeText(requireContext(), "Pemasukan berhasil diubah",
                    Toast.LENGTH_SHORT).show()
            } else {
                val transaction = Transaction(
                    categoryId = selectedCategory.id,
                    categoryName = selectedCategory.name,
                    amount = amount,
                    description = description,
                    date = date,
                    type = "Pemasukan"
                )
                prefManager.addTransaction(transaction)
                Toast.makeText(requireContext(), "Pemasukan berhasil ditambahkan",
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
