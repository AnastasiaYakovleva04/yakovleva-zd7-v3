package com.example.yakovleva_zd7_v3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductionPartsAdapter(
    private var parts: List<ProductionFragment.PartSelection>,
    private val onPartSelectionChanged: (ProductionFragment.PartSelection) -> Unit
) : RecyclerView.Adapter<ProductionPartsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.partCheckbox)
        val partName: TextView = view.findViewById(R.id.partName)
        val partType: TextView = view.findViewById(R.id.partType)
        val partCharacteristics: TextView = view.findViewById(R.id.partCharacteristics)
        val partPrice: TextView = view.findViewById(R.id.partPrice)
        val supplierInfo: TextView = view.findViewById(R.id.supplierInfo)
        val quantityEdit: EditText = view.findViewById(R.id.quantityEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_production_part, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val partSelection = parts[position]
        val part = partSelection.part

        holder.checkBox.isChecked = partSelection.isSelected
        holder.partName.text = part.name
        holder.partType.text = "Тип: ${part.type}"
        holder.partCharacteristics.text = "Характеристики: ${part.characteristics}"
        holder.partPrice.text = "%.2f $".format(part.price)
        holder.supplierInfo.text = "Поставщик ID: ${part.supplierId}"
        holder.quantityEdit.setText(partSelection.quantity.toString())

        holder.quantityEdit.isEnabled = partSelection.isSelected

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            partSelection.isSelected = isChecked
            holder.quantityEdit.isEnabled = isChecked
            onPartSelectionChanged(partSelection)
        }

        holder.quantityEdit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                try {
                    val quantity = holder.quantityEdit.text.toString().toInt()
                    if (quantity > 0) {
                        partSelection.quantity = quantity
                        onPartSelectionChanged(partSelection)
                    }
                } catch (e: NumberFormatException) {
                    holder.quantityEdit.setText(partSelection.quantity.toString())
                }
            }
        }
    }

    override fun getItemCount() = parts.size

    fun updateList(newParts: List<ProductionFragment.PartSelection>) {
        parts = newParts
        notifyDataSetChanged()
    }
}