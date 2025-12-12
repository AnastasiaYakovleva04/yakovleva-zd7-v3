package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.constraintlayout.helper.widget.Carousel
import androidx.constraintlayout.motion.widget.MotionLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import android.util.Log

class CatalogFragment : Fragment() {

    private lateinit var carousel: Carousel
    private lateinit var tabLayout: TabLayout
    private lateinit var motionLayout: MotionLayout
    private lateinit var btnPrev: ImageButton
    private lateinit var btnNext: ImageButton

    private val categories = listOf("Все", "Столы", "Стулья", "Диваны", "Лампы")
    private var currentProducts: List<Product> = emptyList()
    private var currentIndex = 0
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_catalog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        carousel = view.findViewById(R.id.carousel)
        tabLayout = view.findViewById(R.id.tabLayout)
        motionLayout = view.findViewById(R.id.motionLayout)
        btnPrev = view.findViewById(R.id.btn_prev)
        btnNext = view.findViewById(R.id.btn_next)

        setupTabs()
        setupCarousel()

        btnPrev.setOnClickListener {
            if (currentProducts.isNotEmpty()) {
                carousel.transitionToIndex(carousel.currentIndex - 1, 0)
                motionLayout.transitionToState(R.id.previous)
            }
        }

        btnNext.setOnClickListener {
            if (currentProducts.isNotEmpty()) {
                carousel.transitionToIndex(carousel.currentIndex + 1, 0)
                motionLayout.transitionToState(R.id.next)
            }
        }

