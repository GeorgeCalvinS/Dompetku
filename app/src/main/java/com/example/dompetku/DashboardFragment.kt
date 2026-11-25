package com.example.dompetku

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dompetku.R
import com.example.dompetku.databinding.FragmentDashboardBinding
import java.text.NumberFormat
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefManager: SharedPrefManager
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = SharedPrefManager(requireContext())

        binding.btnAddIncome.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_income)
        }

        binding.btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_expense)
        }

        loadDashboard()
    }

    override fun onResume() {
        super.onResume()
        if (::prefManager.isInitialized) {
            loadDashboard()
        }
    }

    private fun loadDashboard() {
        val totalIncome = prefManager.getTotalIncome()
        val totalExpense = prefManager.getTotalExpense()
        val balance = prefManager.getBalance()

        binding.tvTotalIncome.text = currencyFormat.format(totalIncome)
        binding.tvTotalExpense.text = currencyFormat.format(totalExpense)
        binding.tvBalance.text = currencyFormat.format(balance)

        val recentTransactions = prefManager.getTransactions()
            .sortedByDescending { it.date }
            .take(5)
            .toMutableList()

        val adapter = TransactionAdapter(
            recentTransactions,
            onEdit = { },
            onDelete = { },
            showActions = false
        )
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentTransactions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
