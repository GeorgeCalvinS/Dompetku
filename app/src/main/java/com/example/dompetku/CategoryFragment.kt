package com.example.dompetku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dompetku.R
import com.example.dompetku.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: SharedPrefManager
    private lateinit var adapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = SharedPrefManager(requireContext())

        setupRecyclerView()
        setupFilterChips()

        binding.fabAddCategory.setOnClickListener {
            showCategoryDialog(null)
        }

        loadCategories()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            mutableListOf(),
            onEdit = { category -> showCategoryDialog(category) },
            onDelete = { category -> deleteCategory(category) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategories.adapter = adapter
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            loadCategories()
        }
    }

    private fun loadCategories() {
        val allCategories = prefManager.getCategories()
        val filteredCategories = when (binding.chipGroupFilter.checkedChipId) {
            R.id.chipIncome -> allCategories.filter { it.type.equals("Pemasukan", ignoreCase = true) }
            R.id.chipExpense -> allCategories.filter { it.type.equals("Pengeluaran", ignoreCase = true) }
            else -> allCategories
        }
        adapter.updateData(filteredCategories)

        if (filteredCategories.isEmpty()) {
            binding.tvEmptyCategory.visibility = View.VISIBLE
            binding.rvCategories.visibility = View.GONE
        } else {
            binding.tvEmptyCategory.visibility = View.GONE
            binding.rvCategories.visibility = View.VISIBLE
        }
    }

    private fun showCategoryDialog(category: Category?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_category, null)

        val etCategoryName = dialogView.findViewById<EditText>(R.id.etDialogCategoryName)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerDialogCategoryType)

        val types = arrayOf("Pemasukan", "Pengeluaran")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = spinnerAdapter

        if (category != null) {
            etCategoryName.setText(category.name)
            spinnerType.setSelection(
                if (category.type.equals("Pemasukan", ignoreCase = true)) 0 else 1
            )
        }

        val title = if (category == null) "Tambah Kategori" else "Ubah Kategori"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etCategoryName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "Nama kategori tidak boleh kosong",
                        Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val type = if (spinnerType.selectedItemPosition == 0) "Pemasukan" else "Pengeluaran"

                if (category == null) {
                    val newCategory = Category(
                        name = name,
                        type = type
                    )
                    prefManager.addCategory(newCategory)
                    Toast.makeText(requireContext(), "Kategori berhasil ditambahkan",
                        Toast.LENGTH_SHORT).show()
                } else {
                    val updatedCategory = category.copy(name = name, type = type)
                    prefManager.updateCategory(updatedCategory)
                    Toast.makeText(requireContext(), "Kategori berhasil diubah",
                        Toast.LENGTH_SHORT).show()
                }
                loadCategories()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteCategory(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Apakah Anda yakin ingin menghapus kategori \"${category.name}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                prefManager.deleteCategory(category.id)
                Toast.makeText(requireContext(), "Kategori berhasil dihapus",
                    Toast.LENGTH_SHORT).show()
                loadCategories()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
