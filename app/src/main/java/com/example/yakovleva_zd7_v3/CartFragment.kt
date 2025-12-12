package com.example.yakovleva_zd7_v3

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class CartFragment : Fragment() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var checkoutButton: Button
    private lateinit var emptyCartText: TextView
    private var cartItems = mutableListOf<CartItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        totalPriceText = view.findViewById(R.id.totalPrice)
        checkoutButton = view.findViewById(R.id.checkoutButton)
        emptyCartText = view.findViewById(R.id.emptyCartText)

        // Настраиваем RecyclerView
        cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cartRecyclerView.adapter = CartAdapter(cartItems) { item, action ->
            when (action) {
                "increase" -> increaseQuantity(item)
                "decrease" -> decreaseQuantity(item)
                "remove" -> removeItem(item)
            }
        }

        checkoutButton.setOnClickListener {
            checkout()
        }

        Log.d("CartFragment", "onViewCreated - Загрузка корзины")
        loadCartItems()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d("CartFragment", "onResume - Загрузка корзины")
        loadCartItems()
        updateUI()
    }

    //загрузить корзину
    private fun loadCartItems() {
        cartItems.clear()

        val userPrefs = requireContext().getSharedPreferences("user_session", 0)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId == -1) {
            return
        }

        val cartKey = "cart_$userId"
        val cartPrefs = requireContext().getSharedPreferences(cartKey, 0)

        val cartSet = cartPrefs.getStringSet("items", LinkedHashSet()) ?: LinkedHashSet()

        cartSet.forEach { itemString ->
            val parts = itemString.split("|")
            if (parts.size >= 4) {
                val item = CartItem(
                    id = parts[0],
                    name = parts[1],
                    price = parts[2].toDouble(),
                    quantity = parts[3].toInt()
                )
                cartItems.add(item)
            }
        }

        cartRecyclerView.adapter?.notifyDataSetChanged()
    }

    //сохранить корзину
    private fun saveCartItems() {
        // Получаем ID текущего пользователя
        val userPrefs = requireContext().getSharedPreferences("user_session", 0)
        val userId = userPrefs.getInt("user_id", -1)

        Log.d("CartFragment", "saveCartItems - userId: $userId, items count: ${cartItems.size}")

        if (userId == -1) {
            showSnackBar("Ошибка: пользователь не найден")
            return
        }

        val cartKey = "cart_$userId"
        val cartPrefs = requireContext().getSharedPreferences(cartKey, 0)
        val cartSet = mutableSetOf<String>()

        cartItems.forEach { item ->
            cartSet.add("${item.id}|${item.name}|${item.price}|${item.quantity}")
            Log.d("CartFragment", "Сохранение: ${item.name} x${item.quantity}")
        }

        Log.d("CartFragment", "saveCartItems - всего товаров для сохранения: ${cartSet.size}")

        val success = cartPrefs.edit().putStringSet("items", cartSet).commit()

        if (success) {
            Log.d("CartFragment", "Корзина успешно сохранена в $cartKey")
        } else {
            Log.e("CartFragment", "Ошибка сохранения корзины в $cartKey")
        }
    }


    private fun increaseQuantity(item: CartItem) {
        item.quantity++
        saveCartItems()
        updateUI()
        showSnackBar("Количество увеличено: ${item.name}")
    }

    private fun decreaseQuantity(item: CartItem) {
        if (item.quantity > 1) {
            item.quantity--
            saveCartItems()
            updateUI()
            showSnackBar("Количество уменьшено: ${item.name}")
        } else {
            removeItem(item)
        }
    }

    private fun removeItem(item: CartItem) {
        cartItems.remove(item)
        saveCartItems()
        updateUI()
        showSnackBar("Товар удален: ${item.name}")
    }

    private fun updateUI() {
        val total = cartItems.sumOf { it.price * it.quantity }
        totalPriceText.text = "%.2f $".format(total)

        if (cartItems.isEmpty()) {
            emptyCartText.visibility = View.VISIBLE
            cartRecyclerView.visibility = View.GONE
            checkoutButton.isEnabled = false
            checkoutButton.text = "Корзина пуста"
        } else {
            emptyCartText.visibility = View.GONE
            cartRecyclerView.visibility = View.VISIBLE
            checkoutButton.isEnabled = true
            checkoutButton.text = "Оформить заказ (${cartItems.size})"
        }

        cartRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun checkout() {
        if (cartItems.isEmpty()) {
            showSnackBar("Корзина пуста")
            return
        }

        val userPrefs = requireContext().getSharedPreferences("user_session", 0)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId == -1) {
            showSnackBar("Ошибка: пользователь не найден")
            return
        }

        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())

                val totalAmount = cartItems.sumOf { it.price * it.quantity }
                val orderNumber = "ORD-${System.currentTimeMillis()}"
                val newOrder = Order(
                    orderNumber = orderNumber,
                    clientId = userId,
                    status = "Новый",
                    totalAmount = totalAmount
                )

                val orderId = db.orderDao().createOrder(newOrder)

                cartItems.forEach { cartItem ->
                    val category = when {
                        cartItem.name.contains("стол", ignoreCase = true) -> "table"
                        cartItem.name.contains("стул", ignoreCase = true) -> "chair"
                        cartItem.name.contains("диван", ignoreCase = true) -> "sofa"
                        cartItem.name.contains("шкаф", ignoreCase = true) -> "shelf"
                        else -> "other"
                    }

                    val orderItem = OrderItem(
                        orderId = orderId,
                        productId = cartItem.id,
                        productName = cartItem.name,
                        quantity = cartItem.quantity,
                        price = cartItem.price,
                        category = category
                    )
                    db.orderItemDao().insert(orderItem)
                }

                val cartKey = "cart_$userId"
                val cartPrefs = requireContext().getSharedPreferences(cartKey, 0)
                cartPrefs.edit().clear().commit()

                cartItems.clear()

                requireActivity().runOnUiThread {
                    updateUI()
                    showSnackBar("Заказ №$orderNumber успешно создан!")
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    showSnackBar("Ошибка создания заказа: ${e.message}")
                }
            }
        }.start()
    }

    //показ снекбара
    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vanilla))
        snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.carob))
        snackbar.show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CartFragment()
    }
}