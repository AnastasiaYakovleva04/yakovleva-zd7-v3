package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout

class PartsListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: PartAdapter
    private val partsList = mutableListOf<Part>()

    private val categories = listOf("Все", "Болт", "Гайка", "Винт", "Шайба", "Шуруп", "Крепеж", "Другое")
    private var currentCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_parts_list, container, false)

        recyclerView = view.findViewById(R.id.partsRecyclerView)
        emptyText = view.findViewById(R.id.emptyText)
        tabLayout = view.findViewById(R.id.tabLayout)

        adapter = PartAdapter(
            partsList,
            onEditClick = { part -> editPart(part) },
            onDeleteClick = { part -> deletePart(part) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupTabs()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadParts(currentCategory)
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
                val category = if (tab?.position == 0) null else categories[tab?.position ?: 0]
                currentCategory = category
                loadParts(category)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    //загрузка деталей
    private fun loadParts(category: String?) {
        val prefs = requireContext().getSharedPreferences("user_session", 0)
        val supplierId = prefs.getInt("user_id", -1)

        if (supplierId == -1) {
            showSnackBar("Ошибка: поставщик не найден")
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val allParts = db.partDao().getPartsBySupplier(supplierId)
                val filteredParts = if (category == null || category == "Все") {
                    allParts
                } else {
                    allParts.filter { it.type == category }
                }

                activity?.runOnUiThread {
                    partsList.clear()
                    partsList.addAll(filteredParts)
                    adapter.updateList(filteredParts)

                    if (filteredParts.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        emptyText.text = if (category == null || category == "Все") {
                            "Детали не добавлены"
                        } else {
                            "Нет деталей типа '$category'"
                        }
                    } else {
                        emptyText.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    showSnackBar("Ошибка загрузки деталей")
                }
            }
        }.start()
    }

    //редактирование детали
    private fun editPart(part: Part) {
        val bundle = Bundle().apply {
            putInt("partId", part.id)
        }
        findNavController().navigate(R.id.action_partsListFragment_to_addPartFragment, bundle)
    }

    //удаление детали
    private fun deletePart(part: Part) {
        Thread {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                db.partDao().delete(part)

                activity?.runOnUiThread {
                    partsList.remove(part)
                    adapter.updateList(partsList)
                    showSnackBar("Деталь удалена: ${part.name}")

                    if (partsList.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    showSnackBar("Ошибка удаления: ${e.message}")
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
}