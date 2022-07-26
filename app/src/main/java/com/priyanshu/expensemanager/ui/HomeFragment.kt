package com.priyanshu.expensemanager.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.priyanshu.expensemanager.R
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.set_balance_info.view.*
import org.eazegraph.lib.models.PieModel


class HomeFragment : Fragment() {

    private lateinit var viewModel: TransactionListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(TransactionListViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onResume() {
        val sharedPreferences : SharedPreferences = this.requireActivity().getSharedPreferences("Preference", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        setNetBalance(sharedPreferences,editor)
        refreshInfo(sharedPreferences,editor)
        super.onResume()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Adding Back Button
        val toolbar : MaterialToolbar = requireActivity().findViewById(R.id.topAppBar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_person_48)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
        }
        appBar()

        val sharedPreferences : SharedPreferences = this.requireActivity().getSharedPreferences("Preference", Context.MODE_PRIVATE)
        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        setNetBalance(sharedPreferences, editor)

        updatePieChart()

        set_balance_info.setOnClickListener {
            showDialog()
        }

        // Transaction List
        with(transaction_list){
            layoutManager = LinearLayoutManager(activity)
            adapter = TransactionAdapter {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddTransactionFragment(
                        it
                    )
                )

            }
        }

        add_transaction.setOnClickListener{
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddTransactionFragment(
                    0
                )
            )
        }


