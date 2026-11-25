package com.example.dompetku

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetku.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var categories: MutableList<Category>,
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.apply {
            tvCategoryName.text = category.name
            tvCategoryType.text = category.type
            btnEditCategory.setOnClickListener { onEdit(category) }
            btnDeleteCategory.setOnClickListener { onDelete(category) }
        }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }
}
