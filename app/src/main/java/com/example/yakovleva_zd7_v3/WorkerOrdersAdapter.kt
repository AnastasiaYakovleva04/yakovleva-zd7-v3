package com.example.yakovleva_zd7_v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WorkerOrdersAdapter(
    private var orders: List<OrdersListFragment.OrderWithItems>,
    private val onOrderClick: (OrdersListFragment.OrderWithItems) -> Unit
) : RecyclerView.Adapter<WorkerOrdersAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderNumber: TextView = view.findViewById(R.id.orderNumber)
        val clientName: TextView = view.findViewById(R.id.clientName)
        val orderDate: TextView = view.findViewById(R.id.orderDate)
        val orderTotal: TextView = view.findViewById(R.id.orderTotal)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val orderItems: TextView = view.findViewById(R.id.orderItems)
        val actionButton: Button = view.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_worker_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderWithItems = orders[position]
        val order = orderWithItems.order
        val items = orderWithItems.items
        val client = orderWithItems.client

        holder.orderNumber.text = "Заказ ${order.orderNumber}"
        holder.clientName.text = "Клиент: ${client?.name ?: "Неизвестен"}"
        holder.orderDate.text = "Дата: ${dateFormat.format(Date(order.createdAt))}"
        holder.orderTotal.text = "%.2f $".format(order.totalAmount)
        holder.orderStatus.text = order.status

        when (order.status) {
            "Новый" -> {
                holder.orderStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.almond))
                holder.actionButton.text = "Произвести"
                holder.actionButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.matcha))
                holder.actionButton.visibility = View.VISIBLE
            }
            "В производстве" -> {
                holder.orderStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.almond))
                holder.actionButton.text = "Продолжить"
                holder.actionButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.chai))
                holder.actionButton.visibility = View.VISIBLE
            }
            "Готов" -> {
                holder.orderStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.almond))
                holder.actionButton.text = "Просмотр"
                holder.actionButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.chai))
                holder.actionButton.visibility = View.VISIBLE
            }
            else -> {
                holder.actionButton.visibility = View.GONE
            }
        }

        val itemsText = items.joinToString("\n") {
            "• ${it.productName} (${it.quantity} шт)"
        }
        holder.orderItems.text = itemsText

        holder.actionButton.setOnClickListener {
            onOrderClick(orderWithItems)
        }

        holder.itemView.setOnClickListener {
            onOrderClick(orderWithItems)
        }
    }

    override fun getItemCount() = orders.size

    fun updateList(newOrders: List<OrdersListFragment.OrderWithItems>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}