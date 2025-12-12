package com.example.yakovleva_zd7_v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val items: List<CartItem>,
    private val onItemAction: (CartItem, String) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.productName)
        val price: TextView = itemView.findViewById(R.id.productPrice)
        val quantity: TextView = itemView.findViewById(R.id.quantityText)
        val total: TextView = itemView.findViewById(R.id.productTotal)
        val increaseBtn: Button = itemView.findViewById(R.id.increaseButton)
        val decreaseBtn: Button = itemView.findViewById(R.id.decreaseButton)
        val removeBtn: Button = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]

        holder.name.text = item.name
        holder.price.text = "%.2f $".format(item.price)
        holder.quantity.text = item.quantity.toString()
        holder.total.text = "%.2f $".format(item.price * item.quantity)

        holder.increaseBtn.setOnClickListener {
            onItemAction(item, "increase")
        }

        holder.decreaseBtn.setOnClickListener {
            onItemAction(item, "decrease")
        }

        holder.removeBtn.setOnClickListener {
            onItemAction(item, "remove")
        }
    }

    override fun getItemCount(): Int = items.size
}