        // Month Card List
        with(monthly_card_list){
            layoutManager = LinearLayoutManager(activity)
            adapter = MonthlyCardAdapter({
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMonthlyExpenseFragment(
                        it
                    )
                )
            },requireContext())

        }

        viewModel.month.observe(viewLifecycleOwner, Observer{
            (monthly_card_list.adapter as MonthlyCardAdapter).submitList(it)
        })


        viewModel.transactions.observe(viewLifecycleOwner, Observer{
            (transaction_list.adapter as TransactionAdapter).submitList(it)
        })




        refreshInfo(sharedPreferences, editor)

    }

    private fun refreshInfo(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor) {
        var cash = sharedPreferences.getFloat(getString(R.string.CASH), 0f)
        var credit = sharedPreferences.getFloat(getString(R.string.CASH), 0f)
        var bank = sharedPreferences.getFloat(getString(R.string.CASH), 0f)


        viewModel.cash.observe(viewLifecycleOwner, Observer {
            if (cash != 0f && it != null) {
                cash = sharedPreferences.getFloat(getString(R.string.CASH), 0f)
                cash += it
                editor.putFloat(getString(R.string.CASH), cash)
                cash_amount.text = cash.toString()
            }
        })
        viewModel.credit.observe(viewLifecycleOwner, Observer {
            if (credit != 0f && it != null) {
                credit = sharedPreferences.getFloat(getString(R.string.CASH), 0f)
                credit += it
                editor.putFloat(getString(R.string.CREDIT), credit)
                credit_amount.text = credit.toString()
            }
        })
        viewModel.bank.observe(viewLifecycleOwner, Observer {
            if (bank != 0f && it != null) {
                bank = sharedPreferences.getFloat(getString(R.string.CASH), 0f)
                Log.d("TAG", "BANK: " + it)
                Log.d("TAG", "BANK S:" + bank)
                bank += it
                editor.putFloat(getString(R.string.BANK), bank)
                debit_amount.text = bank.toString()
            }
        })
        updatePieChart()
    }

    private fun setNetBalance(sharedPreferences: SharedPreferences, editor: SharedPreferences.Editor) {
        // Getting data to set up name and monthly budget in the home screen
        var yearlyBudget = sharedPreferences.getFloat(getString(R.string.YearlyBudget),0f)

        net_balance.text = yearlyBudget.toString()
        viewModel.expense.observe(viewLifecycleOwner, Observer{
            if(it!=null){
                yearlyBudget = sharedPreferences.getFloat(getString(R.string.YearlyBudget),0f)
                yearlyBudget += it
                editor.putFloat(getString(R.string.YearlyBudget),yearlyBudget)
                net_balance.text = yearlyBudget.toString()
            }
        })
    }

    private fun appBar() {
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
//                R.id.profile -> {
//                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
//                    true
//                }
                R.id.calendar -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCalendarFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun showDialog() {

        val dialog = LayoutInflater.from(requireContext()).inflate(R.layout.set_balance_info, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(requireContext())
            .setView(dialog)
            .setTitle("Set Details")
        //show dialog
        val  mAlertDialog = mBuilder.show()

        checkValues(dialog)

        dialog.set_info.setOnClickListener {
            mAlertDialog.dismiss()
            val cashAmount = dialog.Cash.text.toString()
            val bankAmount = dialog.Bank.text.toString()
            setBalanceInfo(cashAmount.toFloat(),bankAmount.toFloat(),dialog)
        }
        dialog.cancel.setOnClickListener { mAlertDialog.dismiss() }
        mAlertDialog.show()

    }

    private fun checkValues(dialog: View) {
        val sharedPreferences : SharedPreferences = this.requireActivity().getSharedPreferences("Preference", Context.MODE_PRIVATE)

        var yearlyBudget = sharedPreferences.getFloat(getString(R.string.FinalMonthBudget),0f)*12


        val boardingTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
                val bankAmount = dialog.Bank.text.toString()
                val cashAmount = dialog.Cash.text.toString()

                if(cashAmount.isEmpty() || bankAmount.isEmpty()){
                    dialog.set_info.isEnabled = false
                }else if(!cashAmount.isEmpty() && cashAmount.toFloat() > yearlyBudget){
                    dialog.set_info.isEnabled = false
                    dialog.Cash.error = "Greater than net balance available"
                }else if(!bankAmount.isEmpty() && bankAmount.toFloat()>yearlyBudget || cashAmount.toFloat()+bankAmount.toFloat() > yearlyBudget){
                    dialog.set_info.isEnabled = false
                    dialog.Bank.error = "Greater than net balance available"
                }
                else{
                    dialog.Cash.error = null
                    dialog.set_info.isEnabled = true
                    val creditAmount = yearlyBudget - (cashAmount.toFloat() + bankAmount.toFloat())
                    dialog.Credit.setText(creditAmount.toString())
                }

            }
        }
        dialog.Cash.addTextChangedListener(boardingTextWatcher)
        dialog.Bank.addTextChangedListener(boardingTextWatcher)

    }


    private fun setBalanceInfo(cashAmount: Float, bankAmount: Float, dialog: View) {
        val sharedPreferences : SharedPreferences = this.requireActivity().getSharedPreferences("Preference", Context.MODE_PRIVATE)

        var yearlyBudget = sharedPreferences.getFloat(getString(R.string.FinalMonthBudget),0f)*12
        val creditAmount = (yearlyBudget) - (cashAmount + bankAmount)
        dialog.Credit.setText(creditAmount.toString())

        val editor:SharedPreferences.Editor =  sharedPreferences.edit()
        editor.putFloat(getString(R.string.CASH),cashAmount)
        editor.putFloat(getString(R.string.BANK),bankAmount)
        editor.putFloat(getString(R.string.CREDIT),creditAmount)
        editor.putBoolean(getString(R.string.FLAG),true)
        editor.apply()


        // UpdatePieChart
        updatePieChart()

        // Refresh Fragment
        refreshFragment()

    }

    fun updatePieChart(){

        val sharedPreferences : SharedPreferences = this.requireActivity().getSharedPreferences("Preference", Context.MODE_PRIVATE)
        var mcash = sharedPreferences.getFloat("CashAmount",0f)
        var mbank = sharedPreferences.getFloat("BankAmount",0f)
        var mcredit = sharedPreferences.getFloat("CreditAmount",0f)

        cash_amount.text = ""+mcash
        debit_amount.text = ""+mbank
        credit_amount.text = ""+mcredit

        var cash = (mcash / (mcash+mbank+mcredit))*100
        var credit = (mcredit / (mcash+mbank+mcredit))*100
        var bank = (mbank / (mcash+mbank+mcredit))*100

        piechart.clearChart()
        // Adding pie chart
        piechart?.addPieSlice(
            PieModel("Cash", cash, Color.parseColor("#FFA726"))
        )
        piechart?.addPieSlice(
            PieModel("Credit", credit, Color.parseColor("#66BB6A"))
        )
        piechart?.addPieSlice(
            PieModel("Bank", bank, Color.parseColor("#EF5350"))
        )

        piechart?.startAnimation();

    }
    @Suppress("DEPRECATION")
    fun refreshFragment() {
        val ft: FragmentTransaction = requireFragmentManager().beginTransaction()
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false)
        }
        ft.detach(this).attach(this).commit()
    }



}