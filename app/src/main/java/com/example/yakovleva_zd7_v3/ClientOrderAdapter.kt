package com.example.yakovleva_zd7_v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ClientOrdersAdapter(
    private var orders: List<ClientOrdersFragment.OrderWithItems>,
    private val onOrderClick: (ClientOrdersFragment.OrderWithItems) -> Unit = {}
) : RecyclerView.Adapter<ClientOrdersAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderNumber: TextView = view.findViewById(R.id.orderNumber)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val orderDate: TextView = view.findViewById(R.id.orderDate)
        val orderTotal: TextView = view.findViewById(R.id.orderTotal)
        val orderItems: TextView = view.findViewById(R.id.orderItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_client_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderWithItems = orders[position]
        val order = orderWithItems.order
        val items = orderWithItems.items

        holder.orderNumber.text = "Заказ ${order.orderNumber}"
        holder.orderStatus.text = order.status

        //цвет статуса в зависимости от состояния
        when (order.status) {
            "Новый" -> {
                holder.orderStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.almond)
                )
                holder.orderStatus.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.carob)
                )
            }
            "В производстве" -> {
                holder.orderStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.almond)
                )
                holder.orderStatus.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.chai)
                )
            }
            "Готов" -> {
                holder.orderStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.almond)
                )
                holder.orderStatus.setBackgroundColor(
                    ContextCompat.getColor(holder.itemView.context, R.color.matcha)
                )
            }
            else -> holder.orderStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.carob)
            )
        }

        holder.orderDate.text = "Дата: ${dateFormat.format(Date(order.createdAt))}"
        holder.orderTotal.text = "%.2f $".format(order.totalAmount)

        val itemsText = items.joinToString("\n") {
            "• ${it.productName} (${it.quantity} шт) - %.2f $".format(it.price * it.quantity)
        }
        holder.orderItems.text = itemsText

        holder.itemView.setOnClickListener {
            onOrderClick(orderWithItems)
        }
    }

    override fun getItemCount() = orders.size

    fun updateList(newOrders: List<ClientOrdersFragment.OrderWithItems>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}