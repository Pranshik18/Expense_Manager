package com.priyanshu.expensemanager.ui


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.priyanshu.expensemanager.R
import com.priyanshu.expensemanager.data.expense
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.cardlist_item.*
import kotlinx.android.synthetic.main.list_item.*

class CalendarAdapter(private val listener: (String) -> Unit):
    ListAdapter<expense, CalendarAdapter.ViewHolder>(
        DiffCallback3()
    ){

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardlist_item, parent, false)

        return ViewHolder(itemLayout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder (override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {
        init{
            itemView.setOnClickListener{
                listener.invoke(getItem(adapterPosition).date)
            }
        }

        fun bind(transaction: expense){
            with(transaction){
                item_name.text = transaction.name
                item_price.text = transaction.amount.toString()

            }
        }
    }
}

class DiffCallback3 : DiffUtil.ItemCallback<expense>() {
    override fun areItemsTheSame(oldItem: expense, newItem: expense): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: expense, newItem: expense): Boolean {
        return oldItem == newItem
    }
}