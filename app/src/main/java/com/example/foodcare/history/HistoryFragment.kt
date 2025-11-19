package com.example.foodcare.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodcare.R
import com.example.foodcare.data.database.AppDatabase
import com.example.foodcare.data.model.HistoryEvent
import com.example.foodcare.data.repository.HistoryRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var countValueText: TextView
    private lateinit var emptyHistoryText: TextView
    private lateinit var searchEditText: TextInputEditText
    private lateinit var tabsGroup: MaterialButtonToggleGroup
    private lateinit var filtersButton: MaterialButton
    private lateinit var backButton: ImageButton

    private val allEvents: MutableList<HistoryEvent> = mutableListOf()
    private var currentFilter: HistoryFilter = HistoryFilter()

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Репозиторий истории
    private val historyRepository: HistoryRepository by lazy {
        val db = AppDatabase.getDatabase(requireContext().applicationContext)
        HistoryRepository(db.historyDao())
    }

    // Категории для фильтра
    private val categories: List<String> = listOf(
        "Все категории",
        "Молочные продукты",
        "Мясо, птица",
        "Овощи",
        "Фрукты",
        "Напитки",
        "Хлебобулочные изделия",
        "Бакалея",
        "Замороженные продукты",
        "Сладости",
        "Яйца",
        "Консервы",
        "Снэки",
        "Прочее"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        countValueText = view.findViewById(R.id.countValue)
        emptyHistoryText = view.findViewById(R.id.emptyHistoryText)
        searchEditText = view.findViewById(R.id.searchEditText)
        tabsGroup = view.findViewById(R.id.historyTabs)
        filtersButton = view.findViewById(R.id.filtersButton)
        backButton = view.findViewById(R.id.backButton)

        setupRecyclerView()
        setupBackButton()
        setupSearch()
        setupTabs()
        setupFiltersButton()
        observeHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // не нужно
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val query = s?.toString().orEmpty()
                currentFilter = currentFilter.copy(query = query)
                applyFilterAndUpdateUi()
            }

            override fun afterTextChanged(s: Editable?) {
                // не нужно
            }
        })
    }

    private fun setupTabs() {
        tabsGroup.addOnButtonCheckedListener { _: MaterialButtonToggleGroup, checkedId: Int, isChecked: Boolean ->
            if (!isChecked) return@addOnButtonCheckedListener

            currentFilter = when (checkedId) {
                R.id.allTabButton -> currentFilter.copy(status = null)
                R.id.usedTabButton -> currentFilter.copy(status = "USED")
                R.id.discardedTabButton -> currentFilter.copy(status = "DISCARDED")
                else -> currentFilter.copy(status = null)
            }

            applyFilterAndUpdateUi()
        }

        tabsGroup.check(R.id.allTabButton)
    }

    private fun setupFiltersButton() {
        filtersButton.setOnClickListener {
            showFiltersBottomSheet()
        }
    }

    private fun observeHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            historyRepository.getHistory().collectLatest { list ->
                allEvents.clear()
                allEvents.addAll(list)
                applyFilterAndUpdateUi()
            }
        }
    }

    // ---------- ПРИМЕНЕНИЕ ФИЛЬТРОВ ----------

    private fun applyFilterAndUpdateUi() {
        val filtered: List<HistoryEvent> = allEvents.filter { event ->
            // статус
            currentFilter.status?.let { status ->
                if (event.actionType != status) return@filter false
            }

            // поиск по названию
            if (currentFilter.query.isNotBlank() &&
                !event.productName.contains(currentFilter.query, ignoreCase = true)
            ) {
                return@filter false
            }

            // категория
            currentFilter.category?.let { category ->
                if (event.category != category) return@filter false
            }

            // дата "с"
            currentFilter.dateFromMillis?.let { from ->
                if (stripTime(event.createdAt) < stripTime(from)) return@filter false
            }

            // дата "по"
            currentFilter.dateToMillis?.let { to ->
                if (stripTime(event.createdAt) > stripTime(to)) return@filter false
            }

            true
        }

        historyAdapter.submitList(filtered)
        countValueText.text = filtered.size.toString()
        emptyHistoryText.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun stripTime(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // ---------- BottomSheet с фильтрами ----------

    private fun showFiltersBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(
            R.layout.bottom_sheet_history_filters,
            null
        )
        dialog.setContentView(sheetView)

        val categoryInputLayout =
            sheetView.findViewById<TextInputLayout>(R.id.categoryInputLayout)
        val categoryAutoComplete =
            sheetView.findViewById<AutoCompleteTextView>(R.id.categoryAutoComplete)
        val dateFromText = sheetView.findViewById<TextView>(R.id.dateFromText)
        val dateToText = sheetView.findViewById<TextView>(R.id.dateToText)
        val resetButton = sheetView.findViewById<MaterialButton>(R.id.resetButton)
        val applyButton = sheetView.findViewById<MaterialButton>(R.id.applyButton)

        // Категории
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            categories
        )
        categoryAutoComplete.setAdapter(adapter)

        val currentCategoryLabel = currentFilter.category ?: "Все категории"
        categoryAutoComplete.setText(currentCategoryLabel, false)

        // Даты
        dateFromText.text = currentFilter.dateFromMillis?.let { dateFormatter.format(Date(it)) }
            ?: "Не выбрана"
        dateToText.text = currentFilter.dateToMillis?.let { dateFormatter.format(Date(it)) }
            ?: "Не выбрана"

        var tempDateFrom: Long? = currentFilter.dateFromMillis
        var tempDateTo: Long? = currentFilter.dateToMillis

        dateFromText.setOnClickListener {
            showDatePickerDialog(tempDateFrom) { selected ->
                tempDateFrom = selected
                dateFromText.text = selected?.let { ms -> dateFormatter.format(Date(ms)) }
                    ?: "Не выбрана"
            }
        }

        dateToText.setOnClickListener {
            showDatePickerDialog(tempDateTo) { selected ->
                tempDateTo = selected
                dateToText.text = selected?.let { ms -> dateFormatter.format(Date(ms)) }
                    ?: "Не выбрана"
            }
        }

        resetButton.setOnClickListener {
            currentFilter = currentFilter.copy(
                category = null,
                dateFromMillis = null,
                dateToMillis = null
            )
            applyFilterAndUpdateUi()
            dialog.dismiss()
        }

        applyButton.setOnClickListener {
            val selectedCategoryLabel = categoryAutoComplete.text?.toString()
            val categoryValue =
                if (selectedCategoryLabel.isNullOrBlank() ||
                    selectedCategoryLabel == "Все категории"
                ) {
                    null
                } else {
                    selectedCategoryLabel
                }

            currentFilter = currentFilter.copy(
                category = categoryValue,
                dateFromMillis = tempDateFrom,
                dateToMillis = tempDateTo
            )

            applyFilterAndUpdateUi()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePickerDialog(
        initialMillis: Long?,
        onDateSelected: (Long?) -> Unit
    ) {
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")

        if (initialMillis != null) {
            builder.setSelection(initialMillis)
        }

        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            onDateSelected(selection)
        }
        picker.addOnNegativeButtonClickListener {
            onDateSelected(null)
        }
        picker.addOnCancelListener {
            // ничего не делаем
        }

        picker.show(parentFragmentManager, "history_date_picker")
    }
}
