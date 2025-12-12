package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class ClientOrdersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: ClientOrdersAdapter
    private val ordersList = mutableListOf<OrderWithItems>()

    data class OrderWithItems(
        val order: Order,
        val items: List<OrderItem>
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_client_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.ordersRecyclerView)
        emptyText = view.findViewById(R.id.emptyOrdersText)

        adapter = ClientOrdersAdapter(ordersList) { orderWithItems ->
            showOrderDetails(orderWithItems.order)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadOrders()
    }

    //при возвращении
    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    //загрузка заказов
    private fun loadOrders() {
        val userPrefs = requireContext().getSharedPreferences("user_session", 0)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId == -1) {
            showSnackBar("Ошибка: пользователь не найден")
            return
        }

        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val orders = db.orderDao().getClientOrders(userId)

                val ordersWithItems = mutableListOf<OrderWithItems>()
                orders.forEach { order ->
                    val items = db.orderItemDao().getItemsForOrder(order.id)
                    ordersWithItems.add(OrderWithItems(order, items))
                }

                requireActivity().runOnUiThread {
                    ordersList.clear()
                    ordersList.addAll(ordersWithItems.sortedByDescending { it.order.createdAt })
                    adapter.updateList(ordersWithItems)

                    if (ordersWithItems.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        emptyText.text = getString(R.string.no_orders)
                    } else {
                        emptyText.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка загрузки заказов: ${e.message}")
                }
            }
        }.start()
    }

    //показ деталей заказа
    private fun showOrderDetails(order: Order) {
        showSnackBar("Заказ ${order.orderNumber} - ${order.status}")
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
        fun newInstance() = ClientOrdersFragment()
    }
}