        motionLayout.post {
            loadProducts(null)
        }
    }

    //настройка таб-навигации
    private fun setupTabs() {
        tabLayout.removeAllTabs()

        categories.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.matcha))
        tabLayout.setTabTextColors(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray),
            ContextCompat.getColor(requireContext(), R.color.matcha)
        )

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (isLoading) return

                val category = if (tab?.position == 0) null else categories[tab?.position ?: 0]
                loadProducts(category)
                currentIndex = 0

                tab?.view?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.almond))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    //настройка карусели
    private fun setupCarousel() {
        carousel.setAdapter(object : Carousel.Adapter {
            override fun populate(view: View?, index: Int) {
                if (view == null || currentProducts.isEmpty()) return

                val productIndex = (currentIndex + index - 1).mod(currentProducts.size)
                val product = if (productIndex in currentProducts.indices) {
                    currentProducts[productIndex]
                } else {
                    currentProducts.firstOrNull() ?: return
                }

                val cardIndex = when (view.id) {
                    R.id.product_card_1 -> 0
                    R.id.product_card_2 -> 1
                    R.id.product_card_3 -> 2
                    else -> return
                }

                fillCardData(view, cardIndex, product)
            }

            override fun count(): Int {
                return if (currentProducts.isEmpty()) 0 else currentProducts.size * 1000
            }

            override fun onNewItem(index: Int) {
                currentIndex = index

                view?.postDelayed({
                    motionLayout.transitionToState(R.id.start)
                }, 200)
            }
        })
    }

    //заполнение карточек товаров данными
    private fun fillCardData(cardView: View, cardIndex: Int, product: Product) {
        try {
            val nameId = when (cardIndex) {
                0 -> R.id.product_name_1
                1 -> R.id.product_name_2
                2 -> R.id.product_name_3
                else -> R.id.product_name_1
            }

            val descriptionId = when (cardIndex) {
                0 -> R.id.product_category_1
                1 -> R.id.product_category_2
                2 -> R.id.product_category_3
                else -> R.id.product_category_1
            }

            val priceId = when (cardIndex) {
                0 -> R.id.product_price_1
                1 -> R.id.product_price_2
                2 -> R.id.product_price_3
                else -> R.id.product_price_1
            }

            val imageId = when (cardIndex) {
                0 -> R.id.product_image_1
                1 -> R.id.product_image_2
                2 -> R.id.product_image_3
                else -> R.id.product_image_1
            }

            val buttonId = when (cardIndex) {
                0 -> R.id.add_to_cart_1
                1 -> R.id.add_to_cart_2
                2 -> R.id.add_to_cart_3
                else -> R.id.add_to_cart_1
            }

            val nameView = cardView.findViewById<TextView>(nameId)
            val descriptionView = cardView.findViewById<TextView>(descriptionId)
            val priceView = cardView.findViewById<TextView>(priceId)
            val imageView = cardView.findViewById<ImageView>(imageId)
            val buttonView = cardView.findViewById<Button>(buttonId)

            nameView.text = product.name

            if (product.description != null && product.description.isNotEmpty()) {
                val maxLength = 60
                val description = if (product.description.length > maxLength) {
                    product.description.substring(0, maxLength) + "..."
                } else {
                    product.description
                }
                descriptionView.text = description
            } else {
                descriptionView.text = when (product.category.lowercase()) {
                    "chair" -> "Комфортный стул премиум-класса"
                    "table" -> "Стильный стол из натурального дерева"
                    "sofa" -> "Удобный диван с ортопедическим эффектом"
                    "lamp" -> "Настольная лампа с теплым светом"
                    else -> "Высококачественная мебель"
                }
            }

            priceView.text = "%.2f $".format(product.price)

            if (product.imagePath.isNotEmpty()) {
                Picasso.get()
                    .load(product.imagePath)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(imageView)
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            buttonView.setOnClickListener {
                addToCart(product)
                showSnackBar(requireView(), "Добавлено в корзину: ${product.name}")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //загрузка товаров
    private fun loadProducts(category: String?) {
        if (isLoading) return

        isLoading = true
        currentProducts = emptyList()

        tryLoadFromApi(category)
    }

    //попытка загрузить продукты с апи
    private fun tryLoadFromApi(category: String?) {
        val url = if (category != null && category != "Все") {
            val apiCategory = when (category) {
                "Столы" -> "table"
                "Стулья" -> "chair"
                "Диваны" -> "sofa"
                "Лампы" -> "lamp"
                else -> category.lowercase()
            }
            "https://furniture-api.fly.dev/v1/products?category=$apiCategory&limit=30"
        } else {
            "https://furniture-api.fly.dev/v1/products?limit=30"
        }

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val productResponse = Gson().fromJson(response.toString(), ProductResponse::class.java)

                    if (productResponse.success && productResponse.data.isNotEmpty()) {
                        currentProducts = productResponse.data

                        activity?.runOnUiThread {
                            carousel.refresh()
                            currentIndex = 0
                            isLoading = false
                        }
                    } else {
                        loadTestProducts(category)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    loadTestProducts(category)
                }
            },
            { error ->
                error.printStackTrace()
                loadTestProducts(category)
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)
    }

    //загрузка тестовых товаров
    private fun loadTestProducts(category: String?) {
        val testProducts = listOf(
            Product(id = "1", name = "Деревянный обеденный стол", category = "table",
                description = "Стол из натурального дуба, размер 180x90 см", price = 299.99,
                imagePath = ""),
            Product(id = "2", name = "Кожаный угловой диван", category = "sofa",
                description = "Диван из натуральной кожи, угловая модель", price = 899.99,
                imagePath = ""),
            Product(id = "3", name = "Офисный стул с поддержкой спины", category = "chair",
                description = "Эргономичный офисный стул с регулировкой высоты", price = 149.99,
                imagePath = ""),
            Product(id = "4", name = "Книжный шкаф из дерева", category = "shelf",
                description = "Вместительный книжный шкаф, 5 полок", price = 399.99,
                imagePath = ""),
            Product(id = "5", name = "Журнальный столик со стеклом", category = "table",
                description = "Стильный журнальный столик со стеклянной столешницей", price = 199.99,
                imagePath = "")
        )

        val filteredProducts = if (category != null && category != "Все") {
            when (category) {
                "Столы" -> testProducts.filter { it.category == "table" }
                "Диваны" -> testProducts.filter { it.category == "sofa" }
                "Стулья" -> testProducts.filter { it.category == "chair" }
                "Лампы" -> testProducts.filter { it.category == "lamp" }
                else -> testProducts.filter { it.category.equals(category, ignoreCase = true) }
            }
        } else {
            testProducts
        }

        currentProducts = filteredProducts

        activity?.runOnUiThread {
            carousel.refresh()
            currentIndex = 0
            isLoading = false

            if (filteredProducts.isNotEmpty()) {
                showSnackBar(requireView(), "Загружены тестовые товары")
            }
        }
    }

    //добавление в корзину
    private fun addToCart(product: Product) {
        val userPrefs = requireContext().getSharedPreferences("user_session", 0)
        val userId = userPrefs.getInt("user_id", -1)

        if (userId == -1) {
            showSnackBar(requireView(), "Ошибка: пользователь не найден")
            return
        }

        val cartKey = "cart_$userId"
        val prefs = requireContext().getSharedPreferences(cartKey, 0)

        val cartSet = LinkedHashSet(prefs.getStringSet("items", LinkedHashSet()) ?: LinkedHashSet())

        val existingItem = cartSet.find { it.startsWith("${product.id}|") }

        if (existingItem != null) {
            val parts = existingItem.split("|")
            if (parts.size >= 4) {
                val quantity = parts[3].toInt() + 1
                cartSet.remove(existingItem)
                cartSet.add("${product.id}|${product.name}|${product.price}|$quantity")
            }
        } else {
            cartSet.add("${product.id}|${product.name}|${product.price}|1")
        }

        val editor = prefs.edit()
        editor.putStringSet("items", cartSet)
        editor.commit()

        showSnackBar(requireView(), "Товар '${product.name}' добавлен в корзину")
    }

    //показ снекбара
    private fun showSnackBar(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.vanilla))
        snackbar.setTextColor(ContextCompat.getColor(requireContext(), R.color.carob))
        snackbar.show()
    }
}