package com.example.yakovleva_zd7_v3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

class AddPartFragment : Fragment() {

    private lateinit var editName: EditText
    private lateinit var spinnerType: Spinner
    private lateinit var editCharacteristics: EditText
    private lateinit var editPrice: EditText
    private lateinit var buttonSave: Button
    private lateinit var titleText: TextView

    private var currentPartId: Int = -1  // -1 - создание, > 0 - редактирование

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_part, container, false)

        editName = view.findViewById(R.id.editPartName)
        spinnerType = view.findViewById(R.id.editPartType)
        editCharacteristics = view.findViewById(R.id.editCharacteristics)
        editPrice = view.findViewById(R.id.editPrice)
        buttonSave = view.findViewById(R.id.buttonSavePart)
        titleText = view.findViewById(R.id.titleText)

        setupSpinner()
        loadPartData()
        setupButton()

        return view
    }

    //настройка спиннера
    private fun setupSpinner() {
        val partTypes = arrayOf("Болт", "Гайка", "Винт", "Шайба", "Шуруп", "Крепеж", "Другое")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, partTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
    }

    //загрузка данных детали
    private fun loadPartData() {
        arguments?.let {
            currentPartId = it.getInt("partId", -1)
        }

        if (currentPartId != -1) {
            //режим редактирования
            Thread {
                try {
                    val db = AppDatabase.getDatabase(requireContext())
                    val part = db.partDao().getPartById(currentPartId)

                    part?.let {
                        activity?.runOnUiThread {
                             titleText.text = "Редактировать деталь"

                            editName.setText(it.name)
                            editCharacteristics.setText(it.characteristics)
                            editPrice.setText(it.price.toString())

                            val adapter = spinnerType.adapter as ArrayAdapter<String>
                            for (i in 0 until adapter.count) {
                                if (adapter.getItem(i) == it.type) {
                                    spinnerType.setSelection(i)
                                    break
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    //настройка кнопки
    private fun setupButton() {
        buttonSave.setOnClickListener {
            val name = editName.text.toString().trim()
            val type = spinnerType.selectedItem.toString()
            val characteristics = editCharacteristics.text.toString().trim()
            val priceText = editPrice.text.toString().trim()

            if (name.isEmpty() || priceText.isEmpty()) {
                showSnackBar("Заполните все обязательные поля")
                return@setOnClickListener
            }

            val price = try {
                priceText.toDouble()
            } catch (e: NumberFormatException) {
                showSnackBar("Введите корректную цену")
                return@setOnClickListener
            }

            val prefs = requireContext().getSharedPreferences("user_session", 0)
            val supplierId = prefs.getInt("user_id", -1)

            if (supplierId == -1) {
                showSnackBar("Ошибка: пользователь не найден")
                return@setOnClickListener
            }

            Thread {
                try {
                    val db = AppDatabase.getDatabase(requireContext())

                    if (currentPartId == -1) {
                        //создание новой детали
                        val part = Part(
                            name = name,
                            type = type,
                            characteristics = characteristics,
                            supplierId = supplierId,
                            price = price
                        )
                        val partId = db.partDao().insert(part)

                        activity?.runOnUiThread {
                            clearForm()
                            showSnackBar("Деталь добавлена: $name")
                            findNavController().popBackStack()
                        }
                    } else {
                        //обновление существующей детали
                        val part = Part(
                            id = currentPartId,
                            name = name,
                            type = type,
                            characteristics = characteristics,
                            supplierId = supplierId,
                            price = price
                        )
                        db.partDao().update(part)

                        activity?.runOnUiThread {
                            showSnackBar("Деталь обновлена: $name")
                            findNavController().popBackStack() // Возвращаемся назад
                        }
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        showSnackBar("Ошибка: ${e.message}")
                    }
                }
            }.start()
        }
    }

    //очистка полей
    private fun clearForm() {
        editName.text.clear()
        editCharacteristics.text.clear()
        editPrice.text.clear()
        spinnerType.setSelection(0)
    }

    //показ снекбара
    private fun showSnackBar(message: String) {
        val snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG)
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