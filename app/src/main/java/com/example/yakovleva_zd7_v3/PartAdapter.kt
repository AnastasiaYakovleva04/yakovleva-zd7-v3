package com.example.yakovleva_zd7_v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PartAdapter(
    private var parts: MutableList<Part>,
    private val onEditClick: (Part) -> Unit,
    private val onDeleteClick: (Part) -> Unit
) : RecyclerView.Adapter<PartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.partName)
        val typeView: TextView = view.findViewById(R.id.partType)
        val priceView: TextView = view.findViewById(R.id.partPrice)
        val characteristicsView: TextView = view.findViewById(R.id.partCharacteristics)
        val editButton: Button = view.findViewById(R.id.buttonEdit)
        val deleteButton: Button = view.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_part, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val part = parts[position]

        holder.nameView.text = part.name
        holder.typeView.text = "Тип: ${part.type}"
        holder.priceView.text = "%.2f $".format(part.price)
        holder.characteristicsView.text = "Характеристики: ${part.characteristics}"

        holder.editButton.setOnClickListener {
            onEditClick(part)
        }

        holder.deleteButton.setOnClickListener {
            onDeleteClick(part)
        }
    }

    override fun getItemCount() = parts.size

    fun updateList(newParts: List<Part>) {
        parts.clear()
        parts.addAll(newParts)
        notifyDataSetChanged()
    }
}