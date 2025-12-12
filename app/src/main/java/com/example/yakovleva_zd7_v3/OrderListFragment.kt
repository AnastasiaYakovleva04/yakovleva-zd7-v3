package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class OrdersListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: WorkerOrdersAdapter
    private val ordersList = mutableListOf<OrderWithItems>()

    data class OrderWithItems(
        val order: Order,
        val items: List<OrderItem>,
        val client: User?
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.ordersRecyclerView)
        emptyText = view.findViewById(R.id.emptyText)
        tabLayout = view.findViewById(R.id.tabLayout)

        adapter = WorkerOrdersAdapter(ordersList) { orderWithItems ->
            when (orderWithItems.order.status) {
                "Новый" -> startProduction(orderWithItems.order)
                "В производстве" -> openProduction(orderWithItems.order)
                else -> showOrderDetails(orderWithItems.order)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupTabs()
        loadOrders("Новый")
    }

    //при возвращении на экран
    override fun onResume() {
        super.onResume()
        val selectedTab = tabLayout.selectedTabPosition
        val status = when (selectedTab) {
            0 -> "Новый"
            1 -> "В производстве"
            2 -> "Готов"
            else -> "Новый"
        }
        loadOrders(status)
    }

    //настройка таб-навигации
    private fun setupTabs() {
        tabLayout.removeAllTabs()

        val tabs = listOf("Новые", "В производстве", "Готовые")
        tabs.forEach { tabName ->
            tabLayout.addTab(tabLayout.newTab().setText(tabName))
        }

        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.matcha))
        tabLayout.setTabTextColors(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray),
            ContextCompat.getColor(requireContext(), R.color.matcha)
        )

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val status = when (tab?.position) {
                    0 -> "Новый"
                    1 -> "В производстве"
                    2 -> "Готов"
                    else -> "Новый"
                }
                loadOrders(status)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    //открытие экрана производства
    private fun openProduction(order: Order) {
        val bundle = Bundle().apply {
            putLong("orderId", order.id)
        }
        findNavController().navigate(R.id.action_ordersListFragment_to_productionFragment, bundle)
    }

    //загрузка заказов
    private fun loadOrders(status: String) {
        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val orders = db.orderDao().getOrdersByStatus(status)

                val ordersWithItems = mutableListOf<OrderWithItems>()
                orders.forEach { order ->
                    val items = db.orderItemDao().getItemsForOrder(order.id)
                    val client = db.userDao().getAllUsers().find { it.id == order.clientId }
                    ordersWithItems.add(OrderWithItems(order, items, client))
                }

                requireActivity().runOnUiThread {
                    ordersList.clear()
                    ordersList.addAll(ordersWithItems.sortedByDescending { it.order.createdAt })
                    adapter.updateList(ordersWithItems)

                    if (ordersWithItems.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        emptyText.text = when (status) {
                            "Новый" -> "Нет новых заказов"
                            "В производстве" -> "Нет заказов в работе"
                            "Готов" -> "Нет выполненных заказов"
                            else -> "Заказы не найдены"
                        }
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

    //начать производство
    private fun startProduction(order: Order) {
        val prefs = requireContext().getSharedPreferences("user_session", 0)
        val workerId = prefs.getInt("user_id", -1)

        if (workerId == -1) {
            showSnackBar("Ошибка: работник не найден")
            return
        }

        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                db.orderDao().startProduction(order.id, "В производстве", workerId)

                requireActivity().runOnUiThread {
                    showSnackBar("Производство заказа ${order.orderNumber} начато")

                    val bundle = Bundle().apply {
                        putLong("orderId", order.id)
                    }
                    findNavController().navigate(R.id.action_ordersListFragment_to_productionFragment, bundle)

                    val selectedTab = tabLayout.selectedTabPosition
                    val status = when (selectedTab) {
                        0 -> "Новый"
                        1 -> "В производстве"
                        2 -> "Готов"
                        else -> "Новый"
                    }
                    loadOrders(status)
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка: ${e.message}")
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
        fun newInstance() = OrdersListFragment()
    }
}