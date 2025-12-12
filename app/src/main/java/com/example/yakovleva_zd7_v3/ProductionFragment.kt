package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class ProductionFragment : Fragment() {

    private lateinit var orderNumberText: TextView
    private lateinit var clientNameText: TextView
    private lateinit var orderStatusText: TextView
    private lateinit var orderTotalText: TextView
    private lateinit var orderItemsText: TextView
    private lateinit var partsRecyclerView: RecyclerView
    private lateinit var completeButton: Button
    private lateinit var emptyText: TextView

    private var orderId: Long = -1
    private var order: Order? = null
    private var client: User? = null
    private var orderItems: List<OrderItem> = emptyList()
    private var allParts: List<Part> = emptyList()
    private var selectedParts = mutableListOf<PartSelection>()

    data class PartSelection(
        val part: Part,
        var isSelected: Boolean = false,
        var quantity: Int = 1
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_production, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        orderNumberText = view.findViewById(R.id.orderNumberText)
        clientNameText = view.findViewById(R.id.clientNameText)
        orderStatusText = view.findViewById(R.id.orderStatusText)
        orderTotalText = view.findViewById(R.id.orderTotalText)
        orderItemsText = view.findViewById(R.id.orderItemsText)
        partsRecyclerView = view.findViewById(R.id.partsRecyclerView)
        completeButton = view.findViewById(R.id.completeButton)
        emptyText = view.findViewById(R.id.emptyText)

        arguments?.let {
            orderId = it.getLong("orderId", -1)
        }

        if (orderId == -1L) {
            showSnackBar("Ошибка: заказ не найден")
            findNavController().popBackStack()
            return
        }

        loadOrderData()
        setupCompleteButton()
    }

    //загрузка данных заказа
    private fun loadOrderData() {
        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())

                order = db.orderDao().getOrderById(orderId)
                order?.let { order ->
                    orderItems = db.orderItemDao().getItemsForOrder(order.id)
                    client = db.userDao().getAllUsers().find { it.id == order.clientId }

                    allParts = db.partDao().getAllParts()

                    val existingParts = db.orderPartDao().getPartsForOrder(order.id)

                    selectedParts.clear()
                    allParts.forEach { part ->
                        val existing = existingParts.find { it.partId == part.id }
                        selectedParts.add(PartSelection(
                            part = part,
                            isSelected = existing != null,
                            quantity = existing?.quantity ?: 1
                        ))
                    }

                    requireActivity().runOnUiThread {
                        updateOrderUI()
                        setupPartsRecyclerView()
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка загрузки данных: ${e.message}")
                }
            }
        }.start()
    }

    //обновление интерфейса заказа
    private fun updateOrderUI() {
        order?.let { order ->
            orderNumberText.text = "Заказ ${order.orderNumber}"
            clientNameText.text = "Клиент: ${client?.name ?: "Неизвестен"}"
            orderStatusText.text = "Статус: ${order.status}"
            orderTotalText.text = "%.2f $".format(order.totalAmount)

            val itemsText = orderItems.joinToString("\n") {
                "• ${it.productName} (${it.quantity} шт) - %.2f $".format(it.price * it.quantity)
            }
            orderItemsText.text = itemsText

            when (order.status) {
                "Новый" -> {
                    orderStatusText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.matcha
                        )
                    )
                    completeButton.text = "Начать производство"
                    completeButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.matcha
                        )
                    )
                    completeButton.isEnabled = true
                }

                "В производстве" -> {
                    orderStatusText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.chai
                        )
                    )
                    completeButton.text = "Завершить производство"
                    completeButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.chai
                        )
                    )
                    completeButton.isEnabled = true
                }

                "Готов" -> {
                    orderStatusText.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.carob
                        )
                    )
                    completeButton.text = "Заказ готов"
                    completeButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.carob
                        )
                    )
                    completeButton.isEnabled = false
                }

                else -> {
                    completeButton.isEnabled = false
                }
            }
        }
    }

    //настройка ресайклера деталей
    private fun setupPartsRecyclerView() {
        val adapter = ProductionPartsAdapter(selectedParts) { partSelection ->
            savePartSelection(partSelection)
        }

        partsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        partsRecyclerView.adapter = adapter

        if (selectedParts.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            partsRecyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            partsRecyclerView.visibility = View.VISIBLE
        }
    }

    //сохранение выбранных деталей
    private fun savePartSelection(partSelection: PartSelection) {
        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())

                if (partSelection.isSelected) {
                    val orderPart = OrderPart(
                        orderId = orderId,
                        partId = partSelection.part.id,
                        quantity = partSelection.quantity,
                        used = false
                    )

                    val existing = db.orderPartDao().getPartsForOrder(orderId)
                        .find { it.partId == partSelection.part.id }

                    if (existing == null) {
                        db.orderPartDao().addPartToOrder(orderPart)
                    } else {
                        db.orderPartDao().updatePartQuantity(orderId, partSelection.part.id, partSelection.quantity)
                    }

                    requireActivity().runOnUiThread {
                        showSnackBar("Добавлено: ${partSelection.part.name}")
                    }
                } else {
                    db.orderPartDao().removePartFromOrder(orderId, partSelection.part.id)

                    requireActivity().runOnUiThread {
                        showSnackBar("Удалено: ${partSelection.part.name}")
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка: ${e.message}")
                }
            }
        }.start()
    }

    //настройка кнопки
    private fun setupCompleteButton() {
        completeButton.setOnClickListener {
            order?.let { order ->
                when (order.status) {
                    "Новый" -> startProduction()
                    "В производстве" -> completeProduction()
                    else -> showSnackBar("Неизвестный статус заказа")
                }
            }
        }
    }

    //начать произваодство
    private fun startProduction() {
        val prefs = requireContext().getSharedPreferences("user_session", 0)
        val workerId = prefs.getInt("user_id", -1)

        if (workerId == -1) {
            showSnackBar("Ошибка: работник не найден")
            return
        }

        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                db.orderDao().startProduction(orderId, "В производстве", workerId)

                requireActivity().runOnUiThread {
                    showSnackBar("Производство начато")
                    order?.status = "В производстве"
                    updateOrderUI()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка: ${e.message}")
                }
            }
        }.start()
    }

    //закончить производство
    private fun completeProduction() {
        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val selectedPartIds = selectedParts
                    .filter { it.isSelected }
                    .map { it.part.id }

                selectedPartIds.forEach { partId ->
                    db.orderPartDao().markPartUsed(orderId, partId)
                }

                db.orderDao().completeOrder(orderId, "Готов", System.currentTimeMillis())

                requireActivity().runOnUiThread {
                    showSnackBar("Производство заказа завершено!")
                    order?.status = "Готов"
                    updateOrderUI()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка: ${e.message}")
                }
            }
        }.start()
    }

    //показ снекбара
    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.vanilla)
        )
        snackbar.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.carob)
        )
        snackbar.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(orderId: Long) = ProductionFragment().apply {
            arguments = Bundle().apply {
                putLong("orderId", orderId)
            }
        }
    }
}